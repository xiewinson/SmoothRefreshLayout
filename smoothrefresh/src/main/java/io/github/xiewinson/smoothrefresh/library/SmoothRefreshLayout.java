package io.github.xiewinson.smoothrefresh.library;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ListViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ListView;

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

import static android.animation.LayoutTransition.DISAPPEARING;

/**
 * Created by winson on 2017/10/3.
 */

public class SmoothRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private NestedScrollingParentHelper parentHelper;
    private NestedScrollingChildHelper childHelper;

    public SmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentHelper = new NestedScrollingParentHelper(this);
        childHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
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
    private int correctContentPaddingBottom = -1;

    private int headerRefreshingOffset;
    private int headerMinOffset;
    private int headerMaxOffset;

    private boolean isBeingDragged;
    private int touchSlop;

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

    private ValueAnimator refreshAnimator;

    private boolean isExpandingAnimator;
    private boolean isAnimTouchTrigger = true;
    private int startAnimValue;
    private int endAnimValue;

    private OnRefreshListener onRefreshListener;

    private OnLoadMoreListener onLoadMoreListener;

    private IPageWrapper pageWrapper;
    private View pageView;
    private boolean footerEnable = true;
    private boolean footerVisiblieWhenAnim = false;

    private int currentPageState;

    public static final int DEFAULT_ANIMATOR_DURATION = 200;

    private boolean isRecyclerView = false;
    private boolean isListView = false;

    private int[] parentConsumed = new int[2];

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

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        initRefreshAnimator();
        LayoutTransition transition = new LayoutTransition();
        ObjectAnimator anim = ObjectAnimator.ofFloat(null, "translationY", 300.0f);
        anim.setDuration(3000);
        transition.setAnimator(DISAPPEARING, anim);
//        setLayoutTransition(transition);
        contentView = getChildAt(0);

        if (contentView == null) {
            throw new NullPointerException("you must add a contentView");
        }
        isRecyclerView = contentView instanceof RecyclerView;
        isListView = contentView instanceof ListView;

        contentWrapper = ContentViewWrapper.Factory.getInstance(contentView);
        if (contentWrapper instanceof ListWrapper) {
            ((ListWrapper) contentWrapper).setOnListScrollListener(new OnListScrollListener() {
                @Override
                public void onFirstItemScroll(int firstItemY) {
                    if (headerView != null && (refreshing || isListView)) {
                        moveHeaderView(computeHeaderTopByContentTop(firstItemY));
                    }

                }

                @Override
                public void onBottomItemScroll(int bottomItemY) {
                    currentPageOffset = bottomItemY;
                    if (footerEnable && pageView != null && isFooterPage()) {
                        movePageView(currentPageOffset);
                    }
                }

                @Override
                public void onReachBottom() {
                    if (footerEnable
                            && isEnabled()
                            && onLoadMoreListener != null
                            && !isBeingDragged
                            && !refreshing
                            && !isLoadMore
                            && currentPageState == PageState.NONE) {
                        isLoadMore = true;
                        post(() -> {
                            if (currentPageState == PageState.NONE) {
                                setPageState(PageState.LOADING_FOOTER);
                                onLoadMoreListener.onLoadMore();
                            } else {
                                isLoadMore = false;
                            }
                        });

                    }
                }
            });
        }
    }

    private void initRefreshAnimator() {
        refreshAnimator = new ValueAnimator();
        refreshAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newContentValue = (int) animation.getAnimatedValue();
                int oldHeaderValue = headerView.getTop();
                int newHeaderValue = computeHeaderTopByContentTop(newContentValue);
                moveContentView(newContentValue);
                if (headerView.getTop() > newHeaderValue) {
                    moveHeaderView(newHeaderValue);
                }
                if (footerVisiblieWhenAnim) {
                    movePageView((int) (headerView.getTop() - oldHeaderValue + pageView.getY()));
                }

                if (isRecyclerView && startAnimValue < endAnimValue) {
                    contentWrapper.scrollVerticalBy(oldHeaderValue - newContentValue);
                }
            }
        });

        refreshAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (contentWrapper.isList() && !isExpandingAnimator) {
                    moveContentView(headerView.getBottom());
                    contentView.setPadding(contentView.getPaddingLeft(), contentMinOffset, contentView.getPaddingRight(), contentView.getPaddingBottom());

                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isExpandingAnimator) {
                    onEnterRefreshAnimEnd();
                } else {
                    onExitRefreshAnimEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (isExpandingAnimator) {
                    onEnterRefreshAnimEnd();
                } else {
                    onExitRefreshAnimEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        refreshAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public void setRefreshHeader(HeaderWrapper headerWrapper) {
        this.headerWrapper = headerWrapper;
        this.headerView = headerWrapper.getView(this);
        initHeaderView();
    }

    public void setPages(PageWrapper pageWrapper) {
        this.pageWrapper = pageWrapper;
        setPageState(PageState.NONE);
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (headerView != null && headerView.getVisibility() != GONE) {
            headerView.layout(getPaddingLeft(),
                    currentHeaderOffset,
                    getPaddingLeft() + headerView.getMeasuredWidth(),
                    currentHeaderOffset + headerView.getMeasuredHeight());
        }

        if (contentView.getVisibility() != GONE) {
//            if (!contentWrapper.isList()) {
            int contentTop = currentContentOffset;
            if (currentContentOffset == 0) {
                contentTop = contentTop + getPaddingTop();
            }
            contentView.layout(getPaddingLeft(),
                    contentTop,
                    getPaddingLeft() + contentView.getMeasuredWidth(),
                    contentTop + contentView.getMeasuredHeight());
//            } else {
//                contentView.layout(getPaddingLeft(),
//                        getPaddingTop(),
//                        getPaddingLeft() + contentView.getMeasuredWidth(),
//                        getPaddingTop() + contentView.getMeasuredHeight());
//            }
        }

        if (pageView != null && pageView.getVisibility() != GONE) {
            int contentTop = getPaddingTop();
//            if (isFooterPage()) {
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

        if (isFullScreenPage() || !isEnabled() || refreshing || animatorRunning) {
            return super.dispatchTouchEvent(ev);
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId(0);
                lastHeaderY = ev.getY(0);
                correctOverScrollMode = contentView.getOverScrollMode();
//                initHeaderParams();
                break;


            case MotionEvent.ACTION_MOVE:
                if (activePointerId == -1) {
                    return super.dispatchTouchEvent(ev);
                }
                float currentY = ev.getY(ev.findPointerIndex(activePointerId));
                float dy = currentY - lastHeaderY;
                //下拉
                if (dy > 0 && (isBeingDragged || Math.abs(dy) > touchSlop) && !canChildScrollUp()) {
                    isBeingDragged = true;
                    contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                    handleTouchActionMove(dy);
                    lastHeaderY = currentY;
                }
                //上滑
                else if (isBeingDragged && dy < 0) {
                    handleTouchActionMove(dy);
                    lastHeaderY = currentY;
                    if (currentHeaderOffset <= headerMinOffset) {
                        isBeingDragged = false;
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                int downPointIndex = ev.getActionIndex();
                if (downPointIndex < 0) {
                    return super.dispatchTouchEvent(ev);
                }
                activePointerId = ev.getPointerId(downPointIndex);
                lastHeaderY = ev.getY(downPointIndex);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int upPointIndex = ev.getActionIndex();
                int upPointerId = ev.getPointerId(upPointIndex);
                if (upPointerId == activePointerId) {
                    int newPointIndex = upPointIndex == 0 ? 1 : 0;
                    activePointerId = ev.getPointerId(newPointIndex);
                }
                lastHeaderY = ev.getY(ev.findPointerIndex(activePointerId));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (handleTouchActionUp()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                contentView.setOverScrollMode(correctOverScrollMode);
                lastHeaderY = -1;
                isBeingDragged = false;

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
        if (isListView) {
            return ListViewCompat.canScrollList((ListView) contentView, -1);
        }
        return contentView != null && contentView.canScrollVertically(-1);
    }

    private boolean canChildScrollDown() {
        return contentView != null && contentView.canScrollVertically(1);
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
        if (isRecyclerView && pageView != null) {
            movePageView((int) (headerResult - headerView.getTop() + pageView.getY()));
        }
        if (!isListView) {
            moveHeaderView(headerResult);
        }
        moveContentView(result);

    }

    /**
     * @return 若返回true，则将变为刷新状态
     */
    private boolean handleTouchActionUp() {
        isAnimTouchTrigger = true;
        if (isHeaderFullVisible()) {
            onEnterRefreshAnimEnd();
            return true;
        } else if (isHeaderOverPull()) {
            showEnterRefreshAnim();
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


    private void movePageView(int newY) {
        this.currentPageOffset = newY;
        if (pageView != null) {
//            pageView.offsetTopAndBottom(-pageView.getTop() + lastItemY);
            if (isFullScreenPage()) {
                pageView.setY(0);
            } else {
                pageView.setY(newY);
            }
//
        }
    }

    @UiThread
    private void setPageState(@PageState int state) {
//        if (!isFullScreenPage() && !contentWrapper.isList()) {
//            return;
//        }

        if (pageWrapper == null) return;
//            throw new IllegalArgumentException("you must use setPages() before change pages");
        if (this.currentPageState == state) return;
        View newPageView = pageWrapper.getView(this, state);
        if (pageView != null && pageView != newPageView)
            removeView(pageView);
        this.currentPageState = state;

        pageView = newPageView;
        if (pageView != null && pageView.getParent() == null) {
            addView(pageView);
            if (footerEnable
                    && isEnabled()
                    && !isFullScreenPage()
                    && contentWrapper.isList()) {
                pageView.getViewTreeObserver();
                pageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            pageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        if (correctContentPaddingBottom < 0) {
                            correctContentPaddingBottom = contentView.getPaddingBottom();
                        }
                        int newPaddingBottom = correctContentPaddingBottom + pageView.getMeasuredHeight();
                        if (newPaddingBottom > contentView.getPaddingBottom()) {
                            contentView.setPadding(contentView.getPaddingLeft(),
                                    contentView.getPaddingTop(),
                                    contentView.getPaddingRight(),
                                    newPaddingBottom);
                        }

                    }
                });
            }
        }
        if (pageView != null && !isFullScreenPage()) {
            movePageView(this.currentPageOffset);
            footerVisiblieWhenAnim = isFooterPage()
                    && pageView != null
                    && currentPageOffset < headerView.getMeasuredHeight();
        }


        switch (state) {
            case PageState.NONE:
                if (pageView != null) {
                    pageView.setVisibility(INVISIBLE);
                }
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

            case PageState.LOADING_FOOTER:
                if (pageView != null) {
                    pageView.setVisibility(VISIBLE);
                }
                break;

            default:
                contentView.setVisibility(VISIBLE);
                break;

        }

    }

    /**
     * 展示错误页面
     */
    @UiThread
    public void showErrorPage() {
        setPageState(PageState.ERROR);
    }

    /**
     * 展示错误footer
     */
    @UiThread
    public void showErrorFooter() {
        if (!footerEnable) {
            return;
        }
        setPageState(PageState.ERROR_FOOTER);
    }


    /**
     * 展示空数据页面
     */
    @UiThread
    public void showEmptyPage() {
        setPageState(PageState.EMPTY);
    }


    /**
     * 展示空数据的footer
     */
    @UiThread
    public void showEmptyFooter() {
        if (!footerEnable) {
            return;
        }
        setPageState(PageState.EMPTY_FOOTER);
    }

    /**
     * 展示没有更多的footer
     */
    @UiThread
    public void showNoMoreFooter() {
        if (!footerEnable) {
            return;
        }
        setPageState(PageState.NO_MORE_FOOTER);
    }

    /**
     * 加载更多开始或结束
     */
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

    private boolean isFooterPage() {
        return contentWrapper.isList()
                && (currentPageState == PageState.EMPTY_FOOTER
                || currentPageState == PageState.ERROR_FOOTER
                || currentPageState == PageState.LOADING_FOOTER
                || currentPageState == PageState.NO_MORE_FOOTER);
    }

    /**
     * 下拉刷新开始或结束
     */
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
            post(() -> onRefreshListener.onRefresh());
        }

        if (!refreshing) {
            boolean isFinish = false;
            if (isFullScreenPage()) {
                this.refreshing = false;
                isFinish = true;
            }
            setPageState(PageState.NONE);
            if (isFinish) return;
        }

        if (isFullScreenPage()) {
            this.refreshing = refreshing;
            return;
        }

        if (refreshing) {
            this.refreshing = true;
            post(() -> {
                isAnimTouchTrigger = false;
                showEnterRefreshAnim();
            });
        } else {
            setHeaderState(RefreshHeaderState.REFRESH_COMPLETED);
            postDelayed(() -> {
                SmoothRefreshLayout.this.refreshing = false;
                if (contentWrapper.topChildIsFirstItem()) {
                    showExitRefreshAnim();
                } else {
                    onExitRefreshAnimEnd();
                }
            }, DEFAULT_ANIMATOR_DURATION);
        }
    }

    //展开刷新header的动画
    private void showEnterRefreshAnim() {

        refreshing = true;
        animatorRunning = true;
        isExpandingAnimator = true;
        headerView.setVisibility(VISIBLE);
        if (refreshAnimator != null) {
            refreshAnimator.cancel();
        }
        setHeaderState(RefreshHeaderState.REFRESHING);
        refreshAnimatorParams(contentWrapper.getTopOffset(), contentRefreshingOffset);
        refreshAnimator.start();
    }

    private void onEnterRefreshAnimEnd() {
        isLoadMore = false;
        setPageState(PageState.NONE);
        if (contentWrapper.isList()) {
            moveContentView(contentMinOffset, false);
            contentView.setPadding(contentView.getPaddingLeft(), contentRefreshingOffset, contentView.getPaddingRight(), contentView.getPaddingBottom());
            contentWrapper.scrollVerticalBy(contentMinOffset - contentRefreshingOffset);
        } else {
            moveContentView(contentRefreshingOffset);
        }
        moveHeaderView(headerRefreshingOffset);
        animatorRunning = false;
        refreshing = true;
        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
        if (!isAnimTouchTrigger) {
            contentWrapper.smoothScrollVerticalToTop();
        }
    }

    //松手时返回顶部的动画
    private void showExitRefreshAnim() {
        animatorRunning = true;
        isExpandingAnimator = false;
        if (refreshAnimator != null) {
            refreshAnimator.cancel();
        }
        refreshAnimatorParams(headerView.getBottom(), contentMinOffset);
        refreshAnimator.start();
    }

    private void onExitRefreshAnimEnd() {
        moveContentView(contentMinOffset);
        if (contentWrapper.isList()) {
            contentView.setPadding(contentView.getPaddingLeft(),
                    contentMinOffset,
                    contentView.getPaddingRight(),
                    contentView.getPaddingBottom());
        }
        moveHeaderView(headerMinOffset);
        animatorRunning = false;
        refreshing = false;
        headerView.setVisibility(INVISIBLE);
        currentRefreshState = RefreshHeaderState.NONE;

    }

    private void refreshAnimatorParams(final int startValue, final int endValue) {
        this.startAnimValue = startValue;
        this.endAnimValue = endValue;
        refreshAnimator.setIntValues(startValue, endValue);

        int duration;
        if (startValue < endValue) {
            duration = DEFAULT_ANIMATOR_DURATION;
        } else {
            duration = Math.min((int) (((float) startValue - contentMinOffset) / (headerRefreshingOffset - headerMinOffset) * DEFAULT_ANIMATOR_DURATION),
                    DEFAULT_ANIMATOR_DURATION);
        }
        refreshAnimator.setDuration(duration);
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
        if (refreshAnimator != null && refreshAnimator.isRunning()) {
            refreshAnimator.cancel();
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

        if (!isBeingDragged) {
            dispatchNestedPreScroll(dx, dy, parentConsumed, null);
        }
        int newDy = dy - parentConsumed[1];
        if (parentConsumed[1] != 0) {
            consumed[1] = newDy;
            return;
        }

        if (!isFullScreenPage() && isEnabled() && !refreshing && !animatorRunning) {
            if (newDy < 0 && !canChildScrollUp()) {
                handleTouchActionMove(-newDy);
                isBeingDragged = true;

            } else if (newDy > 0 && isBeingDragged) {
                handleTouchActionMove(-newDy);
                if (currentHeaderOffset <= headerMinOffset) {
                    isBeingDragged = false;
                }
            }
        }

        if (!refreshing && newDy > 0 && isHeaderVisible()) {
            consumed[1] = newDy;
        }

        if (!contentWrapper.isList()) {
            if (refreshing) {
                int result = contentView.getTop() - newDy;
                if (result > contentRefreshingOffset) result = contentRefreshingOffset;
                if (result < contentMinOffset) result = contentMinOffset;
                if (newDy > 0 || !canChildScrollUp()) {
                    moveContentView(result, false);
                    moveHeaderView(computeHeaderTopByContentTop(result));
                }

            }
            if (isHeaderVisible() && dy > 0) {
                consumed[1] = newDy;
            }
        }


    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (!refreshing || (dyUnconsumed < 0 && isHeaderFullVisible())) {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
        }
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (velocityY > 0 && !canChildScrollDown()) {
            return true;
        }
        if (!contentWrapper.isList() && isHeaderVisible()) {
            return true;
        }
        return !refreshing && isHeaderVisible();
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (velocityY > 0 && !canChildScrollDown()) {
            return true;
        }
        return false;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        parentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View child) {
        parentHelper.onStopNestedScroll(child);
        if (isBeingDragged) {
            handleTouchActionUp();
        }
        isBeingDragged = false;
        stopNestedScroll();
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        childHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return childHelper.isNestedScrollingEnabled();
    }


    @Override
    public boolean startNestedScroll(@ViewCompat.ScrollAxis int axes) {
        return childHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        childHelper.stopNestedScroll();
    }


    @Override
    public boolean hasNestedScrollingParent() {
        return childHelper.hasNestedScrollingParent();
    }


    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }


    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed,
                                           @Nullable int[] offsetInWindow) {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }


    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return childHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return childHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
