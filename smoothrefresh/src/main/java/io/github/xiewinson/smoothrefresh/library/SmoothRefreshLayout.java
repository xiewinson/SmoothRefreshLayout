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
import io.github.xiewinson.smoothrefresh.library.wrapper.header.HeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.IHeaderWrapper;

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
    private IHeaderWrapper headerWrapper;
    private View headerView;
    private View contentView;

    private int contentRefreshingTop;
    private int contentMinTop;
    private int contentMaxTop;

    private int headerRefreshingTop;
    private int headerMinTop;
    private int headerMaxTop;

    private boolean enterPullRefreshHeader;

    private int currentHeaderTop = 0;
    private int currentContentTop = 0;

    private int correctOverScrollMode;
    private float lastHeaderY = -1;
    private int currentRefreshState = RefreshHeaderState.NONE;

    //是否处于刷新
    private boolean refreshing;
    //是否正在执行动画
    private boolean animatorRunning = false;

    private ValueAnimator headerAnimator;

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
                    if (headerView != null && refreshing) {
                        moveHeaderView(computeHeaderTopByContentTop(firstItemY));
                    }
                }
            });
        }
    }

    public void setRefreshHeader(HeaderWrapper headerWrapper) {
        this.headerWrapper = headerWrapper;
        this.headerView = headerWrapper.getHeaderView(this);
        initHeaderView();
    }

    private void initHeaderView() {
        headerView.setVisibility(INVISIBLE);
        addView(headerView);
        post(new Runnable() {
            @Override
            public void run() {
                initHeaderParams();
            }
        });
    }


    private void initHeaderParams() {
        if (headerView == null) {
            throw new IllegalArgumentException("please use setRefreshHeader before initHeaderParams");
        }
        int[] params = headerWrapper.getHeaderPosCalculator().getRefreshHeaderPosition(headerView,
                contentView.getTop(),
                contentView.getPaddingTop());

        headerMinTop = params[0];
        headerRefreshingTop = params[1];
        headerMaxTop = params[2];

        contentMinTop = contentWrapper.getTopOffset();
        contentRefreshingTop = contentWrapper.getTopOffset() + (headerRefreshingTop - headerMinTop);
        contentMaxTop = computeContentTopByHeaderTop(headerMaxTop);

        moveHeaderView(headerMinTop);

    }

    private int computeHeaderTopByContentTop(int currentContentTop) {
        return currentContentTop - contentMinTop + headerMinTop;
    }

    private int computeContentTopByHeaderTop(int currentRefreshHeaderTop) {
        return currentRefreshHeaderTop - headerMinTop + contentMinTop;
    }

    private boolean isHeaderVisible() {
        return headerView.getTop() > headerMinTop;
    }

    private boolean isHeaderPartVisible() {
        int top = headerView.getTop();
        return top > headerMinTop
                && top <= headerRefreshingTop;
    }

    private boolean isHeaderOverPull() {
        int top = headerView.getTop();
        return top > headerRefreshingTop
                && top <= headerMaxTop;
    }

    private boolean isHeaderFullVisible() {
        return headerView.getTop() == headerRefreshingTop;
    }

    private boolean isHeaderInVisible() {
        return headerView.getTop() <= headerMinTop;
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
        headerView.layout(getPaddingLeft(),
                currentHeaderTop,
                getPaddingLeft() + headerView.getMeasuredWidth(),
                currentHeaderTop + headerView.getMeasuredHeight());

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
                lastHeaderY = currentY;
                correctOverScrollMode = contentView.getOverScrollMode();
//                initHeaderParams();
                break;


            case MotionEvent.ACTION_MOVE:
                float dy = currentY - lastHeaderY;
                //下拉
                if (dy > 0) {
                    if (enterPullRefreshHeader || (!canChildScrollUp()
                            && headerView.getTop() != contentMaxTop)) {
                        contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                        handleTouchActionMove(dy);
                    }
                }
                //上滑
                else if (enterPullRefreshHeader && dy < 0) {
                    if (headerView.getTop() != contentMinTop) {
                        handleTouchActionMove(dy);

                    }
                }
                lastHeaderY = currentY;
                lastHeaderY = currentY;
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
                lastHeaderY = -1;
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
        moveHeaderView(computeHeaderTopByContentTop(result));

    }

    /**
     * @return 若返回true，则将变为刷新状态
     */
    private boolean handleTouchActionUp() {
        if (isHeaderFullVisible()) {
            onEnterRefreshAnimEnd(true);
            return true;
        } else if (isHeaderOverPull()) {
            showEnterRefreshAnim(true);
            return true;
        } else if (isHeaderPartVisible()) {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
            showExitRefreshAnim();
        } else if (isHeaderInVisible()) {
            onExitRefreshAnimEnd();
        }
        return false;
    }

    private void moveHeaderView(int top) {
        this.currentHeaderTop = top;
        headerView.offsetTopAndBottom(-headerView.getTop() + top);
        headerView.setVisibility(headerView.getTop() <= headerMinTop ? INVISIBLE : VISIBLE);
    }

    private float moveContentView(int top, boolean triggerCallback) {
        this.currentContentTop = top;
        contentWrapper.moveContentView(top);

        float ratio = (Math.abs(top - contentMinTop)) / (float) (contentRefreshingTop - contentMinTop);
        ratio = ratio < 0 ? 0 : ratio;
        ratio = ratio > 1 ? 1 : ratio;
        if (triggerCallback) {
            headerWrapper.onPullRefreshHeader(ratio);
        }
        return ratio;
    }

    private float moveContentView(int top) {
        return moveContentView(top, true);
    }

    public void setRefreshing(boolean refreshing) {

        if (headerWrapper == null) {
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
                    showEnterRefreshAnim(false);
                }
            });
        } else {
            setState(RefreshHeaderState.REFRESH_COMPLETED);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    SmoothRefreshLayout.this.refreshing = false;
                    if (contentWrapper.topChildIsFirstItem()) {
                        showExitRefreshAnim();
                    } else {
                        onExitRefreshAnimEnd();
                    }
                }
            }, DEFAULT_ANIMATOR_DURATION);
        }
    }

    //展开刷新header的动画
    private void showEnterRefreshAnim(final boolean isTouchTrigger) {
        animatorRunning = true;
        headerView.setVisibility(VISIBLE);
        if (headerAnimator != null) {
            headerAnimator.cancel();
        }
        setState(RefreshHeaderState.REFRESHING);

        headerAnimator = headerAnimator(contentWrapper.getTopOffset(), contentRefreshingTop);

        headerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onEnterRefreshAnimEnd(isTouchTrigger);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onEnterRefreshAnimEnd(isTouchTrigger);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        headerAnimator.start();
    }

    private void onEnterRefreshAnimEnd(boolean isTouchTrigger) {
        moveContentView(contentRefreshingTop);
        moveHeaderView(headerRefreshingTop);
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
    private void showExitRefreshAnim() {
        animatorRunning = true;
        if (headerAnimator != null) {
            headerAnimator.cancel();
        }
        headerAnimator = headerAnimator(contentWrapper.getTopOffset(), contentMinTop);
        headerAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onExitRefreshAnimEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onExitRefreshAnimEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        headerAnimator.start();
    }

    private void onExitRefreshAnimEnd() {
        moveContentView(contentMinTop);
        moveHeaderView(headerMinTop);

        animatorRunning = false;
        refreshing = false;
        headerView.setVisibility(INVISIBLE);
        currentRefreshState = RefreshHeaderState.NONE;

    }

    private ValueAnimator headerAnimator(final int startValue, final int endValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startValue, endValue);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newContentValue = (int) animation.getAnimatedValue();
                int oldHeaderValue = headerView.getTop();
                int newHeaderValue = computeHeaderTopByContentTop(newContentValue);
                moveContentView(newContentValue);
                if (headerView.getTop() > newHeaderValue) {
                    moveHeaderView(newHeaderValue);
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
        if (state != this.currentRefreshState && headerWrapper != null) {
            headerWrapper.onStateChanged(state);
        }
        this.currentRefreshState = state;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (headerAnimator != null && headerAnimator.isRunning()) {
            headerAnimator.cancel();
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
        return false;
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
            if (!refreshing && dy > 0 && isHeaderVisible()) {
                consumed[1] = dy;
            }

            if (refreshing) {
                int result = contentView.getTop() - dy;
                if (result > contentRefreshingTop) result = contentRefreshingTop;
                if (result < contentMinTop) result = contentMinTop;
                if (dy > 0 || !canChildScrollUp()) {
                    moveContentView(result, false);
                    moveHeaderView(computeHeaderTopByContentTop(result));
                }
                if (isHeaderVisible()) {
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
        if (!contentWrapper.isList() && isHeaderVisible()) {
            return true;
        }
        return !refreshing && isHeaderVisible();
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
