package io.github.xiewinson.smoothrefresh.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.listener.OnContentViewScrollListener;
import io.github.xiewinson.smoothrefresh.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefresh.library.wrapper.content.ContentViewWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.content.IContentViewWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.content.ListWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.IRefreshHeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.RefreshHeaderWrapper;

/**
 * Created by winson on 2017/10/3.
 */

public class SmoothRefreshLayout extends FrameLayout implements NestedScrollingParent {

    public SmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private IContentViewWrapper contentWrapper;
    private IRefreshHeaderWrapper refreshHeaderWrapper;
    private View refreshHeaderView;
    private View contentView;
    private int refreshHeaderHeight;
    private int refreshingContentTop;
    private int minRefreshContentTop;
    private int maxRefreshContentTop;
    private boolean enterPullRefreshHeader;

    private int currentRefreshHeaderTop = -1;
    private int currentContentTop = -1;

    private int correctOverScrollMode;
    private float lastRefreshHeaderY = -1;
    private int currentRefreshState = RefreshHeaderState.NONE;

    //是否处于刷新
    private boolean refreshing;
    //是否正在执行动画
    private boolean animatorRunning = false;

    private ValueAnimator refreshHeaderAnimator;

    private OnRefreshListener onRefreshListener;

    public static final int DEFAULT_ANIMATOR_DURATION = 300;

    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    private void init() {

        contentView = getChildAt(0);
        if (contentView == null) {
            throw new NullPointerException("you must put a contentView");
        }
        contentWrapper = ContentViewWrapper.Factory.getInstance(contentView);
        if (contentWrapper instanceof ListWrapper) {
            ((ListWrapper) contentWrapper).setContentViewScrollListener(new OnContentViewScrollListener() {
                @Override
                public void onFirstItemScroll(int firstItemY) {
                    if (refreshHeaderView != null) {
                        layoutRefreshHeaderView(firstItemY - refreshHeaderHeight);
                    }
                }
            });
        }
    }

    public void setRefreshHeader(RefreshHeaderWrapper headerWrapper) {
        this.refreshHeaderWrapper = headerWrapper;
        this.refreshHeaderView = headerWrapper.getRefreshHeaderView();
        initRefreshHeaderView();
    }

    private void initRefreshHeaderView() {
        refreshHeaderView.setVisibility(INVISIBLE);
        addView(refreshHeaderView, 0, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        post(new Runnable() {
            @Override
            public void run() {
                initRefreshHeaderParams();
            }
        });
    }

    private void initRefreshHeaderParams() {
        if (refreshHeaderView == null) {
            throw new IllegalArgumentException("please use setRefreshHeader before initRefreshHeaderParams");
        }
        refreshHeaderHeight = refreshHeaderView.getMeasuredHeight();
        minRefreshContentTop = contentWrapper.getTopOffset();
        refreshingContentTop = contentWrapper.getTopOffset() + refreshHeaderHeight;
        maxRefreshContentTop = refreshingContentTop + refreshHeaderHeight * 3;
        layoutRefreshHeaderView(minRefreshContentTop - refreshHeaderHeight);

    }

    private boolean isRefreshHeaderVisible() {
        return refreshHeaderView.getTop() > minRefreshContentTop - refreshHeaderHeight;
    }

    private boolean isRefreshHeaderPartVisible() {
        int top = refreshHeaderView.getTop();
        return top > minRefreshContentTop - refreshHeaderHeight
                && top <= refreshingContentTop - refreshHeaderHeight;
    }

    private boolean isRefreshHeaderFullVisible() {
        int top = refreshHeaderView.getTop();
        return top > refreshingContentTop - refreshHeaderHeight
                && top <= maxRefreshContentTop - refreshHeaderHeight;
    }

    private boolean isRefreshHeaderInVisible() {
        return refreshHeaderView.getTop() <= minRefreshContentTop - refreshHeaderHeight;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (currentRefreshHeaderTop != -1) {
            layoutRefreshHeaderView(currentRefreshHeaderTop);
        }
        if (!(contentWrapper instanceof ListWrapper)) {
            contentWrapper.layout(currentContentTop);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (contentWrapper.isSupportNestedScroll()) {
            return super.dispatchTouchEvent(ev);
        }

        int action = ev.getAction();
        float currentY = ev.getY();

        if (!isEnabled() || refreshing || animatorRunning) {
            return super.dispatchTouchEvent(ev);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastRefreshHeaderY = currentY;
                correctOverScrollMode = contentView.getOverScrollMode();
//                initRefreshHeaderParams();
                break;


            case MotionEvent.ACTION_MOVE:
                float dy = currentY - lastRefreshHeaderY;
                //下拉
                if (dy > 0) {
                    if (enterPullRefreshHeader || (!canChildScrollUp()
                            && refreshHeaderView.getTop() != maxRefreshContentTop
                            && contentWrapper.getTopOffset() != maxRefreshContentTop + refreshHeaderHeight)) {
                        contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                        handleTouchActionMove(dy);
                    }
                }
                //上滑
                else if (enterPullRefreshHeader && dy < 0) {
                    if (contentWrapper.getTopOffset() != refreshingContentTop
                            || refreshHeaderView.getTop() != minRefreshContentTop) {
                        handleTouchActionMove(dy);

                    }
                }
                lastRefreshHeaderY = currentY;
                lastRefreshHeaderY = currentY;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (handleTouchActionUp()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                contentView.setOverScrollMode(correctOverScrollMode);
                lastRefreshHeaderY = -1;
                enterPullRefreshHeader = false;

                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (animatorRunning) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean canChildScrollUp() {
        return contentView != null && contentView.canScrollVertically(-1);
    }

    private void handleTouchActionMove(float dy) {
        enterPullRefreshHeader = true;
        if (dy > 0) {
            dy *= 0.3f;
        }
        if (dy > 0 && dy < 1) {
            dy = 1;
        } else if (dy > -1 && dy < 0) {
            dy = -1;
        }
        int result = (int) (dy + contentWrapper.getTopOffset());
        if (result <= minRefreshContentTop) {
            refreshHeaderView.setVisibility(INVISIBLE);
            result = minRefreshContentTop;
        } else {
            refreshHeaderView.setVisibility(VISIBLE);
        }

        result = Math.min(result, maxRefreshContentTop);

        if (result >= refreshingContentTop) {
            setState(RefreshHeaderState.RELEASE_TO_REFRESH);
        } else {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
        }

        layoutContentView(result);
        layoutRefreshHeaderView(result - refreshHeaderHeight);

    }

    /**
     * @return 若返回true，则将变为刷新状态
     */
    private boolean handleTouchActionUp() {
        if (isRefreshHeaderFullVisible() || currentRefreshState == RefreshHeaderState.RELEASE_TO_REFRESH) {
            expandRefreshHeader(true);
            return true;
        } else if (isRefreshHeaderPartVisible()) {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
            collapseRefreshHeader();
        } else if (isRefreshHeaderInVisible()) {
            onCollapseAnimatorEnd();
        }
        return false;
    }

    private void layoutRefreshHeaderView(int top) {
        this.currentRefreshHeaderTop = top;
        refreshHeaderView.layout(refreshHeaderView.getLeft(),
                top,
                refreshHeaderView.getRight(),
                top + refreshHeaderHeight);
    }

    private float layoutContentView(int top, boolean triggerCallback) {
        this.currentContentTop = top;
        contentWrapper.layout(top);

        float ratio = (Math.abs(top - minRefreshContentTop)) / (float) (refreshingContentTop - minRefreshContentTop);
        ratio = ratio < 0 ? 0 : ratio;
        ratio = ratio > 1 ? 1 : ratio;
        if (triggerCallback) {
            refreshHeaderWrapper.onPullRefreshHeader(ratio);
        }
        return ratio;
    }

    private float layoutContentView(int top) {
        return layoutContentView(top, true);
    }

    public void setRefreshing(boolean refreshing) {

        if (refreshHeaderWrapper == null) {
            throw new IllegalArgumentException("please use setRefreshHeader before setRefreshing");
        }
        //已经开始这个状态直接返回
        if (this.refreshing == refreshing || animatorRunning) {
            return;
        }

        if (refreshing) {
            this.refreshing = true;
            post(new Runnable() {
                @Override
                public void run() {
                    expandRefreshHeader(false);
                }
            });
        } else {
            setState(RefreshHeaderState.REFRESH_COMPLETED);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    SmoothRefreshLayout.this.refreshing = false;
                    if (contentWrapper.topChildIsFirstItem()) {
                        collapseRefreshHeader();
                    } else {
                        onCollapseAnimatorEnd();
                    }
                }
            }, DEFAULT_ANIMATOR_DURATION);
        }
    }

    //展开刷新header的动画
    private void expandRefreshHeader(final boolean isTouchTrigger) {
        animatorRunning = true;
        refreshHeaderView.setVisibility(VISIBLE);
        if (refreshHeaderAnimator != null) {
            refreshHeaderAnimator.cancel();
        }
        setState(RefreshHeaderState.REFRESHING);

        refreshHeaderAnimator = getRefreshHeaderAnimator(true, contentWrapper.getTopOffset(), refreshingContentTop);

        refreshHeaderAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onExpandAnimatorEnd(isTouchTrigger);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onExpandAnimatorEnd(isTouchTrigger);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        refreshHeaderAnimator.start();
    }

    private void onExpandAnimatorEnd(boolean isTouchTrigger) {
        layoutContentView(refreshingContentTop);
        layoutRefreshHeaderView(refreshingContentTop - refreshHeaderHeight);
        animatorRunning = false;
        refreshing = true;
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
        if (!isTouchTrigger) {
            contentWrapper.smoothScrollVerticalToTop();
        }
    }

    //松手时返回顶部的动画
    private void collapseRefreshHeader() {
        animatorRunning = true;
        if (refreshHeaderAnimator != null) {
            refreshHeaderAnimator.cancel();
        }
        refreshHeaderAnimator = getRefreshHeaderAnimator(false, contentWrapper.getTopOffset(), minRefreshContentTop);
        refreshHeaderAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onCollapseAnimatorEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onCollapseAnimatorEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        refreshHeaderAnimator.start();
    }

    private void onCollapseAnimatorEnd() {
        layoutContentView(minRefreshContentTop);
        layoutRefreshHeaderView(minRefreshContentTop - refreshHeaderHeight);

        animatorRunning = false;
        refreshing = false;
        refreshHeaderView.setVisibility(INVISIBLE);
        currentRefreshState = RefreshHeaderState.NONE;

    }

    private ValueAnimator getRefreshHeaderAnimator(final boolean isExpandAnimator, final int startValue, final int endValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startValue, endValue);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int newValue = (int) animation.getAnimatedValue();
//                final int oldValue = contentWrapper.getTopOffset();
                layoutContentView(newValue);
                if (isExpandAnimator || refreshHeaderView.getTop() > newValue - refreshHeaderHeight) {
                    layoutRefreshHeaderView(newValue - refreshHeaderHeight);
                }
//                if (endValue > startValue || refreshHeaderView.getTop() >= newValue - refreshHeaderHeight) {
//                    contentWrapper.scrollVerticalBy(oldValue - newValue);
//                }
            }
        });

        int duration;
        if (startValue < endValue) {
            duration = DEFAULT_ANIMATOR_DURATION;
        } else {
            duration = Math.min((int) (((float) startValue - minRefreshContentTop) / refreshHeaderHeight * DEFAULT_ANIMATOR_DURATION),
                    DEFAULT_ANIMATOR_DURATION);
        }
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        return valueAnimator;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    private void setState(@RefreshHeaderState int state) {
        if (state != this.currentRefreshState && refreshHeaderWrapper != null) {
            refreshHeaderWrapper.onStateChanged(state);
        }
        this.currentRefreshState = state;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (refreshHeaderAnimator != null && refreshHeaderAnimator.isRunning()) {
            refreshHeaderAnimator.cancel();
        }
        onRefreshListener = null;
        if (contentWrapper != null) {
            contentWrapper.recycle();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes) {
        if (target == contentView
                && axes == ViewCompat.SCROLL_AXIS_VERTICAL
                && isEnabled()
//                && !refreshing
//                && !animatorRunning
                ) {
            return true;
        }
        return super.onStartNestedScroll(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
        if (!refreshing && !animatorRunning) {
            if (dy < 0 && !canChildScrollUp()) {
                handleTouchActionMove(-dy);
                enterPullRefreshHeader = true;
            } else if (dy > 0 && enterPullRefreshHeader) {
                handleTouchActionMove(-dy);
            }
        }
//
//        if (!refreshing && dy > 0 && refreshHeaderView.getTop() > minRefreshContentTop - refreshHeaderHeight) {
//            consumed[1] = dy;
//        }


//        if (refreshHeaderView.getTop() >= minRefreshContentTop - refreshHeaderHeight
//                && refreshHeaderView.getTop() <= refreshingContentTop - refreshHeaderHeight) {
//            if (dy > 0) {
//                consumed[1] = dy;
//            }
//            if (refreshing && !(contentWrapper instanceof ListWrapper)) {
//                int result = contentView.getTop() - dy;
//                if (result > refreshingContentTop) result = refreshingContentTop;
//                if (result < minRefreshContentTop) result = minRefreshContentTop;
//                layoutContentView(result, false);
//                consumed[1] = dy;
//            }
//        }


    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
//        if (!(contentWrapper instanceof ListWrapper)
//                && refreshing
//                && refreshHeaderView.getTop() >= minRefreshContentTop - refreshHeaderHeight
//                && refreshHeaderView.getTop() <= refreshingContentTop - refreshHeaderHeight) {
//            return true;
//        }
        return !refreshing && isRefreshHeaderVisible();
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        if (enterPullRefreshHeader) {
            handleTouchActionUp();
        }
        enterPullRefreshHeader = false;
    }
}
