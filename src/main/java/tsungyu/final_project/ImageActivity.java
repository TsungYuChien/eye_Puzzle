package tsungyu.final_project;


import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static java.lang.Math.abs;

public class ImageActivity extends AppCompatActivity {

    ArrayList<puzzlePiece> pieces;
    String CurrentPhotoPath;    //Path from camera
    String CurrentPhotoUri;     //Uri from gallery

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imgactivity);

        final RelativeLayout layout = findViewById(R.id.mainLayout);
        final ImageView imageView = findViewById(R.id.mainimg);

        Intent intent=getIntent();
        final String assetName=intent.getStringExtra("assetName");

        CurrentPhotoPath=intent.getStringExtra("CurrentPhotoPath");
        CurrentPhotoUri=intent.getStringExtra("CurrentPhotoUri");

        // run image related code after the view was laid out
        // to have all dimensions calculated
        imageView.post(new Runnable() {
            @Override
            public void run() {

            if(assetName!=null){
                setpicfromasset(assetName,imageView);
            }else if(CurrentPhotoPath!=null){
                setpicfrompath(CurrentPhotoPath,imageView);
            }else if(CurrentPhotoUri!=null){
                imageView.setImageURI(Uri.parse(CurrentPhotoUri));
            }

                pieces = splitImage();                               //先切割
                TouchListen touchlistener =new TouchListen(ImageActivity.this);

                Collections.shuffle(pieces);                         //洗牌

                for (puzzlePiece piece : pieces) {                   //piece繼承pieces
                    piece.setOnTouchListener(touchlistener);
                    layout.addView(piece);

                    //隨機位置（在最下面）
                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) piece.getLayoutParams();
                    params.leftMargin=new Random().nextInt(layout.getWidth()-piece.pieceW);
                    params.topMargin=layout.getHeight()-piece.pieceH;

                    piece.setLayoutParams(params);

                }
            }

        });


    }

    private void setpicfromasset(String assetName,ImageView imageView){
        int targetW=imageView.getWidth();
        int targetH=imageView.getHeight();

        AssetManager assetManager=getAssets();
        try{

            InputStream inputStream=assetManager.open("image/"+assetName);
            BitmapFactory.Options bmOption=new BitmapFactory.Options();

            bmOption.inJustDecodeBounds=true;
            BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOption);

            int photoW=bmOption.outWidth;
            int photoH=bmOption.outHeight;

            int scalefactor=Math.min(photoW/targetW,photoH/targetH);

            inputStream.reset();

            //把image解碼放進bitmap
            bmOption.inJustDecodeBounds=false;
            bmOption.inSampleSize=scalefactor;
            bmOption.inPurgeable=true;

            Bitmap bitmap= BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOption);
            imageView.setImageBitmap(bitmap);

        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private void setpicfrompath(String CurrentPhotoPath,ImageView imageView){

        int targetW=imageView.getWidth();
        int targetH=imageView.getHeight();


        BitmapFactory.Options bmOption=new BitmapFactory.Options();

        bmOption.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(CurrentPhotoPath,bmOption);

        int photoW=bmOption.outWidth;
        int photoH=bmOption.outHeight;

        int scalefactor=Math.min(photoW/targetW,photoH/targetH);


        //把image解碼放進bitmap
        bmOption.inJustDecodeBounds=false;
        bmOption.inSampleSize=scalefactor;
        bmOption.inPurgeable=true;

        Bitmap bitmap= BitmapFactory.decodeFile(CurrentPhotoPath,bmOption);
        Bitmap rotatedBitmap=bitmap;

        //如果需要rotate
        try{
            ExifInterface exifInterface=new ExifInterface(CurrentPhotoPath);

            int orientation=exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap=rotateImage(bitmap,90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap=rotateImage(bitmap,180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap=rotateImage(bitmap,270);
                    break;
            }

        }catch (IOException e){
            Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
        }

        imageView.setImageBitmap(rotatedBitmap);
    }

    public static Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix=new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    private ArrayList<puzzlePiece> splitImage(){

        int pieceNum=12;
        int row=4;
        int col=3;

        ImageView imageView = findViewById(R.id.mainimg);
        ArrayList<puzzlePiece> pieces=new ArrayList<>(pieceNum);

        BitmapDrawable drawable=(BitmapDrawable) imageView.getDrawable();       //先把圖檔放到drawable
        Bitmap bitmap=drawable.getBitmap();                                     //再把drawable轉成bitmap

        int[] dime=bitmapPosInImgview(imageView);
        int scaleBitmapL=dime[0];
        int scaleBitmapT=dime[1];
        int scaleBitmapW=dime[2];
        int scaleBitmapH=dime[3];

        int cropImgW=scaleBitmapW-2*abs(scaleBitmapL);
        int cropImgH=scaleBitmapH-2*abs(scaleBitmapT);

        Bitmap scaleBitmap=Bitmap.createScaledBitmap(bitmap,scaleBitmapW,scaleBitmapH,true);
        Bitmap cropBitmap=Bitmap.createBitmap(scaleBitmap,abs(scaleBitmapL),abs(scaleBitmapT),cropImgW,cropImgH);


        int pieceW=cropImgW/col;
        int pieceH=cropImgH/row;

        int ysite=0;
        for(int i=0;i<row;i++){
            int xsite=0;
            for(int j=0;j<col;j++){

                //先算每塊的offset 為了到時候加上邊框用的
                int offsetX=0;
                int offsetY=0;
                if(j>0){
                    offsetX=pieceW/3;
                }
                if(i>0){
                    offsetY=pieceH/3;
                }


                Bitmap pieceBitmap=Bitmap.createBitmap(cropBitmap,xsite-offsetX,ysite-offsetY,pieceW+offsetX,pieceH+offsetY);
                puzzlePiece piece=new puzzlePiece(getApplicationContext());
                piece.setImageBitmap(pieceBitmap);
                piece.xsite=xsite-offsetX+imageView.getLeft();
                piece.ysite=ysite-offsetY+imageView.getTop();
                piece.pieceW=pieceW+offsetX;
                piece.pieceH=pieceH+offsetY;

                Bitmap puzzlePiece=Bitmap.createBitmap(pieceW+offsetX,pieceH+offsetY,Bitmap.Config.ARGB_8888);

                //畫圖的路徑
                int bumpSize=pieceH/4;
                Canvas canvas=new Canvas(puzzlePiece);
                Path path=new Path();
                path.moveTo(offsetX,offsetY);

                //上
                if(i==0){
                    //最上面那一塊
                    path.lineTo(pieceBitmap.getWidth(),offsetY);
                }else{
                    path.lineTo(offsetX+(pieceBitmap.getWidth()-offsetX)/3,offsetY);
                    path.lineTo(pieceBitmap.getWidth(),offsetY);
                }

                //右
                if(j==col-1){
                    //最右邊一塊
                    path.lineTo(pieceBitmap.getWidth(),pieceBitmap.getHeight());
                }else{
                    path.lineTo(pieceBitmap.getWidth(),offsetY+(pieceBitmap.getHeight()-offsetY)/3);
                    path.lineTo(pieceBitmap.getWidth(),pieceBitmap.getHeight());
                }

                //下
                if(i==row-1) {
                    path.lineTo(offsetX, pieceBitmap.getHeight());
                }else {
                    path.lineTo(offsetX+(pieceBitmap.getWidth()-offsetX)/3*2,pieceBitmap.getHeight());
                    path.lineTo(offsetX,pieceBitmap.getHeight());
                }

                //左
                if(j==0){
                    path.close();
                }else{
                    path.lineTo(offsetX,offsetY+(pieceBitmap.getHeight()-offsetY)/3*2);
                    path.close();
                }


                Paint paint=new Paint();
                paint.setColor(0XFF000000);
                paint.setStyle(Paint.Style.FILL);

                canvas.drawPath(path,paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(pieceBitmap,0,0,paint);

                //加上邊框
                Paint border=new Paint();
                border.setColor(Color.WHITE);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(8.0f);
                canvas.drawPath(path,border);

                border=new Paint();
                border.setColor(Color.BLACK);
                border.setStyle(Paint.Style.STROKE);
                border.setStrokeWidth(3.0f);
                canvas.drawPath(path,border);

                piece.setImageBitmap(puzzlePiece);


                pieces.add(piece);

                xsite+=pieceW;
            }
            ysite+=pieceH;
        }
        return pieces;

    }

    private int[] bitmapPosInImgview(ImageView imageView){              //bitmap在imageview的位置(取得寬、高和左、上)

        int []re=new int[4];

        if(imageView==null||imageView.getDrawable()==null){
            return re;
        }

        float[] f=new float[9];
        imageView.getImageMatrix().getValues(f);

        final float scalex=f[Matrix.MSCALE_X];
        final float scaley=f[Matrix.MSCALE_Y];

        final Drawable d=imageView.getDrawable();
        final int originalW=d.getIntrinsicWidth();
        final int originalH=d.getIntrinsicHeight();

        final int actualW=Math.round(originalW * scalex);
        final int actualH=Math.round(originalH * scaley);

        re[2]=actualW;
        re[3]=actualH;

        //照片位置
        int imgViewW=imageView.getWidth();
        int imgViewH=imageView.getHeight();

        int top=(int)(imgViewH-actualH)/2;
        int left=(int)(imgViewW-actualW)/2;

        re[0]=left;
        re[1]=top;

        return re;

    }

    //利用可否移動來判斷遊戲是否結束了
    private boolean isGameover(){
        for(puzzlePiece piece: pieces){
            if(piece.canMove){
                return false;
            }
        }
        return true;
    }

    public void checkGameover(){
        if(isGameover()){
            finish();
        }
    }



}
