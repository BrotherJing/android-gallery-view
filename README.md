# android-gallery-view
[![](https://jitpack.io/v/BrotherJing/android-gallery-view.svg)](https://jitpack.io/#BrotherJing/android-gallery-view)

a widget for displaying images.

## Usage

In your `build.gradle`:

```
compile 'com.github.BrotherJing:android-gallery-view:v1.0.0'
```

Then create the gallery view in your layout or code.

```
<io.brotherjing.galleryview.GalleryView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/gallery"/>
```

Customize your adapter. For example, an adapter that load image from url.

```java
public class UrlGalleryAdapter extends GalleryAdapter {

    private List<String> data;

    public UrlGalleryAdapter(Context context, List<String> data) {
        super(context);
        this.data = data;
    }

    @Override
    public int getInitPicIndex() {
        return 0;
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
```

Finally,

```java
galleryView.setAdapter(new UrlGalleryAdapter(this,data));
```

![](http://7xrcar.com1.z0.glb.clouddn.com/gallery.gif)