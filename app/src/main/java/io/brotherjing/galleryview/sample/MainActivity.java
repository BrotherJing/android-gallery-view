package io.brotherjing.galleryview.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

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
                Log.i("gallery",index+"");
                label.setText(index+"");
            }
        });
    }
}