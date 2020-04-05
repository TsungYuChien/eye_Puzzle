package tsungyu.final_project;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class TouchListen implements View.OnTouchListener {

    private float xDelta;
    private float yDelta;

    private ImageActivity imgAct;

    public TouchListen(ImageActivity imgAct){
        this.imgAct=imgAct;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        float x=event.getRawX();
        float y=event.getRawY();
        final double tolerance=sqrt(pow(v.getWidth(),2)+pow(v.getHeight(),2)/10);

        puzzlePiece piece=(puzzlePiece) v;
        if(!piece.canMove){
            return true;
        }

        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams) v.getLayoutParams();
        switch (event.getAction() & MotionEvent.ACTION_MASK){

            case MotionEvent.ACTION_DOWN:
                xDelta=x-params.leftMargin;
                yDelta=y-params.topMargin;
                piece.bringToFront();           //搬到最上層
                break;
            case MotionEvent.ACTION_MOVE:
                params.leftMargin=(int)(x-xDelta);
                params.topMargin=(int)(y-yDelta);
                v.setLayoutParams(params);
                break;
            case MotionEvent.ACTION_UP:
                int xDif=abs(piece.xsite-params.leftMargin);
                int yDif=abs(piece.ysite-params.topMargin);

                if(xDif<=tolerance && yDif<=tolerance){
                    params.leftMargin=piece.xsite;
                    params.topMargin=piece.ysite;
                    piece.setLayoutParams(params);
                    piece.canMove=false;
                    sendviewBack(piece);
                    imgAct.checkGameover();
                }
                break;

        }

        return true;
    }

    public void sendviewBack(final View child){
        final ViewGroup parent=(ViewGroup)child.getParent();

        if(null!=parent){
            parent.removeView(child);
            parent.addView(child,0);
        }
    }


}
