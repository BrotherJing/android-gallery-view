package io.brotherjing.galleryview.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.brotherjing.galleryview.GalleryView;
import io.brotherjing.galleryview.ZoomImageView;

public class MainActivity extends AppCompatActivity {

    private GalleryView galleryView;
    private TextView label;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        galleryView = (GalleryView)findViewById(R.id.gallery);
        label = (TextView)findViewById(R.id.tvLabel);

        galleryView.setScrollEndListener(new GalleryView.OnScrollEndListener() {
            @Override
            public void onScrollEnd(int index) {
                label.setText((index+1)+"/"+galleryView.getAdapter().getCount());
            }
        });
        /*ZoomImageView imageView = (ZoomImageView)findViewById(R.id.imageView);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.koala);
        imageView.setImageBitmap(bitmap);*/
        List<String> data = new ArrayList<>();
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Tulips.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Penguins.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Hydrangeas.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/koala.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Lighthouse.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Jellyfish.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Desert.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Chrysanthemum.jpg");
        data.add("http://7xrcar.com1.z0.glb.clouddn.com/Lighthouse.jpg");
        galleryView.setAdapter(new UrlGallaryAdapter(this,data));
    }
}
