package io.brotherjing.galleryview;

import android.content.Context;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by jingyanga on 2016/9/28.
 */

public abstract class GalleryAdapter {

    protected Context context;

    public GalleryAdapter(Context context){
        this.context = context;
    }

    public abstract int getInitPicIndex();

    public abstract int getCount();

    public abstract void fillViewAtPosition(int position, ImageView imageView);

}
