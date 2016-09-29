package io.brotherjing.galleryview.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.brotherjing.galleryview.GalleryView;

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
        galleryView.setAdapter(new UrlGalleryAdapter(this,data));
    }
}
