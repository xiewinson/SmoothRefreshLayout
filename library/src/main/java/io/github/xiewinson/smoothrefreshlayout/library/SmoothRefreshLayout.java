package io.github.xiewinson.smoothrefreshlayout.library;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnViewGroupScrollListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.content.IViewGroupWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.content.ViewGroupWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.DefaultHeaderWrapper;
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

    private IViewGroupWrapper viewGroupWrapper;
    private IRefreshHeaderWrapper refreshHeaderWrapper;
    private View refreshHeaderView;
    private View contentView;
    private int refreshHeaderHeight;
    private int refreshingHeaderY;
    private int minRefreshHeaderY;
    private int maxRefreshHeaderY;
    private int correctOverScrollMode;

    private float lastRefreshHeaderY;

    private int currentRefreshState;

    //是否处于刷新
    private boolean refreshing;
    //是否正在执行动画
    private boolean animatorRunning = false;
    private boolean isInterceptChildTouch = false;

    private ValueAnimator refreshHeaderAnimator;

    private OnRefreshListener onRefreshListener;

    public static final int DEFAULT_ANIMATOR_DURATION = 300;

    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {

        contentView = getChildAt(0);
        viewGroupWrapper = ViewGroupWrapper.Factory.getInstance(contentView);
        viewGroupWrapper.setViewGroupScrollListener(new OnViewGroupScrollListener() {
            @Override
            public void onScroll(View topChild, boolean isFirst) {
                if (refreshHeaderView != null && topChild != null && refreshing) {
                    refreshHeaderView.setTranslationY(isFirst ? topChild.getY() - refreshHeaderHeight : -refreshHeaderHeight);
                }
            }
        });

        addRefreshHeaderView(new DefaultHeaderWrapper(getContext()));
    }

    public void addRefreshHeaderView(RefreshHeaderWrapper headerWrapper) {
        this.refreshHeaderWrapper = headerWrapper;
        this.refreshHeaderView = headerWrapper.getRefreshHeaderView();
        initRefreshHeaderView();
    }

    private void initRefreshHeaderView() {
        refreshHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    refreshHeaderView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    refreshHeaderView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                initRefreshHeaderParams();

            }
        });
        refreshHeaderView.setVisibility(INVISIBLE);
        addView(refreshHeaderView, 0, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    }

    private void initRefreshHeaderParams() {
        refreshHeaderHeight = refreshHeaderView.getMeasuredHeight();
        refreshingHeaderY = contentView.getPaddingTop();
        minRefreshHeaderY = refreshingHeaderY - refreshHeaderHeight;
        maxRefreshHeaderY = refreshingHeaderY + refreshHeaderHeight * 3;
        refreshHeaderView.setY(minRefreshHeaderY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (!isEnabled() || refreshing || animatorRunning) {
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
                        moveRefreshHeader(dy);
                    }
                }
                //上拉
                else {
                    if (contentView.getPaddingTop() != refreshingHeaderY
                            || refreshHeaderView.getY() != minRefreshHeaderY)
                        moveRefreshHeader(dy);
                }
                lastRefreshHeaderY = currentY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
//                if (isInterceptChildTouch && contentView.getPaddingTop() >= refreshingHeaderY + refreshHeaderHeight) {
//                    ev.setAction(MotionEvent.ACTION_CANCEL);
//                }
                isInterceptChildTouch = false;
                handleTouchActionUp();
                contentView.setOverScrollMode(correctOverScrollMode);
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

    private void handleTouchActionUp() {
        float translationY = refreshHeaderView.getY();
        if (translationY >= refreshingHeaderY) {
            expandRefreshHeader();
        } else if (translationY < refreshingHeaderY && translationY > minRefreshHeaderY) {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
            collaspRefreshHeader();
        }
    }

    private boolean canChildScrollUp() {
        return contentView != null && contentView.canScrollVertically(-1);
    }

    private int moveRefreshHeader(float dy) {
        refreshHeaderView.setVisibility(VISIBLE);
        if (dy > 0) {
            dy *= 0.3f;
        }
        int result = (int) (refreshHeaderView.getY() + dy);

        if (result < minRefreshHeaderY) {
            result = minRefreshHeaderY;
        }
        if (result > maxRefreshHeaderY) {
            result = maxRefreshHeaderY;
        }

        if (result >= refreshingHeaderY) {
            setState(RefreshHeaderState.RELEASE_TO_REFRESH);
        } else {
            setState(RefreshHeaderState.PULL_TO_REFRESH);
        }

        if (refreshHeaderView.getY() != result) {
            moveViews(result);
//            if(result < 0) {
//                contentView.scrollBy(0, (int) result);
//            }
            isInterceptChildTouch = true;
        }
        float offset = (Math.abs(result) - refreshingHeaderY) / (float) refreshHeaderHeight;
        offset = offset < 0 ? 0 : offset;
        offset = offset > 1 ? 1 : offset;
        refreshHeaderWrapper.onPullRefreshHeader(offset);
        return result;
    }

    private void moveViews(int value) {
        refreshHeaderView.setY(value);
        contentView.setPadding(contentView.getPaddingLeft(),
                value + refreshHeaderHeight,
                contentView.getPaddingLeft(),
                contentView.getPaddingRight());
    }

    public void setRefreshing(boolean refreshing) {

        //已经开始这个状态直接返回
        if (this.refreshing == refreshing || animatorRunning) {
            return;
        }

        if (refreshing) {
            this.refreshing = true;
            post(new Runnable() {
                @Override
                public void run() {
                    expandRefreshHeader();
                }
            });
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    SmoothRefreshLayout.this.refreshing = false;
                    setState(RefreshHeaderState.REFRESH_COMPLETED);
                    collaspRefreshHeader();
                }
            }, DEFAULT_ANIMATOR_DURATION);
        }
    }

    //展开刷新header的动画
    private void expandRefreshHeader() {
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
                onExpandAnimatorEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onExpandAnimatorEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        refreshHeaderAnimator.start();
    }

    private void onExpandAnimatorEnd() {
        animatorRunning = false;
        refreshing = true;

        contentView.setPadding(contentView.getPaddingLeft(),
                refreshingHeaderY + refreshHeaderHeight,
                contentView.getPaddingRight(),
                contentView.getPaddingBottom());

        if (onRefreshListener != null) {
            onRefreshListener.onRefresh();
        }
    }

    //松手时返回顶部的动画
    private void collaspRefreshHeader() {
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
                onCollaspAnimatorEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onCollaspAnimatorEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        refreshHeaderAnimator.start();
    }

    private void onCollaspAnimatorEnd() {
        contentView.setPadding(contentView.getPaddingLeft(), refreshingHeaderY, contentView.getPaddingRight(), contentView.getPaddingBottom());
        refreshHeaderView.setY(minRefreshHeaderY);
        animatorRunning = false;
        refreshing = false;
        refreshHeaderView.setVisibility(INVISIBLE);
    }

    private ValueAnimator getRefreshHeaderAnimator(int startValue, int endValue) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startValue, endValue);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int newValue = (int) animation.getAnimatedValue();
                int oldValue = (int) refreshHeaderView.getY();
                moveViews(newValue);
                contentView.scrollBy(0, oldValue - newValue);
            }
        });

//        if ((startValue < refreshingHeaderY && startValue > minRefreshHeaderY)
//                || (startValue > refreshingHeaderY && startValue < refreshingHeaderY + refreshHeaderHeight)) {
//            valueAnimator.setDuration(100);
//        } else {
//            valueAnimator.setDuration(DEFAULT_ANIMATOR_DURATION);
//        }
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
        if (state != currentRefreshState && refreshHeaderWrapper != null) {
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
        if (viewGroupWrapper != null) {
            viewGroupWrapper.removeViewGroupScrollListener();
        }
    }


}
