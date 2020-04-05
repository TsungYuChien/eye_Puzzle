package tsungyu.final_project;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;

public class ImageAdapter extends BaseAdapter {

    private Context mycontext;
    private AssetManager assetManager;
    private String[] file;

    public ImageAdapter(Context c){

        mycontext=c;
        assetManager=mycontext.getAssets();
        try{
            file=assetManager.list("image");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public int getCount() {             //有多少筆資料
        return file.length;
    }

    @Override
    public Object getItem(int i) {      //獲取item
        return null;
    }

    @Override
    public long getItemId(int i) {      //透過id拿item
        return 0;
    }


    //創建新的imageview給每個item(position,view,viewgroup)
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        if(view==null){
            final LayoutInflater layoutInflater=LayoutInflater.from(mycontext);
            view=layoutInflater.inflate(R.layout.gridelement,null);
        }

        final ImageView imageView=view.findViewById(R.id.gridImgView);

        imageView.setImageBitmap(null);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                new AsyncTask<Void,Void,Void>(){
                    private Bitmap bitmap;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        bitmap = getpicfromasset(imageView, file[i]);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        imageView.setImageBitmap(bitmap);
                    }
                }.execute();
            }
        });

        return view;
    }

    //這個會將所有asset/image的檔案載入
    private Bitmap getpicfromasset(ImageView imageView,String assetName){

        int targetW=imageView.getWidth();
        int targetH=imageView.getHeight();

        if(targetW==0||targetH==0){
            return null;
        }

        try {
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

            return BitmapFactory.decodeStream(inputStream,new Rect(-1,-1,-1,-1),bmOption);


        }catch (IOException e){
            e.printStackTrace();

            return null;
        }

    }


}
