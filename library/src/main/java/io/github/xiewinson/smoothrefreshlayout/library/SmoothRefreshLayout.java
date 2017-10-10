package io.github.xiewinson.smoothrefreshlayout.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.content.ContentViewWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.content.IContentViewWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.IRefreshHeaderWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.RefreshHeaderWrapper;

/**
 * Created by winson on 2017/10/3.
 */

public class SmoothRefreshLayout extends FrameLayout {

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
    private int refreshingHeaderY;
    private int minRefreshHeaderY;
    private int maxRefreshHeaderY;
    private int correctOverScrollMode;
    private boolean isEnterRefresh;

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
                if (refreshHeaderView != null && refreshing) {
                    refreshHeaderView.setY(firstItemY - refreshHeaderHeight);
                }
            }

            @Override
            public void onScroll(int offset) {
                if (refreshHeaderView != null && refreshing) {
                    refreshHeaderView.setY(offset);
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
        refreshingHeaderY = contentView.getPaddingTop();
        minRefreshHeaderY = refreshingHeaderY - refreshHeaderHeight;
        maxRefreshHeaderY = refreshingHeaderY + refreshHeaderHeight * 3;
        refreshHeaderView.setY(minRefreshHeaderY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (!isEnabled() || refreshing || animatorRunning || (ev.getAction() != MotionEvent.ACTION_DOWN && lastRefreshHeaderY < 0)) {
            return super.dispatchTouchEvent(ev);
        }


        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastRefreshHeaderY = ev.getY();
                correctOverScrollMode = contentView.getOverScrollMode();
                initRefreshHeaderParams();
                break;

            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - lastRefreshHeaderY;
                //下拉
                if (dy > 0) {
                    if (!canChildScrollUp()
                            && refreshHeaderView.getY() != maxRefreshHeaderY
                            && contentView.getPaddingTop() != maxRefreshHeaderY + refreshHeaderHeight) {
                        contentView.setOverScrollMode(OVER_SCROLL_NEVER);
                        handleTouchActionMove(dy);
                    }
                }
                //上拉
                else {
                    if (contentView.getPaddingTop() != refreshingHeaderY
                            || refreshHeaderView.getY() != minRefreshHeaderY) {
                        handleTouchActionMove(dy);

                    }
                }
                lastRefreshHeaderY = currentY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (handleTouchActionUp()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                contentView.setOverScrollMode(correctOverScrollMode);
                lastRefreshHeaderY = -1;
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

    /**
     * @return 若返回true，则将变为刷新状态
     */
    private boolean handleTouchActionUp() {
        float currentY = refreshHeaderView.getY();
        if (currentY >= refreshingHeaderY || currentRefreshState == RefreshHeaderState.RELEASE_TO_REFRESH) {
            isEnterRefresh = true;
            expandRefreshHeader(true);
            return true;
        } else if (currentY < refreshingHeaderY && currentY > minRefreshHeaderY) {
            isEnterRefresh = true;
            setState(RefreshHeaderState.PULL_TO_REFRESH);
            collapseRefreshHeader();
        }
        isEnterRefresh = false;
        return false;
    }

    private boolean canChildScrollUp() {
        return contentView != null && contentView.canScrollVertically(-1);
    }

    private void handleTouchActionMove(float dy) {
        isEnterRefresh = true;
        if (dy > 0) {
            dy *= 0.3f;
        }
        if (dy > 0 && dy < 1) {
            dy = 1;
        } else if (dy > -1 && dy < 0) {
            dy = -1;
        }
        int result = (int) (refreshHeaderView.getY() + dy);

        if (result <= minRefreshHeaderY) {
            refreshHeaderView.setVisibility(INVISIBLE);
            result = minRefreshHeaderY;
        } else {
            refreshHeaderView.setVisibility(VISIBLE);
        }

        result = Math.min(result, maxRefreshHeaderY);

        if (result >= refreshingHeaderY) {
            setState(RefreshHeaderState.RELEASE_TO_REFRESH);
        } else {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
        }

        if (refreshHeaderView.getY() != result) {
            moveViews(result);
            contentWrapper.scrollVerticalBy((int) dy);
        }

    }

    private float moveViews(int value) {
        refreshHeaderView.setY(value);
        contentView.setPadding(contentView.getPaddingLeft(),
                value + refreshHeaderHeight,
                contentView.getPaddingRight(),
                contentView.getPaddingBottom());
        float ratio = (Math.abs(value - minRefreshHeaderY)) / (float) (refreshingHeaderY - minRefreshHeaderY);
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

        refreshHeaderAnimator = getRefreshHeaderAnimator((int) refreshHeaderView.getY(), refreshingHeaderY);

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
        moveViews(refreshingHeaderY);
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
        refreshHeaderAnimator = getRefreshHeaderAnimator((int) refreshHeaderView.getY(), minRefreshHeaderY);
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
        moveViews(minRefreshHeaderY);
        animatorRunning = false;
        refreshing = false;
        refreshHeaderView.setVisibility(INVISIBLE);
        currentRefreshState = RefreshHeaderState.NONE;

    }

    private static final int ANIMATOR_EXPAND = 0;
    private static final int ANIMATOR_COLLAPSE = 1;


    private ValueAnimator getRefreshHeaderAnimator(final int startValue, int endValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startValue, endValue);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final int newValue = (int) animation.getAnimatedValue();
                final int oldValue = (int) refreshHeaderView.getY();
                moveViews(newValue);
                refreshHeaderView.setY(newValue);
                contentWrapper.scrollVerticalBy(oldValue - newValue);
            }
        });

        int duration;
        if (startValue < endValue) {
            duration = DEFAULT_ANIMATOR_DURATION;
        } else {
            duration = Math.min((int) (((float) startValue - minRefreshHeaderY) / refreshHeaderHeight * DEFAULT_ANIMATOR_DURATION),
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
}
