package io.brotherjing.galleryview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * Created by jingyanga on 2016/9/28.
 */

public class ScalableImageView extends ImageView {

    /**
     * The initial scaleType is CENTER_INSIDE, but we want to manipulate the image with MATRIX.
     * On first touch, we set scaleType to MATRIX, and retrieve the original image matrix.
     */
    private boolean firstTouch = true;

    /**
     * Disable touch event when in animation.
     */
    private boolean inAnimation = false;

    private int touchSlop;

    private int mode;
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private PointF dragStartPoint = new PointF();
    private float lastDx;
    private float lastDy;

    private Matrix matrix = new Matrix();
    private Matrix currentMatrix = new Matrix();
    private PointF midPoint = new PointF();
    private float startDistance = 1f;

    private float totalScale = 1f;
    private float lastTotalScale = 1f;

    public ScalableImageView(Context context) {
        super(context);
        init();
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public void resetImageState(){
        totalScale = 1f;
        setScaleType(ScaleType.CENTER_INSIDE);
        firstTouch = true;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        resetImageState();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(inAnimation)return false;
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                lastDx = 0;
                lastDy = 0;
                dragStartPoint.set(event.getX(), event.getY());
                currentMatrix.set(getImageMatrix());
                if(firstTouch){
                    setScaleType(ScaleType.MATRIX);
                    matrix.set(getImageMatrix());
                    firstTouch = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mode==MODE_ZOOM){
                    float distance = distance(event);
                    if(distance>10f){
                        float scale = distance / startDistance;
                        if(lastTotalScale * scale >4f){
                            scale = 4f/ lastTotalScale;
                            totalScale = 4f;
                        }else if(lastTotalScale * scale <1f){
                            scale = 1f/ lastTotalScale;
                            totalScale = 1f;
                        }else{
                            totalScale = lastTotalScale * scale;
                        }
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                }else if(mode==MODE_DRAG){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float dx = event.getX() - dragStartPoint.x;
                    float dy = event.getY() - dragStartPoint.y;
                    boolean canScroll[] = getCanScroll();
                    boolean shouldCancelX=false, shouldCancelY=false;
                    if(dx-lastDx >0&&!canScroll[0]|| dx-lastDx <0&&!canScroll[2])shouldCancelX = true;
                    if(dy-lastDy >0&&!canScroll[1]|| dy-lastDy <0&&!canScroll[3])shouldCancelY = true;
                    if(shouldCancelX){
                        dragStartPoint.x = event.getX() - lastDx;
                        dx = lastDx;
                    }
                    if(shouldCancelY){
                        dragStartPoint.y = event.getY() - lastDy;
                        dy = lastDy;
                    }
                    matrix.set(currentMatrix);
                    matrix.postTranslate(dx, dy);
                    lastDx = dx;
                    lastDy = dy;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if(mode==MODE_ZOOM){
                    if(Math.abs(totalScale-1f)<1e-3)animateBackToCenter();
                }
                mode = MODE_NONE;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                lastTotalScale = totalScale;
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
        return true;
    }

    /**
     * When the image zooms to 1x, it may not be positioned at the center, animate it back.
     */
    private void animateBackToCenter(){
        ValueAnimator animator = ValueAnimator.ofFloat(0f,1f);
        float bound[] = getImageBounds();
        final float dx = getWidth()/2-(bound[0]+bound[2])/2;
        final float dy = getHeight()/2-(bound[1]+bound[3])/2;
        currentMatrix.set(getImageMatrix());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                matrix.set(currentMatrix);
                matrix.postTranslate(dx*fraction, dy*fraction);
                setImageMatrix(matrix);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                inAnimation = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                inAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                inAnimation = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(200).start();
    }

    /**
     * Get the actual bounds of the image, not the view, after mapping the matrix on it.
     * @return the left, top, right, bottom bounds of the image.
     */
    public float[] getImageBounds(){
        float[] bound = new float[4];
        bound[0]=getDrawable().getBounds().left;
        bound[1]=getDrawable().getBounds().top;
        bound[2]=getDrawable().getBounds().right;
        bound[3]=getDrawable().getBounds().bottom;
        matrix.mapPoints(bound);
        return bound;
    }

    /**
     * Check if the image has reached its bounds.
     * @return whether the image has reached its left, top, right, bottom bounds.
     */
    public boolean[] getCanScroll(){
        boolean[] canScroll = new boolean[4];
        float[] imageBound = getImageBounds();
        canScroll[0] = imageBound[0]<-touchSlop;
        canScroll[1] = imageBound[1]<-touchSlop;
        canScroll[2] = imageBound[2]>getWidth()+touchSlop;
        canScroll[3] = imageBound[3]>getHeight()+touchSlop;
        return canScroll;
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
