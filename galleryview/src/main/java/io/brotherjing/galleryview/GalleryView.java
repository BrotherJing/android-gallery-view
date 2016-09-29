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

    private int lastX;
    private int lastInterceptX, lastInterceptY;
    private int lastScrollX;

    /**
     * Scrolling by flinging, or dragging.
     * When a scrolling ends, we only consider flinging as scrolling to a new page.
     */
    private boolean flinging = false;

    /**
     * There are only 3 ImageViews in used, previous, current and next.
     * When scrolling to next, the previous page is removed and appends to
     * the last page, vice versa.
     * Use this padding to adjust their position after scrolling to a new page.
     */
    private int customPaddingLeft=0;

    /**
     * The value can be -1, 0, 1, which represents the previous, current and
     * next frame.
     */
    private int frameIndex =0;

    /**
     * current picture index, ranged from 0 to picture length
     */
    private int picIndex=0;

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

    private void init(){
        frames = new ScalableImageView[3];
        frames[0] = new ScalableImageView(getContext());
        frames[1] = new ScalableImageView(getContext());
        frames[2] = new ScalableImageView(getContext());
        layoutParams = new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        for (ScalableImageView frame : frames) {
            frame.resetImageState();
            addView(frame, layoutParams);
        }

        mScroller = new Scroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();
    }

    private void updateFramesVisibility(){
        for(int i=0;i<frames.length;++i){
            if(picIndex+i-1<0||picIndex+i-1>=getPicCount())frames[i].setVisibility(INVISIBLE);
            else frames[i].setVisibility(VISIBLE);
        }
    }

    private void onScrollEnd(){
        lastScrollX = getScrollX();
        if(scrollEndListener!=null)scrollEndListener.onScrollEnd(picIndex);
        if(frameIndex==0)return;

        frames[1].resetImageState();
        customPaddingLeft = customPaddingLeft+frameIndex*getWidth();
        if(frameIndex ==-1){//scroll to left
            removeView(frames[2]);
            addView(frames[2],0,layoutParams);
            fillFrame(picIndex-1, frames[2]);
        }else if(frameIndex ==1){//scroll to right
            removeView(frames[0]);
            addView(frames[0],layoutParams);
            fillFrame(picIndex+1, frames[0]);
        }
        shiftFrames(frameIndex);
        updateFramesVisibility();
        frameIndex =0;
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
        boolean canHandle = true;
        boolean childCanScroll[] = child.getCanScroll();
        if(Math.abs(dx)>Math.abs(dy)) {//left-right scroll
            if (dx > 0) canHandle = childCanScroll[0];
            else canHandle = childCanScroll[2];
        }
        return canHandle;
    }

    private int getPicCount(){
        if(adapter!=null){
            return adapter.getCount();
        }
        return 0;
    }

    public void setAdapter(GalleryAdapter adapter) {
        this.adapter = adapter;
        this.picIndex = adapter.getInitPicIndex();
        fillInitialFrames();
        updateFramesVisibility();
    }

    public GalleryAdapter getAdapter() {
        return adapter;
    }

    private void fillInitialFrames(){
        if(adapter==null)return;
        for(int i=0;i<frames.length;++i){
            fillFrame(picIndex+i-1, frames[i]);
        }
    }

    private void fillFrame(int position, ScalableImageView imageView){
        if(position<0||position>=getPicCount())return;
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
        }
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
                int deltaScrollX = getScrollX()-lastScrollX;
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                if(Math.abs(velocityX)>50){
                    if(velocityX*deltaScrollX<0)//same direction
                        frameIndex = (velocityX>0)? frameIndex -1: frameIndex +1;
                    else
                        frameIndex = 0;
                }else{
                    frameIndex = (int)Math.floor((deltaScrollX+ getWidth() /2)*1.0/ getWidth());
                }
                frameIndex = Math.max(-1, Math.min(frameIndex, 1));
                if(picIndex+ frameIndex <0||picIndex+ frameIndex >=getPicCount()) frameIndex =0;
                picIndex+= frameIndex;
                int dx = frameIndex * getWidth() -deltaScrollX;
                smoothScrollBy(dx,0);
                mVelocityTracker.clear();
                break;
            default:break;
        }
        lastX = x;
        return true;
    }

    private void smoothScrollBy(int dx, int dy){
        flinging = true;
        mScroller.startScroll(getScrollX(), 0, dx, dy, 300);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }else{//if mScroller finished or aborted, or the view is scrolling without the scroller
            if(flinging) {
                scrollTo(mScroller.getFinalX(), mScroller.getFinalY());//in case the scrolling is aborted, scroll to final position
                onScrollEnd();
            }
            flinging = false;
        }
    }

    public void setScrollEndListener(OnScrollEndListener scrollEndListener) {
        this.scrollEndListener = scrollEndListener;
    }

    public interface OnScrollEndListener{
        void onScrollEnd(int index);
    }

}
