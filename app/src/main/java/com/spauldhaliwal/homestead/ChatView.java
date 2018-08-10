package com.spauldhaliwal.homestead;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import static android.support.v4.view.ViewCompat.TYPE_TOUCH;

public class ChatView extends RecyclerView {
    private static final String TAG = "ChatView";
    private static final int INVALID_POINTER = -1;


    private int mScrollState = SCROLL_STATE_IDLE;
    private int mScrollPointerId = INVALID_POINTER;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLastTouchX;
    private int mLastTouchY;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];


    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private OnFlingListener mOnFlingListener;

    public ChatView(Context context) {
        super(context);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        final int action = e.getActionMasked();
        final MotionEvent vtev = MotionEvent.obtain(e);
//        final int action = e.getActionMasked();
        final int actionIndex = e.getActionIndex();

        boolean eventAddedToVelocityTracker = false;


        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mScrollPointerId = e.getPointerId(0);
                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
//                if (canScrollHorizontally) {
//                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
//                }
//                if (canScrollVertically) {
//                    nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
//                }
                startNestedScroll(nestedScrollAxis, TYPE_TOUCH);
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                mScrollPointerId = e.getPointerId(actionIndex);
                mInitialTouchX = mLastTouchX = (int) (e.getX(actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY(actionIndex) + 0.5f);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int index = e.findPointerIndex(mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id "
                            + mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }

                final int x = (int) (e.getX(index) + 0.5f);
                final int y = (int) (e.getY(index) + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset, TYPE_TOUCH)) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    // Updated the nested offsets
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];
                }

                if (mScrollState != SCROLL_STATE_DRAGGING) {
                    boolean startScroll = false;
//                    if (canScrollHorizontally && Math.abs(dx) > mTouchSlop) {
                    if (dx > 0) {
                        dx -= mTouchSlop;
                    } else {
                        dx += mTouchSlop;
                    }
                    startScroll = true;
//                    }
//                    if (canScrollVertically && Math.abs(dy) > mTouchSlop) {
                    if (dy > 0) {
                        dy -= mTouchSlop;
                    } else {
                        dy += mTouchSlop;
                    }
                    startScroll = true;
//                    }
//                    if (startScroll) {
//                        setScrollState(SCROLL_STATE_DRAGGING);
//                    }
                }

                if (mScrollState == SCROLL_STATE_DRAGGING) {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];

//                    if (scrollByInternal(
//                            canScrollHorizontally ? dx : 0,
//                            canScrollVertically ? dy : 0,
//                            vtev)) {
//                        getParent().requestDisallowInterceptTouchEvent(true);
//                    }
//                    if (mGapWorker != null && (dx != 0 || dy != 0)) {
//                        mGapWorker.postFromTraversal(this, dx, dy);
//                    }
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
//                onPointerUp(e);
            }
            break;

//            case MotionEvent.ACTION_UP: {
//                mVelocityTracker.addMovement(vtev);
//                eventAddedToVelocityTracker = true;
//                mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
//                final float xvel = canScrollHorizontally
//                        ? -mVelocityTracker.getXVelocity(mScrollPointerId) : 0;
//                final float yvel = canScrollVertically
//                        ? -mVelocityTracker.getYVelocity(mScrollPointerId) : 0;
//                if (!((xvel != 0 || yvel != 0) && fling((int) xvel, (int) yvel))) {
//                    setScrollState(SCROLL_STATE_IDLE);
//                }
//                resetTouch();
//            } break;

            case MotionEvent.ACTION_CANCEL: {
//                cancelTouch();
            }
            break;
        }
        return false;
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {

        super.onCreateContextMenu(menu);
    }
}
