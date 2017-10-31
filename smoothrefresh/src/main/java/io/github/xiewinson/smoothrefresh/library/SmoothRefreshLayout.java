package io.github.xiewinson.smoothrefresh.library;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import io.github.xiewinson.smoothrefresh.library.annotation.PageState;
import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.listener.OnListScrollListener;
import io.github.xiewinson.smoothrefresh.library.listener.OnLoadMoreListener;
import io.github.xiewinson.smoothrefresh.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefresh.library.wrapper.content.ContentViewWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.content.IContentViewWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.content.ListWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.HeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.IHeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.page.IPageWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.page.PageWrapper;

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

    private int contentRefreshingOffset;
    private int contentMinOffset;
    private int contentMaxOffset;
    private int correctContentPaddingBottom;

    private int headerRefreshingOffset;
    private int headerMinOffset;
    private int headerMaxOffset;

    private boolean enterPullRefreshHeader;

    private int currentHeaderOffset = 0;
    private int currentContentOffset = 0;
    private int currentPageOffset = 0;

    private int correctOverScrollMode;
    private int activePointerId;
    private float lastHeaderY = -1;
    private int currentRefreshState = RefreshHeaderState.NONE;

    //是否处于刷新
    private boolean refreshing;

    //是否处于加载更多
    private boolean isLoadMore;

    //是否正在执行动画
    private boolean animatorRunning = false;

    private ValueAnimator headerAnimator;

    private OnRefreshListener onRefreshListener;

    private OnLoadMoreListener onLoadMoreListener;

    private IPageWrapper pageWrapper;
    private View pageView;
    private boolean footerEnable = true;

    private int currentPageState = PageState.NONE;

    public static final int DEFAULT_ANIMATOR_DURATION = 300;

    private boolean isRecyclerView = false;

    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void init() {

        LayoutTransition transition = new LayoutTransition();
        setLayoutTransition(transition);

        contentView = getChildAt(0);
        isRecyclerView = contentView instanceof RecyclerView;
        if (contentView == null) {
            throw new NullPointerException("you must put a contentView");
        }
        contentWrapper = ContentViewWrapper.Factory.getInstance(contentView);
        if (contentWrapper instanceof ListWrapper) {
            ((ListWrapper) contentWrapper).setOnListScrollListener(new OnListScrollListener() {
                @Override
                public void onFirstItemScroll(int firstItemY) {
                    if (headerView != null && refreshing) {
                        moveHeaderView(computeHeaderTopByContentTop(firstItemY));
                    }

                }

                @Override
                public void onBottomItemScroll(int lastItemY) {
                    currentPageOffset = lastItemY;
                    if (footerEnable && pageView != null) {
                        movePageView(currentPageOffset);
                    }
                }

                @Override
                public void onReachBottom() {
                    if (footerEnable
                            && onLoadMoreListener != null
                            && !enterPullRefreshHeader
                            && !refreshing
                            && !isLoadMore
                            && currentPageState == PageState.NONE) {
                        isLoadMore = true;
                        setPageState(PageState.LOADING_FOOTER);
                        onLoadMoreListener.onLoadMore();
                    }
                }
            });
        }
    }

    public void setRefreshHeader(HeaderWrapper headerWrapper) {
        this.headerWrapper = headerWrapper;
        this.headerView = headerWrapper.getView(this);
        initHeaderView();
    }

    public void setPages(PageWrapper pageWrapper) {
        this.pageWrapper = pageWrapper;
    }

    public void setFooterEnable(boolean enable) {
        this.footerEnable = enable;
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

        headerMinOffset = params[0];
        headerRefreshingOffset = params[1];
        headerMaxOffset = params[2];

        contentMinOffset = contentWrapper.getTopOffset();
        contentRefreshingOffset = contentWrapper.getTopOffset() + (headerRefreshingOffset - headerMinOffset);
        contentMaxOffset = computeContentTopByHeaderTop(headerMaxOffset);

        correctContentPaddingBottom = contentView.getPaddingBottom();
        moveHeaderView(headerMinOffset);

    }

    private int computeHeaderTopByContentTop(int currentContentTop) {
        return currentContentTop - contentMinOffset + headerMinOffset;
    }

    private int computeContentTopByHeaderTop(int currentRefreshHeaderTop) {
        return currentRefreshHeaderTop - headerMinOffset + contentMinOffset;
    }

    private boolean isHeaderVisible() {
        return headerView.getTop() > headerMinOffset;
    }

    private boolean isHeaderPartVisible() {
        int top = headerView.getTop();
        return top > headerMinOffset
                && top <= headerRefreshingOffset;
    }

    private boolean isHeaderOverPull() {
        int top = headerView.getTop();
        return top > headerRefreshingOffset
                && top <= headerMaxOffset;
    }

    private boolean isHeaderFullVisible() {
        return headerView.getTop() == headerRefreshingOffset;
    }

    private boolean isHeaderInVisible() {
        return headerView.getTop() <= headerMinOffset;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View chid = getChildAt(i);
            if (chid.getVisibility() != GONE) {
                measureChild(chid, widthMeasureSpec, heightMeasureSpec);
            }
        }

    }

    private boolean pageIsFooter() {
        return contentWrapper.isList()
                && (currentPageState == PageState.EMPTY_FOOTER
                || currentPageState == PageState.ERROR_FOOTER
                || currentPageState == PageState.LOADING_FOOTER
                || currentPageState == PageState.NO_MORE_FOOTER);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (headerView.getVisibility() != GONE) {
            headerView.layout(getPaddingLeft(),
                    currentHeaderOffset,
                    getPaddingLeft() + headerView.getMeasuredWidth(),
                    currentHeaderOffset + headerView.getMeasuredHeight());
        }

        if (contentView.getVisibility() != GONE) {
            if (!contentWrapper.isList()) {
                int contentTop = currentContentOffset;
                if (currentContentOffset == 0) {
                    contentTop = contentTop + getPaddingTop();
                }
                contentView.layout(getPaddingLeft(),
                        contentTop,
                        getPaddingLeft() + contentView.getMeasuredWidth(),
                        contentTop + contentView.getMeasuredHeight());
            } else {
                contentView.layout(getPaddingLeft(),
                        getPaddingTop(),
                        getPaddingLeft() + contentView.getMeasuredWidth(),
                        getPaddingTop() + contentView.getMeasuredHeight());
            }
        }

        if (pageView != null && pageView.getVisibility() != GONE) {
            int contentTop = getPaddingTop();
//            if (pageIsFooter()) {
//                contentTop = currentPageOffset;
//            }
            pageView.layout(getPaddingLeft(),
                    contentTop,
                    getPaddingLeft() + pageView.getMeasuredWidth(),
                    contentTop + pageView.getMeasuredHeight());
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
                    if ((!canChildScrollUp()
                            && headerView.getTop() != contentMaxOffset)) {
                        enterPullRefreshHeader = true;
                        contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                        handleTouchActionMove(dy);
                    }
                }
                //上滑
                else if (enterPullRefreshHeader && dy < 0) {
                    if (headerView.getTop() != contentMinOffset) {
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

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = ev.getActionIndex();
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == activePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            activePointerId = ev.getPointerId(newPointerIndex);
        }
    }

    private boolean canChildScrollUp() {
        return contentView != null && contentView.canScrollVertically(-1);
    }

    private void handleTouchActionMove(float dy) {
        if (dy > 0) {
            dy *= 0.2f;
        }
        if (dy > 0 && dy < 1) {
            dy = 1;
        } else if (dy > -1 && dy < 0) {
            dy = -1;
        }
        int result = (int) (dy + contentWrapper.getTopOffset());
        result = Math.max(result, contentMinOffset);
        result = Math.min(result, contentMaxOffset);

        if (result >= contentRefreshingOffset) {
            setHeaderState(RefreshHeaderState.RELEASE_TO_REFRESH);
        } else {
            setHeaderState(RefreshHeaderState.PULL_TO_REFRESH);
        }

        int headerResult = computeHeaderTopByContentTop(result);
        if (isRecyclerView && dy < 0 && pageView != null) {
            movePageView((int) (headerResult - headerView.getTop() + pageView.getY()));
        }
        moveContentView(result);
        moveHeaderView(headerResult);

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
            setHeaderState(RefreshHeaderState.PULL_TO_REFRESH);
            showExitRefreshAnim();
        } else if (isHeaderInVisible()) {
            onExitRefreshAnimEnd();
        }
        return false;
    }

    private void moveHeaderView(int top) {
        this.currentHeaderOffset = top;
        headerView.offsetTopAndBottom(-headerView.getTop() + top);
        headerView.setVisibility(headerView.getTop() <= headerMinOffset ? INVISIBLE : VISIBLE);
    }

    private float moveContentView(int top, boolean triggerCallback) {
        this.currentContentOffset = top;
        contentWrapper.moveContentView(top);

        float ratio = (Math.abs(top - contentMinOffset)) / (float) (contentRefreshingOffset - contentMinOffset);
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


    private void movePageView(int lastItemY) {
        this.currentPageOffset = lastItemY;
        if (pageView != null) {
//            pageView.offsetTopAndBottom(-pageView.getTop() + lastItemY);
            if (isFullScreenPage()) {
                pageView.setY(0);
            } else {
                pageView.setY(lastItemY);
            }
            pageView.setVisibility(pageView.getY() > getBottom()
                    || currentPageState == PageState.NONE ? INVISIBLE : VISIBLE);
        }
    }

    @UiThread
    private void setPageState(@PageState int state) {
//        if (!isFullScreenPage() && !contentWrapper.isList()) {
//            return;
//        }
        if (pageWrapper == null)
            throw new IllegalArgumentException("you must use setPages() before change pages");
        if (this.currentPageState == state) return;
        this.currentPageState = state;
        if (pageView != null) removeView(pageView);

        pageView = pageWrapper.getView(this, state);
        if (pageView != null) {
            addView(pageView);
            if (!isFullScreenPage()) movePageView(this.currentPageOffset);
        }

        if (footerEnable
                && pageView != null
                && !isFullScreenPage()
                && contentWrapper.isList()) {

            pageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int newPaddingBottom = correctContentPaddingBottom + pageView.getMeasuredHeight();
                    if (newPaddingBottom > contentView.getPaddingBottom()
                            || currentPageState == PageState.NONE) {
                        contentView.setPadding(contentView.getPaddingLeft(),
                                contentView.getPaddingTop(),
                                contentView.getPaddingRight(),
                                newPaddingBottom);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        pageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        switch (state) {
            case PageState.NONE:
                contentView.setVisibility(VISIBLE);
                break;
            case PageState.LOADING:
                contentView.setVisibility(GONE);
                break;
            case PageState.ERROR:
                contentView.setVisibility(GONE);
                onExitRefreshAnimEnd();
                break;

            case PageState.EMPTY:
                contentView.setVisibility(GONE);
                onExitRefreshAnimEnd();
                break;

        }

    }

    @UiThread
    public void showErrorPage() {
        setPageState(PageState.ERROR);
    }


    public void showErrorFooter() {
        if (!footerEnable) {
            return;
        }
//                isLoadMore = false;
        setPageState(PageState.ERROR_FOOTER);
    }

    @UiThread
    public void showEmptyPage() {

        setPageState(PageState.EMPTY);
    }

    @UiThread
    public void showEmptyFooter() {
        if (!footerEnable) {
            return;
        }
//                isLoadMore = false;
        setPageState(PageState.EMPTY_FOOTER);

    }

    @UiThread
    public void setLoadMore(final boolean loadMore) {
        if (!footerEnable) {
            return;
        }
        isLoadMore = loadMore;
        if (isLoadMore) {
            setPageState(PageState.LOADING_FOOTER);
            onLoadMoreListener.onLoadMore();
        } else {
            setPageState(PageState.NONE);
        }
    }

    private boolean isFullScreenPage() {
        return (currentPageState == PageState.EMPTY ||
                currentPageState == PageState.LOADING ||
                currentPageState == PageState.ERROR);
    }

    @UiThread
    public void setRefreshing(boolean refreshing) {
//        if (isLoadMore) {
//            return;
//        }
        if (headerWrapper == null) {
            throw new IllegalArgumentException("please use setRefreshHeader before setRefreshing");
        }
        //已经开始这个状态直接返回
        if (this.refreshing == refreshing || animatorRunning) {
            return;
        }

        if (pageWrapper != null
                && pageWrapper.getView(this, PageState.LOADING) != null
                && refreshing
                && (!contentWrapper.hasListItemChild() || isFullScreenPage())) {
            setPageState(PageState.LOADING);
            this.refreshing = true;
            post(new Runnable() {
                @Override
                public void run() {
                    onRefreshListener.onRefresh();
                }
            });
        }

        if (!refreshing) {
            if (isFullScreenPage()) this.refreshing = false;
            setPageState(PageState.NONE);
        }

        if (isFullScreenPage()) {
            this.refreshing = refreshing;
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
            setHeaderState(RefreshHeaderState.REFRESH_COMPLETED);
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
        setHeaderState(RefreshHeaderState.REFRESHING);

        headerAnimator = headerAnimator(contentWrapper.getTopOffset(), contentRefreshingOffset);

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
        isLoadMore = false;
        setPageState(PageState.NONE);
        moveContentView(contentRefreshingOffset);
        moveHeaderView(headerRefreshingOffset);
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
        headerAnimator = headerAnimator(contentWrapper.getTopOffset(), contentMinOffset);
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
        moveContentView(contentMinOffset);
        moveHeaderView(headerMinOffset);

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
                if (pageView != null) {
                    movePageView((int) (newHeaderValue - oldHeaderValue + pageView.getY()));
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
            duration = Math.min((int) (((float) startValue - contentMinOffset) / (headerRefreshingOffset - headerMinOffset) * DEFAULT_ANIMATOR_DURATION),
                    DEFAULT_ANIMATOR_DURATION);
        }
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        return valueAnimator;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    private void setHeaderState(@RefreshHeaderState int state) {
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
        onLoadMoreListener = null;
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
                if (result > contentRefreshingOffset) result = contentRefreshingOffset;
                if (result < contentMinOffset) result = contentMinOffset;
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
