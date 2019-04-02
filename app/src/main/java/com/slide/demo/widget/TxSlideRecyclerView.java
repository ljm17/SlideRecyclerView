package com.slide.demo.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @author ljm
 * @date 2019/3/25
 * 侧滑菜单栏RecyclerView，模仿QQ聊天列表交互，即触碰已打开菜单栏以外的itemView时，关闭菜单栏并拦截丢弃该次触碰事件
 */
public class TxSlideRecyclerView extends RecyclerView{

    /**最小速度*/
    private static final int MINIMUM_VELOCITY = 500;

    /**滑动的itemView*/
    private ViewGroup mMoveView;

    /**itemView中菜单控件宽度*/
    private int mMenuWidth;

    private VelocityTracker mVelocity;

    /**触碰时的首个横坐标*/
    private int mFirstX;

    /**触碰时的首个纵坐标*/
    private int mFirstY;

    /**触碰末次的横坐标*/
    private int mLastX;

    /**最小滑动距离*/
    private int mTouchSlop;

    private Scroller mScroller;

    /**是否正在水平滑动*/
    private boolean mMoving;

    /**是否由onInterceptTouchEvent（）方法拦截*/
    private boolean mIntercepted;


    public TxSlideRecyclerView(Context context) {
        super(context);
        init();
    }

    public TxSlideRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TxSlideRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(getContext());
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        addVelocityEvent(e);
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                //若Scroller处于动画中，则终止动画
                if (!mScroller.isFinished()){
                    mScroller.abortAnimation();
                }
                mFirstX = x;
                mFirstY = y;
                mLastX = x;
                //获取点击区域所在的itemView
                ViewGroup view = (ViewGroup) findChildViewUnder(x, y);
                //在点击区域以外的itemView开着菜单，则关闭菜单并拦截该次触碰事件
                if (mMoveView != null && view != mMoveView && mMoveView.getScrollX() != 0){
                    closeMenu();
                    mIntercepted = true;
                    return true;
                }
                mMoveView = view;
                //获取itemView中菜单的宽度（规定itemView中为两个子View）
                if (mMoveView != null && mMoveView.getChildCount() == 2){
                    mMenuWidth = mMoveView.getChildAt(1).getWidth();
                }else {
                    mMenuWidth = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocity.computeCurrentVelocity(1000);
                int velocityX = (int) Math.abs(mVelocity.getXVelocity());
                int velocityY = (int) Math.abs(mVelocity.getYVelocity());
                int moveX = Math.abs(x - mFirstX);
                int moveY = Math.abs(y - mFirstY);
                //满足如下条件其一则判定为水平滑动：
                //1、水平速度大于竖直速度,且水平速度大于最小速度
                //2、水平位移大于竖直位移,且大于最小移动距离
                //必需条件：itemView菜单栏宽度大于0，且recyclerView处于静止状态（即并不在竖直滑动）
                boolean isHorizontalMove = (Math.abs(velocityX) >= MINIMUM_VELOCITY && velocityX > velocityY || moveX > moveY
                        && moveX > mTouchSlop) && mMenuWidth > 0 && getScrollState() == 0;
                if (isHorizontalMove){
                    mIntercepted = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                releaseVelocity();
                //itemView以及其子view触发点击事件，菜单未关闭则直接关闭
                closeMenuNow();
                break;
            default:break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        addVelocityEvent(e);
        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                //若是通过onInterceptTouchEvent（）方法ACTION_DOWN拦截而来的，则丢弃此次事件
                if (mIntercepted) {
                    mIntercepted = false;
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mVelocity.computeCurrentVelocity(1000);
                int velocityX = (int) Math.abs(mVelocity.getXVelocity());
                int velocityY = (int) Math.abs(mVelocity.getYVelocity());
                int moveX = Math.abs(x - mFirstX);
                int moveY = Math.abs(y - mFirstY);
                //若为onInterceptTouchEvent()方法拦截而来或者已处于水平滑动状态，则让itemView跟随手指滑动
                //或根据水平滑动条件判断，是否让itemView跟随手指滑动（这里重新判断是避免itemView中不拦截ACTION_DOWN事件，则后续ACTION_MOVE并不会走若为onInterceptTouchEvent（）方法）
                boolean isHorizontalMove = mIntercepted || mMoving || (Math.abs(velocityX) >= MINIMUM_VELOCITY && velocityX > velocityY
                        || moveX > moveY && moveX > mTouchSlop) && mMenuWidth > 0 && getScrollState() == 0;
                if (isHorizontalMove){
                    int dx = mLastX - x;
                    //让itemView在规定区域随手指移动
                    if (mMoveView.getScrollX() + dx >= 0 && mMoveView.getScrollX() + dx <= mMenuWidth) {
                        mMoveView.scrollBy(dx, 0);
                    }
                    mLastX = x;
                    //设置正处于水平滑动状态
                    mMoving = true;
                    mIntercepted = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mMoving) {
                    mMoving = false;
                    mVelocity.computeCurrentVelocity(1000);
                    int scrollX = mMoveView.getScrollX();
                    //若速度大于正方向最小速度，则关闭菜单栏；若速度小于反方向最小速度，则打开菜单栏
                    //若速度没到判断条件，则对菜单显示的宽度进行判断打开/关闭菜单
                    if (mVelocity.getXVelocity() >= MINIMUM_VELOCITY){
                        mScroller.startScroll(scrollX, 0, -scrollX, 0, Math.abs(scrollX));
                    }else if (mVelocity.getXVelocity() < -MINIMUM_VELOCITY){
                        int dx = mMenuWidth - scrollX;
                        mScroller.startScroll(scrollX, 0, dx, 0, Math.abs(dx));
                    } else if (scrollX > mMenuWidth / 2) {
                        int dx = mMenuWidth - scrollX;
                        mScroller.startScroll(scrollX, 0, dx, 0, Math.abs(dx));
                    } else {
                        mScroller.startScroll(scrollX, 0, -scrollX, 0, Math.abs(scrollX));
                    }
                    invalidate();
                }else if (mMoveView != null && mMoveView.getScrollX() != 0){
                    //若不是水平滑动状态，菜单栏开着则关闭
                    closeMenu();
                }
                releaseVelocity();
                break;
            default:break;
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (isInWindow(mMoveView)){
                mMoveView.scrollTo(mScroller.getCurrX(), 0);
                invalidate();
            }else {
                //若处于动画的itemView滑出屏幕，则终止动画，并让其到达结束点位置
                mScroller.abortAnimation();
                mMoveView.scrollTo(mScroller.getFinalX(),0);
            }
        }
    }

    /**
     * 使用Scroller关闭菜单栏
     */
    public void closeMenu(){
        mScroller.startScroll(mMoveView.getScrollX(),0, -mMoveView.getScrollX(), 0 ,300);
        invalidate();
    }

    /**
     * 即刻关闭菜单栏
     */
    public void closeMenuNow(){
        if (mMoveView != null && mMoveView.getScrollX() != 0) {
            mMoveView.scrollTo(0, 0);
        }
    }

    /**
     * 获取VelocityTracker实例，并为其添加事件
     * @param e 触碰事件
     */
    private void addVelocityEvent(MotionEvent e){
        if (mVelocity == null){
            mVelocity = VelocityTracker.obtain();
        }
        mVelocity.addMovement(e);
    }

    /**
     * 释放VelocityTracker
     */
    private void releaseVelocity(){
        if (mVelocity != null){
            mVelocity.clear();
            mVelocity.recycle();
            mVelocity = null;
        }
    }

    /**
     * 判断该itemView是否显示在屏幕内
     * @param view itemView
     * @return isInWindow
     */
    private boolean isInWindow(View view){
        if (getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager manager = (LinearLayoutManager) getLayoutManager();
            int firstPosition = manager.findFirstVisibleItemPosition();
            int lastPosition = manager.findLastVisibleItemPosition();
            int currentPosition = manager.getPosition(view);
            return currentPosition >= firstPosition && currentPosition <= lastPosition;
        }
        return true;
    }

}
