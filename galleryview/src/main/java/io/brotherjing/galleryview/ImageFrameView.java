package io.brotherjing.galleryview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by jingyanga on 2016/9/27.
 */

public class ImageFrameView extends FrameLayout {

    private int mode;
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private Matrix matrix = new Matrix();
    private Matrix currentMatrix = new Matrix();
    private PointF midPoint = new PointF();
    private float startDistance = 1f;

    private ImageView imageView;

    public ImageFrameView(Context context) {
        super(context);
        init();
    }

    public ImageFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        imageView = new ImageView(getContext());
        imageView.setImageResource(android.R.drawable.ic_dialog_email);
        //imageView.setScaleType(ImageView.ScaleType.CENTER);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(imageView, layoutParams);
    }

    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        for(int i=0;i<childCount;++i){
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int left = (width-childWidth)/2;
            int top = (height-childHeight)/2;
            int right = left+childWidth;
            int bottom = top+childHeight;
            child.layout(left, top, right, bottom);
        }
    }*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = MODE_NONE;
                currentMatrix.set(imageView.getImageMatrix());
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode==MODE_ZOOM){
                    imageView.setScaleType(ImageView.ScaleType.MATRIX);
                    float distance = distance(event);
                    if(distance>10f){
                        float scale = distance/ startDistance;
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        //Log.i("gallery", "matrix: "+matrix);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                startDistance = distance(event);
                if(startDistance >10f){
//                    Log.i("gallery", "start distance: "+startDistance);
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mode = MODE_ZOOM;
                    midPoint = mid(event);
                    currentMatrix.set(imageView.getImageMatrix());
                }
                break;
        }
        imageView.setImageMatrix(matrix);
        return true;
    }

    private float distance(MotionEvent event){
        float dx = event.getX(1)-event.getX(0);
        float dy = event.getY(1)-event.getY(0);
        return (float)Math.sqrt(dx*dx+dy*dy);
    }

    private PointF mid(MotionEvent event){
        float midX = (event.getX(1)+event.getX(0))/2;
        float midY = (event.getY(1)+event.getY(0))/2;
        return new PointF(midX, midY);
    }

    public ImageView getImageView() {
        return imageView;
    }
}
