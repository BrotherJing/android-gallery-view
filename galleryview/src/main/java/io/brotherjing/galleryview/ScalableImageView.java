package io.brotherjing.galleryview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

/**
 * Created by jingyanga on 2016/9/28.
 */

public class ScalableImageView extends ImageView {

    private boolean firstTouch = true;
    private boolean inAnimation = false;

    private int touchSlop;

    private int mode;
    private static final int MODE_NONE = 0;
    private static final int MODE_DRAG = 1;
    private static final int MODE_ZOOM = 2;

    private PointF dragStartPoint = new PointF();
    float[] bound;
    private float lastDx;
    private float lastDy;

    private Matrix matrix = new Matrix();
    private Matrix currentMatrix = new Matrix();
    private PointF midPoint = new PointF();
    private float startDistance = 1f;

    private float totalScale = 1f;
    private float currentTotalScale = 1f;

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
    public boolean onTouchEvent(MotionEvent event) {
        if(inAnimation)return false;
        switch (event.getAction()&MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                mode = MODE_DRAG;
                bound = getImageBound();
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
                        if(currentTotalScale* scale >4f){
                            scale = 4f/currentTotalScale;
                            totalScale = 4f;
                        }else if(currentTotalScale* scale <1f){
                            scale = 1f/currentTotalScale;
                            totalScale = 1f;
                        }else{
                            totalScale = currentTotalScale* scale;
                        }
                        matrix.set(currentMatrix);
                        matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                    }
                }else if(mode==MODE_DRAG){
                    getParent().requestDisallowInterceptTouchEvent(true);
                    float dx = event.getX() - dragStartPoint.x;
                    float dy = event.getY() - dragStartPoint.y;
                    boolean canScroll[] = getCanScroll();
                    boolean shouldCancelX=false,shouldCancelY=false;
                    if(dx >0&&!canScroll[0]|| dx <0&&!canScroll[2])shouldCancelX = true;
                    if(dy >0&&!canScroll[1]|| dy <0&&!canScroll[3])shouldCancelY = true;
                    if(shouldCancelX){
                        dx = lastDx;
                    }
                    if(shouldCancelY){
                        dy = lastDy;
                    }
                    matrix.set(currentMatrix);
                    //matrix.postTranslate(shouldCancelX?(lastDx-dx):0,shouldCancelY?(lastDy-dy):0);
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
                currentTotalScale = totalScale;
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

    private void animateBackToCenter(){
        ValueAnimator animator = ValueAnimator.ofFloat(0f,1f);
        float bound[] = getImageBound();
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

    public float[] getImageBound(){
        float[] bound = new float[4];
        bound[0]=getDrawable().getBounds().left;
        bound[1]=getDrawable().getBounds().top;
        bound[2]=getDrawable().getBounds().right;
        bound[3]=getDrawable().getBounds().bottom;
        matrix.mapPoints(bound);
        return bound;
    }

    public boolean[] getCanScroll(){
        boolean[] canScroll = new boolean[4];
        float[] imageBound = getImageBound();
        canScroll[0] = imageBound[0]<-touchSlop;
        canScroll[1] = imageBound[1]<-touchSlop;
        canScroll[2] = imageBound[2]>getWidth()+touchSlop;
        canScroll[3] = imageBound[3]>getHeight()+touchSlop;
        return canScroll;
    }

    public float getImageLeftBound(){
        float[] leftTop = new float[2];
        leftTop[0]=getDrawable().getBounds().left;
        leftTop[1]=getDrawable().getBounds().top;
        matrix.mapPoints(leftTop);
        return leftTop[0];
    }

    public float getImageRightBound(){
        float[] rightTop = new float[2];
        rightTop[0]=getDrawable().getBounds().right;
        rightTop[1]=getDrawable().getBounds().top;
        matrix.mapPoints(rightTop);
        return rightTop[0];
    }

    public float getImageTopBound(){
        float[] rightTop = new float[2];
        rightTop[0]=getDrawable().getBounds().right;
        rightTop[1]=getDrawable().getBounds().top;
        matrix.mapPoints(rightTop);
        return rightTop[1];
    }

    public float getImageBottomBound(){
        float[] leftBottom = new float[2];
        leftBottom[0]=getDrawable().getBounds().left;
        leftBottom[1]=getDrawable().getBounds().bottom;
        matrix.mapPoints(leftBottom);
        return leftBottom[1];
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
