package tsungyu.final_project;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;

public class puzzlePiece extends AppCompatImageView {

    public int xsite;
    public int ysite;
    public int pieceW;
    public int pieceH;
    public boolean canMove=true;


    public puzzlePiece(Context context) {
        super(context);
    }
}
