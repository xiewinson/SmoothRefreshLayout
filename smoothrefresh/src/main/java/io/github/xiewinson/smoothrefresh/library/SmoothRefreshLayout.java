package io.github.xiewinson.smoothrefresh.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

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

public class SmoothRefreshLayout extends ViewGroup implements NestedScrollingParent {

    private NestedScrollingParentHelper parentHelper;

    public SmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentHelper = new NestedScrollingParentHelper(this);
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

    private int contentRefreshingTop;
    private int contentMinTop;
    private int contentMaxTop;

    private int headerRefreshingTop;
    private int headerMinTop;
    private int headerMaxTop;

    private boolean enterPullRefreshHeader;

    private int currentRefreshHeaderTop = 0;
    private int currentContentTop = 0;

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

    private boolean isRecyclerView = false;

    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    private void init() {

        contentView = getChildAt(0);
        isRecyclerView = contentView instanceof RecyclerView;
        if (contentView == null) {
            throw new NullPointerException("you must put a contentView");
        }
        contentWrapper = ContentViewWrapper.Factory.getInstance(contentView);
        if (contentWrapper instanceof ListWrapper) {
            ((ListWrapper) contentWrapper).setContentViewScrollListener(new OnContentViewScrollListener() {
                @Override
                public void onFirstItemScroll(int firstItemY) {
                    if (refreshHeaderView != null && refreshing) {
                        moveRefreshHeaderView(computeRefreshHeaderTopByContentTop(firstItemY));
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
        addView(refreshHeaderView, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
        int[] params = refreshHeaderWrapper.getRefreshHeaderPosCalculator().getRefreshHeaderPosition(refreshHeaderView,
                contentView.getTop(),
                contentView.getPaddingTop());

        headerMinTop = params[0];
        headerRefreshingTop = params[1];
        headerMaxTop = params[2];

        contentMinTop = contentWrapper.getTopOffset();
        contentRefreshingTop = contentWrapper.getTopOffset() + (headerRefreshingTop - headerMinTop);
        contentMaxTop = computeContentTopByRefreshHeaderTop(headerMaxTop);

        moveRefreshHeaderView(headerMinTop);

    }

    private int computeRefreshHeaderTopByContentTop(int currentContentTop) {
        return currentContentTop - contentMinTop + headerMinTop;
    }

    private int computeContentTopByRefreshHeaderTop(int currentRefreshHeaderTop) {
        return currentRefreshHeaderTop - headerMinTop + contentMinTop;
    }

    private boolean isRefreshHeaderVisible() {
        return refreshHeaderView.getTop() > headerMinTop;
    }

    private boolean isRefreshHeaderPartVisible() {
        int top = refreshHeaderView.getTop();
        return top > headerMinTop
                && top <= headerRefreshingTop;
    }

    private boolean isRefreshHeaderOverPull() {
        int top = refreshHeaderView.getTop();
        return top > headerRefreshingTop
                && top <= headerMaxTop;
    }

    private boolean isRefreshHeaderFullVisible() {
        return refreshHeaderView.getTop() == headerRefreshingTop;
    }

    private boolean isRefreshHeaderInVisible() {
        return refreshHeaderView.getTop() <= headerMinTop;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if(getPaddingTop() != 0 || getPaddingBottom() != 0) {
//            setPadding(getPaddingLeft(), 0, getPaddingRight(), 0);
//        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        refreshHeaderView.layout(getPaddingLeft(),
                currentRefreshHeaderTop,
                getPaddingLeft() + refreshHeaderView.getMeasuredWidth(),
                currentRefreshHeaderTop + refreshHeaderView.getMeasuredHeight());
        if (!contentWrapper.isList()) {
            int pd = currentContentTop;
            if (currentContentTop == 0) {
                pd = pd + getPaddingTop();
            }
            contentView.layout(getPaddingLeft(),
                    pd,
                    getPaddingLeft() + contentView.getMeasuredWidth(),
                    pd + contentView.getMeasuredHeight());
        } else {
            contentView.layout(getPaddingLeft(),
                    getPaddingTop(),
                    getPaddingLeft() + contentView.getMeasuredWidth(),
                    getPaddingTop() + contentView.getMeasuredHeight());
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
                            && refreshHeaderView.getTop() != contentMaxTop)) {
                        contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                        handleTouchActionMove(dy);
                    }
                }
                //上滑
                else if (enterPullRefreshHeader && dy < 0) {
                    if (refreshHeaderView.getTop() != contentMinTop) {
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
            dy *= 0.2f;
        }
        if (dy > 0 && dy < 1) {
            dy = 1;
        } else if (dy > -1 && dy < 0) {
            dy = -1;
        }
        int result = (int) (dy + contentWrapper.getTopOffset());
        result = Math.max(result, contentMinTop);
        result = Math.min(result, contentMaxTop);

        if (result >= contentRefreshingTop) {
            setState(RefreshHeaderState.RELEASE_TO_REFRESH);
        } else {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
        }

        moveContentView(result);
        moveRefreshHeaderView(computeRefreshHeaderTopByContentTop(result));

    }

    /**
     * @return 若返回true，则将变为刷新状态
     */
    private boolean handleTouchActionUp() {
        if (isRefreshHeaderFullVisible()) {
            onExpandAnimatorEnd(true);
            return true;
        } else if (isRefreshHeaderOverPull()) {
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

    private void moveRefreshHeaderView(int top) {
        this.currentRefreshHeaderTop = top;
        refreshHeaderView.offsetTopAndBottom(-refreshHeaderView.getTop() + top);
        refreshHeaderView.setVisibility(refreshHeaderView.getTop() <= headerMinTop ? INVISIBLE : VISIBLE);
    }

    private float moveContentView(int top, boolean triggerCallback) {
        this.currentContentTop = top;
        contentWrapper.moveContentView(top);

        float ratio = (Math.abs(top - contentMinTop)) / (float) (contentRefreshingTop - contentMinTop);
        ratio = ratio < 0 ? 0 : ratio;
        ratio = ratio > 1 ? 1 : ratio;
        if (triggerCallback) {
            refreshHeaderWrapper.onPullRefreshHeader(ratio);
        }
        return ratio;
    }

    private float moveContentView(int top) {
        return moveContentView(top, true);
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

        refreshHeaderAnimator = getRefreshHeaderAnimator(contentWrapper.getTopOffset(), contentRefreshingTop);

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
        moveContentView(contentRefreshingTop);
        moveRefreshHeaderView(headerRefreshingTop);
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
        refreshHeaderAnimator = getRefreshHeaderAnimator(contentWrapper.getTopOffset(), contentMinTop);
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
        moveContentView(contentMinTop);
        moveRefreshHeaderView(headerMinTop);

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
                int newContentValue = (int) animation.getAnimatedValue();
                int oldHeaderValue = refreshHeaderView.getTop();
                int newHeaderValue = computeRefreshHeaderTopByContentTop(newContentValue);
                moveContentView(newContentValue);
                if (refreshHeaderView.getTop() > newHeaderValue) {
                    moveRefreshHeaderView(newHeaderValue);
                }
                if (isRecyclerView && startValue < endValue) {
                    contentView.scrollBy(0, oldHeaderValue - newContentValue);
                }
            }
        });

        int duration;
        if (startValue < endValue) {
            duration = DEFAULT_ANIMATOR_DURATION;
        } else {
            duration = Math.min((int) (((float) startValue - contentMinTop) / (headerRefreshingTop - headerMinTop) * DEFAULT_ANIMATOR_DURATION),
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
        if (target == contentView && axes == ViewCompat.SCROLL_AXIS_VERTICAL && isEnabled()) {
            return true;
        }
        return super.onStartNestedScroll(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (!refreshing && !animatorRunning) {
            if (dy < 0 && !canChildScrollUp()) {
                handleTouchActionMove(-dy);
                enterPullRefreshHeader = true;
            } else if (dy > 0 && enterPullRefreshHeader) {
                handleTouchActionMove(-dy);
            }
        }

        if (!contentWrapper.isList()) {
            if (!refreshing && dy > 0 && isRefreshHeaderVisible()) {
                consumed[1] = dy;
            }

            if (refreshing) {
                int result = contentView.getTop() - dy;
                if (result > contentRefreshingTop) result = contentRefreshingTop;
                if (result < contentMinTop) result = contentMinTop;
                if (dy > 0 || !canChildScrollUp()) {
                    moveContentView(result, false);
                    moveRefreshHeaderView(computeRefreshHeaderTopByContentTop(result));
                }
                if (isRefreshHeaderVisible()) {
                    consumed[1] = dy;
                }
            }
        }

    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (!contentWrapper.isList() && isRefreshHeaderVisible()) {
            return true;
        }
        return !refreshing && isRefreshHeaderVisible();
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        parentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {
        parentHelper.onStopNestedScroll(child);
        if (enterPullRefreshHeader) {
            handleTouchActionUp();
        }
        enterPullRefreshHeader = false;
    }
}
