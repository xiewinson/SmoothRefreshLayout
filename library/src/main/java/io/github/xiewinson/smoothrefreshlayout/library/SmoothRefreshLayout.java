package io.github.xiewinson.smoothrefreshlayout.library;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnViewGroupScrollListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.content.IViewGroupWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.content.ViewGroupWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.DefaultHeaderWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.IHeaderWrapper;

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
    private IHeaderWrapper refreshHeaderWrapper;
    private View refreshHeaderView;
    private View contentView;
    private int refreshHeaderHeight;
    private int refreshingHeaderY;
    private int minRefreshHeaderY;

    private float lastY;

    //是否处于刷新
    private boolean refreshing;
    //是否正在执行动画
    private boolean animatorRunning = false;
    private boolean isTriggerChildTouch = false;

    private AnimatorSet animatorSet;

    private OnRefreshListener onRefreshListener;

    public static final String TIPS_REFRESHING = "正在刷新";
    public static final String TIPS_PULL_TO_REFRESH = "下拉刷新";
    public static final String TIPS_RELEASE_TO_REFRESH = "放开刷新";
    private static final String TIPS_REFRESH_COMPLETED = "刷新完成";

    public static final int GAP_DISTANCE = 10;

    public static final int DEFAULT_ANIMATOR_DURATION = 300;

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
                if (topChild != null && refreshing) {
                    refreshHeaderView.setTranslationY(isFirst ? topChild.getY() - refreshHeaderHeight : -refreshHeaderHeight);
                }
            }
        });

        //如果用户没有自定义HeaderWrapper，则配置DefaultHeaderWrapper
        post(new Runnable() {
            @Override
            public void run() {
                if (refreshHeaderWrapper == null) {
                    addRefreshHeaderView(new DefaultHeaderWrapper(getContext()));
                }
            }
        });
    }

    public void addRefreshHeaderView(IHeaderWrapper headerWrapper) {
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
        refreshHeaderView.setY(minRefreshHeaderY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (!isEnabled() || refreshing || animatorRunning) {
            return super.dispatchTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getY();
                initRefreshHeaderParams();
                break;

            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - lastY;
                //下拉
                if (dy > 0) {
                    if (!canChildScrollUp()
                            && refreshHeaderView.getY() != refreshingHeaderY
                            && contentView.getPaddingTop() != refreshingHeaderY + refreshHeaderHeight) {
                        scrollHeader(dy);
                    }
                }
                //上拉
                else {
                    if (contentView.getPaddingTop() != refreshingHeaderY
                            || refreshHeaderView.getY() != minRefreshHeaderY)
                        scrollHeader(dy);
                }
                lastY = currentY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isTriggerChildTouch) {
//                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                isTriggerChildTouch = false;
                adjustAnimator();

                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void adjustAnimator() {
        float translationY = refreshHeaderView.getY();
        if (translationY >= refreshingHeaderY - GAP_DISTANCE) {
            expandHeaderAnimator(0);
        } else {
            setRefreshTitle(TIPS_PULL_TO_REFRESH);
            collaspHeaderAnimator();
        }
    }

    private boolean canChildScrollUp() {
        return contentView != null && contentView.canScrollVertically(-1);
    }


    private void reset() {
        isTriggerChildTouch = false;
    }

    private int scrollHeader(float dy) {
        refreshHeaderView.setVisibility(VISIBLE);
        if (dy > 0) {
            dy *= 0.5f;
        }
        float result = refreshHeaderView.getY() + dy;

        if (result < minRefreshHeaderY) {
            result = minRefreshHeaderY;
        }
        if (result > refreshingHeaderY) {
            result = refreshingHeaderY;
        }

        if (result >= refreshingHeaderY - GAP_DISTANCE) {
            setRefreshTitle(TIPS_RELEASE_TO_REFRESH);
        } else {
            setRefreshTitle(TIPS_PULL_TO_REFRESH);
        }

        if (refreshHeaderView.getY() != result) {
            refreshHeaderView.setY(result);
            contentView.setPadding(contentView.getPaddingLeft(), (int) (result + refreshHeaderHeight), contentView.getPaddingLeft(), contentView.getPaddingRight());
//            if(result < 0) {
//                contentView.scrollBy(0, (int) result);
//            }
            isTriggerChildTouch = true;
        }
        return (int) result;
    }


    public OnRefreshListener getOnRefreshListener() {
        return onRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setRefreshing(boolean refreshing) {

        //已经开始这个状态直接返回
        if (this.refreshing == refreshing || animatorRunning) {
            return;
        }

        if (refreshing) {
            this.refreshing = true;
            expandHeaderAnimator(DEFAULT_ANIMATOR_DURATION);
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    SmoothRefreshLayout.this.refreshing = false;
                    setRefreshTitle(TIPS_REFRESH_COMPLETED);
                    collaspHeaderAnimator();
                }
            }, DEFAULT_ANIMATOR_DURATION);
        }
    }

    //展开刷新header的动画
    private void expandHeaderAnimator(int duration) {
        refreshHeaderView.setVisibility(VISIBLE);
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        setRefreshTitle(TIPS_REFRESHING);
        animatorSet = new AnimatorSet();

        animatorSet.playTogether(
                ObjectAnimator.ofFloat(refreshHeaderView, "y", refreshingHeaderY),
                getContentViewPaddingTopAnimator(refreshingHeaderY + refreshHeaderHeight, true)
        );
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animatorRunning = true;
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
        animatorSet.setDuration(duration);
        animatorSet.start();
    }

    private void onExpandAnimatorEnd() {
        animatorRunning = false;
        refreshing = true;

        contentView.setPadding(contentView.getPaddingLeft(), (int) (refreshingHeaderY + refreshHeaderHeight), contentView.getPaddingRight(), contentView.getPaddingBottom());

        if (onRefreshListener != null) {
//            postDelayed(new Runnable() {
//                @Override
//                public void run() {
            onRefreshListener.onRefresh();
//                }
//            }, 5000);
        }
    }

    //松手时返回顶部的动画
    private void collaspHeaderAnimator() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(refreshHeaderView, "y", minRefreshHeaderY),
                getContentViewPaddingTopAnimator(refreshingHeaderY, false)
        );
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                animatorRunning = true;
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
        animatorSet.start();
    }

    private void onCollaspAnimatorEnd() {
        contentView.setPadding(contentView.getPaddingLeft(), refreshingHeaderY, contentView.getPaddingRight(), contentView.getPaddingBottom());
        refreshHeaderView.setY(minRefreshHeaderY);
        animatorRunning = false;
        refreshing = false;
        refreshHeaderView.setVisibility(INVISIBLE);
    }

    private ValueAnimator getContentViewPaddingTopAnimator(int endValue, final boolean scrollContentView) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(contentView.getPaddingTop(), endValue);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                int oldPaddingTop = contentView.getPaddingTop();
                contentView.setPadding(contentView.getPaddingLeft(), value, contentView.getPaddingRight(), contentView.getPaddingBottom());
                if (scrollContentView) {
                    contentView.scrollBy(0, -(value - oldPaddingTop));
                }
            }
        });
        return valueAnimator;
    }


    //完成刷新时的动画
//    private void collaspHeaderAnimator() {
//
//        if (animatorSet != null) {
//            animatorSet.cancel();
//        }
//        setRefreshTitle(TIPS_REFRESH_COMPLETED);
//        animatorSet = new AnimatorSet();
//        int max = contentView.getPaddingTop();
//        int min = max - refreshHeaderHeight;
//
//        animatorSet.playTogether(
//                getContentViewPaddingTopAnimator(min),
//                ObjectAnimator.ofFloat(refreshHeaderView, "y", refreshHeaderView.getY(), min - refreshHeaderHeight)
//        );
//        animatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                animatorRunning = true;
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                onCollaspAnimatorEnd(min);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                onCollaspAnimatorEnd(min);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        animatorSet.start();
//    }


    public void setRefresh(boolean refreshing) {
        setRefreshing(refreshing);
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    private void setRefreshTitle(String title) {
//        if (titleTv != null) {
//            titleTv.setText(title);
//        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        onRefreshListener = null;
        if (viewGroupWrapper != null) {
            viewGroupWrapper.removeViewGroupScrollListener();
        }
    }


}
