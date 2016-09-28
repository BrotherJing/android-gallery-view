package io.brotherjing.galleryview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by jingyanga on 2016/9/28.
 */

public class ScalableImageView extends ImageView {

    private int mode;
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private PointF dragStartPoint = new PointF();

    private Matrix matrix = new Matrix();
    private Matrix currentMatrix = new Matrix();
    private PointF midPoint = new PointF();
    private float startDistance = 1f;

    private float scale;
    private float totalScale = 1f;
    private float currentTotalScale = 1f;

    public ScalableImageView(Context context) {
        super(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void resetImageState(){
        totalScale = 1f;
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                currentTotalScale = totalScale;
                dragStartPoint.set(event.getX(), event.getY());
                currentMatrix.set(getImageMatrix());
                break;
            case MotionEvent.ACTION_MOVE:
                setScaleType(ImageView.ScaleType.MATRIX);
                if(mode==MODE_ZOOM){
                    float distance = distance(event);
                    if(distance>10f){
                        scale = distance/ startDistance;
                        if(currentTotalScale*scale>4f){
                            scale = 4f/currentTotalScale;
                            totalScale = 4f;
                        }else if(currentTotalScale*scale<1f){
                            scale = 1f/currentTotalScale;
                            totalScale = 1f;
                        }else{
                            totalScale = currentTotalScale*scale;
                        }
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                }else if(mode==MODE_DRAG){
                    float dx = event.getX()-dragStartPoint.x;
                    float dy = event.getY()-dragStartPoint.y;
                    if(Math.abs(dy)>Math.abs(dx)){
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    matrix.set(currentMatrix);
                    matrix.postTranslate(dx, dy);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE_NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                startDistance = distance(event);
                if(startDistance >10f){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    mode = MODE_ZOOM;
                    midPoint = mid(event);
                    currentMatrix.set(getImageMatrix());
                }
                break;
        }
        setImageMatrix(matrix);
        float[] leftTop = new float[2];
        leftTop[0]=getDrawable().getBounds().left;
        leftTop[1]=getDrawable().getBounds().top;
        Log.i("matrix","==================================");
        Log.i("matrix","leftTop before:"+leftTop[0]+","+leftTop[1]);
        matrix.mapPoints(leftTop);
        Log.i("matrix","leftTop after:"+leftTop[0]+","+leftTop[1]);
        Log.i("matrix","==================================");
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


}
