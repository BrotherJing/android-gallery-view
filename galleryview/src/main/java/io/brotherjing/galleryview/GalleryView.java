package io.brotherjing.galleryview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    private int picIndex=0;

    /**
     * initial picture index, constant, any number between 0 and picture length-1
     */
    private final int initPicIndex=0;

    private int customPaddingLeft=0;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private ScalableImageView[] frames;
    private LayoutParams layoutParams;

    private OnScrollEndListener scrollEndListener;

    private GalleryAdapter adapter;

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
        frames = new ScalableImageView[3];
        frames[0] = new ScalableImageView(getContext());//frames[0].setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
        frames[1] = new ScalableImageView(getContext());//frames[1].setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        frames[2] = new ScalableImageView(getContext());//frames[2].setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));
        layoutParams = new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        /*frames[0].setImageResource(R.drawable.koala);
        frames[1].setImageResource(R.drawable.hydrangeas);
        frames[2].setImageResource(R.drawable.penguins);*/
        for (ScalableImageView frame : frames) {
            frame.resetImageState();
            addView(frame, layoutParams);
        }

        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void updateFramesVisibility(){
        for(int i=0;i<frames.length;++i){
            if(picIndex+i-1<0||picIndex+i-1>=adapter.getCount())frames[i].setVisibility(INVISIBLE);
            else frames[i].setVisibility(VISIBLE);
        }
    }

    private void onScrollEnd(){
        lastScrollX = getScrollX();
        if(scrollEndListener!=null)scrollEndListener.onScrollEnd(picIndex);
        if(childIndex==-1){//scroll to left
            frames[1].resetImageState();
            removeView(frames[2]);
            customPaddingLeft = customPaddingLeft-getWidth();
            addView(frames[2],0,layoutParams);
            fillFrame(picIndex-1, frames[2]);
            shiftFrames(-1);
            updateFramesVisibility();
        }else if(childIndex==1){//scroll to right
            frames[1].resetImageState();
            removeView(frames[0]);
            customPaddingLeft = customPaddingLeft+getWidth();
            addView(frames[0],layoutParams);
            fillFrame(picIndex+1, frames[0]);
            shiftFrames(1);
            updateFramesVisibility();
        }
        /*Log.i("gallery","=====================");
        Log.i("gallery","padding left "+getPaddingLeft());
        Log.i("gallery","scrollX"+getScrollX());
        Log.i("gallery","=====================");
        logFramesStatus();*/
        childIndex=0;
    }

    private void logFramesStatus(){
        for(int i=0;i<frames.length;++i){
            Log.i("gallery", "frame "+i+": x="+frames[i].getX()+" visible="+(frames[i].getVisibility()==VISIBLE));
        }
    }

    private void shiftFrames(int direction){
        ScalableImageView temp;
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

    private boolean canChildHandle(ScalableImageView child, int dx, int dy){
        /*float point[] = new float[2];
        point[0] = event.getX();
        point[1] = event.getY();
        point[0] += getScrollX() - child.getLeft();
        point[1] += getScrollY() - child.getTop();
//        Log.i("point",point[0]+","+point[1]);
//        Log.i("point",child.getLeft()+","+child.getRight()+","+child.getTop()+","+child.getBottom());
        return point[0]>0&&point[0]<child.getRight()-child.getLeft()&&
                point[1]>0&&point[1]<child.getBottom()-child.getTop();*/
        boolean canHandle;
        boolean childCanScroll[] = child.getCanScroll();
        if(Math.abs(dx)>Math.abs(dy)) {//left-right scroll
            if (dx > 0) canHandle = childCanScroll[0];
            else canHandle = childCanScroll[2];
        }else {
            if (dy > 0) canHandle = childCanScroll[1];
            else canHandle = childCanScroll[3];
        }
        return canHandle;
    }

    public void setAdapter(GalleryAdapter adapter) {
        this.adapter = adapter;
        updateFramesVisibility();
        fillInitialFrames();
    }

    public GalleryAdapter getAdapter() {
        return adapter;
    }

    private void fillInitialFrames(){
        for(int i=0;i<frames.length;++i){
            if(picIndex+i-1<0||picIndex+i-1>=adapter.getCount())continue;
            else {
                adapter.fillViewAtPosition(picIndex+i-1, frames[i]);
            }
        }
    }

    private void fillFrame(int position, ScalableImageView imageView){
        if(position<0||position>=adapter.getCount())return;
        adapter.fillViewAtPosition(position, imageView);
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
                int dx = x - lastInterceptX;
                int dy = y - lastInterceptY;
                intercept = !canChildHandle(frames[1], dx, dy);
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
                    if(xVelo*scrollX<0)
                        childIndex = (xVelo>0)?childIndex-1:childIndex+1;
                    else
                        childIndex = 0;
                }else{
                    childIndex = (int)Math.floor((scrollX+ getWidth() /2)*1.0/ getWidth());
                }
                childIndex = Math.max(-1, Math.min(childIndex, 1));
                if(picIndex+childIndex<0||picIndex+childIndex>=adapter.getCount())childIndex=0;
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
