package io.brotherjing.galleryview.sample;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import io.brotherjing.galleryview.GalleryAdapter;

/**
 * Created by jingyanga on 2016/9/28.
 */

public class UrlGalleryAdapter extends GalleryAdapter {

    private List<String> data;

    public UrlGalleryAdapter(Context context, List<String> data) {
        super(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public void fillViewAtPosition(int position, ImageView imageView) {
        String url = data.get(position);
        Picasso.with(context).cancelRequest(imageView);
        Picasso.with(context).load(url).placeholder(android.R.drawable.ic_menu_gallery).into(imageView);
    }
}
