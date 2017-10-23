package io.github.xiewinson.smoothrefresh.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
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
        contentWrapper.setContentViewScrollListener(new OnContentViewScrollListener() {
            @Override
            public void onFirstItemScroll(int firstItemY) {
                if (refreshHeaderView != null) {
                    layoutRefreshHeaderView(firstItemY - refreshHeaderHeight);
                }
            }

            @Override
            public void onScroll(int offset) {
                if (refreshHeaderView != null && refreshing) {
                    refreshHeaderView.offsetTopAndBottom(offset);
                }
            }
        });
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
        minRefreshContentTop = contentView.getPaddingTop();
        refreshingContentTop = contentView.getPaddingTop() + refreshHeaderHeight;
        maxRefreshContentTop = refreshingContentTop + refreshHeaderHeight * 3;
        layoutRefreshHeaderView(minRefreshContentTop - refreshHeaderHeight);

    }

    private void layoutRefreshHeaderView(int top) {
//        if(top == -1) {
//            top = contentView.getPaddingTop() - refreshHeaderView.getMeasuredHeight();
//        }
        this.currentRefreshHeaderTop = top;
        refreshHeaderView.layout(refreshHeaderView.getLeft(),
                top,
                refreshHeaderView.getRight(),
                top + refreshHeaderHeight);
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
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (contentWrapper.isSupportNestedScroll()) {
            return super.dispatchTouchEvent(ev);
        }

        int action = MotionEventCompat.getActionMasked(ev);
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
                            && contentView.getPaddingTop() != maxRefreshContentTop + refreshHeaderHeight)) {
                        contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                        handleTouchActionMove(dy);
                    }
                }
                //上滑
                else if (enterPullRefreshHeader && dy < 0) {
                    if (contentView.getPaddingTop() != refreshingContentTop
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
        int result = (int) (contentView.getPaddingTop() + dy);

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

        changeContentPaddingTop(result);

    }

    /**
     * @return 若返回true，则将变为刷新状态
     */
    private boolean handleTouchActionUp() {
        float currentY = refreshHeaderView.getTop();
        if (currentY >= refreshingContentTop - refreshHeaderHeight || currentRefreshState == RefreshHeaderState.RELEASE_TO_REFRESH) {
            expandRefreshHeader(true);
            return true;
        } else if (currentY < refreshingContentTop - refreshHeaderHeight && currentY > minRefreshContentTop - refreshHeaderHeight) {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
            collapseRefreshHeader();
        }
        return false;
    }

    private float changeContentPaddingTop(int paddingTop) {
        contentView.setPadding(contentView.getPaddingLeft(),
                paddingTop,
                contentView.getPaddingRight(),
                contentView.getPaddingBottom());
        float ratio = (Math.abs(paddingTop - minRefreshContentTop)) / (float) (refreshingContentTop - minRefreshContentTop);
        ratio = ratio < 0 ? 0 : ratio;
        ratio = ratio > 1 ? 1 : ratio;
        refreshHeaderWrapper.onPullRefreshHeader(ratio);
        return ratio;
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

        refreshHeaderAnimator = getRefreshHeaderAnimator(contentView.getPaddingTop(), refreshingContentTop);

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
        changeContentPaddingTop(refreshingContentTop);
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
        refreshHeaderAnimator = getRefreshHeaderAnimator(contentView.getPaddingTop(), minRefreshContentTop);
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
        changeContentPaddingTop(minRefreshContentTop);
        animatorRunning = false;
        refreshing = false;
        refreshHeaderView.setVisibility(INVISIBLE);
        currentRefreshState = RefreshHeaderState.NONE;

    }

    private ValueAnimator getRefreshHeaderAnimator(final int startValue, final int endValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startValue, endValue);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int newValue = (int) animation.getAnimatedValue();
                final int oldValue = contentView.getPaddingTop();
                changeContentPaddingTop(newValue);
                if (endValue > startValue || refreshHeaderView.getTop() >= newValue - refreshHeaderHeight) {
                    contentWrapper.scrollVerticalBy(oldValue - newValue);
                }
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
            contentWrapper.removeContentViewScrollListener();
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, @ViewCompat.ScrollAxis int axes) {
        if (target == contentView
                && axes == ViewCompat.SCROLL_AXIS_VERTICAL
                && !canChildScrollUp()
                && isEnabled()
                && !refreshing
                && !animatorRunning) {
            return true;
        }
        return super.onStartNestedScroll(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
        handleTouchActionMove(-dy);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return refreshHeaderView.getTop() > minRefreshContentTop - refreshHeaderHeight;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        super.onNestedScrollAccepted(child, target, axes);
        enterPullRefreshHeader = true;
    }

    @Override
    public void onStopNestedScroll(View child) {
        super.onStopNestedScroll(child);
        enterPullRefreshHeader = false;
        handleTouchActionUp();
    }
}
