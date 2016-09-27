package io.brotherjing.galleryview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by jingyanga on 2016/9/27.
 */

public class GalleryView extends ViewGroup {

    /**
     * last touch x
     */
    private int lastX;
    private int lastInterceptX, lastInterceptY;
    private int lastScrollX;
    private boolean smoothScrolling = false;

    private int childIndex=0;

    /**
     * current picture index, ranged from 0 to picture length
     */
    private int picIndex=2;

    /**
     * initial picture index, constant, any number between 0 and picture length-1
     */
    private final int initPicIndex=2;

    /**
     * number of pictures
     */
    private final int picLength=10;

    private int customPaddingLeft=0;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private ImageFrameView[] frames;
    private LayoutParams layoutParams;

    private OnScrollEndListener scrollEndListener;

    public GalleryView(Context context) {
        super(context);
        init();
    }

    public GalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GalleryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        frames = new ImageFrameView[3];
        frames[0] = new ImageFrameView(getContext());frames[0].setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        frames[1] = new ImageFrameView(getContext());frames[1].setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        frames[2] = new ImageFrameView(getContext());frames[2].setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        layoutParams = new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        for (ImageFrameView frame : frames) {
            addView(frame, layoutParams);
        }
        updateFramesVisibility();

        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void updateFramesVisibility(){
        for(int i=0;i<frames.length;++i){
            if(picIndex+i-1<0||picIndex+i-1>=picLength)frames[i].setVisibility(INVISIBLE);
            else frames[i].setVisibility(VISIBLE);
        }
    }

    private void onScrollEnd(){
        lastScrollX = getScrollX();
        if(scrollEndListener!=null)scrollEndListener.onScrollEnd(picIndex);
        if(childIndex==-1){
            removeView(frames[2]);
            customPaddingLeft = customPaddingLeft-getWidth();
            addView(frames[2],0,layoutParams);
            shiftFrames(-1);
            updateFramesVisibility();
        }else if(childIndex==1){
            removeView(frames[0]);
            customPaddingLeft = customPaddingLeft+getWidth();
            addView(frames[0],layoutParams);
            shiftFrames(1);
            updateFramesVisibility();
        }
        Log.i("gallery","=====================");
        Log.i("gallery","padding left "+getPaddingLeft());
        Log.i("gallery","scrollX"+getScrollX());
        Log.i("gallery","=====================");
        logFramesStatus();
        childIndex=0;
    }

    private void logFramesStatus(){
        for(int i=0;i<frames.length;++i){
            Log.i("gallery", "frame "+i+": x="+frames[i].getX()+" visible="+(frames[i].getVisibility()==VISIBLE));
        }
    }

    private void shiftFrames(int direction){
        ImageFrameView temp;
        if(direction==-1){
            temp = frames[2];
            frames[2] = frames[1];
            frames[1] = frames[0];
            frames[0] = temp;
        }else if(direction==1){
            temp = frames[0];
            frames[0] = frames[1];
            frames[1] = frames[2];
            frames[2] = temp;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int mLeft = customPaddingLeft-getWidth();
        int childCount = getChildCount();
        int childWidth;
        int childHeight;
        MarginLayoutParams cParams;
        for(int j=0;j<childCount;++j){
            View child = getChildAt(j);
            childWidth = child.getMeasuredWidth();
            childHeight = child.getMeasuredHeight();
            cParams = (MarginLayoutParams) child.getLayoutParams();
            int left = mLeft + cParams.leftMargin;
            int top = cParams.topMargin;
            int right = left + childWidth;
            int bottom = childHeight + top;
            child.layout(left, top, right, bottom);
            mLeft += childWidth + cParams.leftMargin + cParams.rightMargin;
            Log.i("gallery","mLeft:"+mLeft+" childWidth="+childWidth);
        }
        logFramesStatus();
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        int x = (int)ev.getX();
        int y = (int)ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                intercept = false;
                if(!mScroller.isFinished()){
                    mScroller.abortAnimation();
                    intercept = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                intercept = Math.abs(x - lastInterceptX) > Math.abs(y - lastInterceptY);
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
            default:
                break;
        }
        lastInterceptX = x;
        lastInterceptY = y;
        lastX = x;
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        mVelocityTracker.addMovement(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!mScroller.isFinished())return false;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = x - lastX;
                scrollBy(-deltaX, 0);
                break;
            case MotionEvent.ACTION_UP:
                int scrollX = getScrollX()-lastScrollX;
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelo = mVelocityTracker.getXVelocity();
                if(Math.abs(xVelo)>50){
                    childIndex = (xVelo>0)?childIndex-1:childIndex+1;
                }else{
                    childIndex = (int)Math.floor((scrollX+ getWidth() /2)*1.0/ getWidth());
                }
                childIndex = Math.max(-1, Math.min(childIndex, 1));
                if(picIndex+childIndex<0||picIndex+childIndex>=picLength)childIndex=0;
                picIndex+=childIndex;
                //Log.i("gallery", childIndex+" "+scrollX+" "+getWidth());
                int dx = childIndex* getWidth() -scrollX;
                smoothScrollBy(dx,0);
                mVelocityTracker.clear();
                break;
            default:break;
        }
        lastX = x;
        return true;
    }

    private void smoothScrollBy(int dx, int dy){
        smoothScrolling = true;
        mScroller.startScroll(getScrollX(), 0, dx, dy, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }else{
            if(smoothScrolling) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                onScrollEnd();
            }
            smoothScrolling = false;
        }
    }

    public void setScrollEndListener(OnScrollEndListener scrollEndListener) {
        this.scrollEndListener = scrollEndListener;
    }

    public interface OnScrollEndListener{
        void onScrollEnd(int index);
    }

}
