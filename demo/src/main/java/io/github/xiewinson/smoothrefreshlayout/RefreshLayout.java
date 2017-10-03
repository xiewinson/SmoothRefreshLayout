/*
 * Copyright (c) 2017. Istuary Innovation Group, Ltd.
 *  Unauthorized copying of this file, via any medium is strictly prohibited proprietary and
 *  confidential.
 *  Created on 星期三, 11 一月 2017 10:29:06 +0800.
 *  ProjectName: ironhide-android; ModuleName: IronHideLibrary; ClassName: RefreshLayout.java.
 *  Author: Lena; Last Modified: 星期三, 11 一月 2017 10:29:06 +0800.
 *  This file is originally created by winson.
 */

package io.github.xiewinson.smoothrefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefreshlayout.library.DeviceUtil;

/**
 * Created by winson on 2017/1/11.
 */

public class RefreshLayout extends FrameLayout {

    private LinearLayout headerView;
    private RecyclerView contentView;
    private TextView titleTv;
    private int headerHeight;
    private int maxHeaderY;
    private int minHeaderY;

    private float lastY;

    private boolean refreshing;
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

    RecyclerView.OnScrollListener onScrollListener;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setLongClickable(true);
        addRefreshView(getContext());
        contentView = (RecyclerView) getChildAt(1);
        contentView.setClipToPadding(false);


        onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View childAt = recyclerView.getChildAt(0);
                if (childAt != null && refreshing) {
                    if (recyclerView.getChildAdapterPosition(childAt) == 0) {
                        headerView.setTranslationY(childAt.getY() - headerHeight);
                    } else {
                        headerView.setTranslationY(-headerHeight);

                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        };
        ((RecyclerView) contentView).addOnScrollListener(onScrollListener);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animatorSet != null && animatorSet.isRunning()) {
            animatorSet.cancel();
        }
        onRefreshListener = null;
        if (contentView != null) {
            contentView.removeOnScrollListener(onScrollListener);
        }
    }

    private void addRefreshView(Context context) {
        headerView = new LinearLayout(context);
        headerView.setBackgroundColor(Color.BLUE);
        headerView.setGravity(Gravity.CENTER);

        ImageView iv = new ImageView(context);
        int size = DeviceUtil.getPxByDp(context, 24);
        headerView.addView(iv, new LinearLayout.LayoutParams(size, size));

        titleTv = new TextView(context);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        titleTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tvParams.leftMargin = DeviceUtil.getPxByDp(context, 8);
        headerView.addView(titleTv, tvParams);

        headerView.setPadding(size, size, size, size);
        addView(headerView, 0, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                headerHeight = headerView.getMeasuredHeight();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    headerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    headerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                headerView.setY(-headerHeight);
                maxHeaderY = contentView.getPaddingTop();
                minHeaderY = maxHeaderY - headerHeight;
            }
        });
        requestLayout();
        headerView.setVisibility(INVISIBLE);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled() || refreshing || animatorRunning) {
            return super.dispatchTouchEvent(ev);

        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastY = ev.getY();
                maxHeaderY = contentView.getPaddingTop();
                minHeaderY = maxHeaderY - headerHeight;
                headerView.setY(minHeaderY);
                break;

            case MotionEvent.ACTION_MOVE:
                float currentY = ev.getY();
                float dy = currentY - lastY;
                //下拉
                if (dy > 0) {
                    if (!canChildScrollUp() || contentView.getY() != 0) {
                        scrollHeader(dy);
                    }
                }
                //上拉
                else {
                    scrollHeader(dy);
                }

                lastY = currentY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isTriggerChildTouch) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                }
                reset();
                adjustAnimator();

                isTriggerChildTouch = false;
                return super.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void adjustAnimator() {
        float translationY = headerView.getY();
        if (translationY >= maxHeaderY - GAP_DISTANCE) {
            expandHeaderAnimator(0);
        } else {
            setRefreshTitle(TIPS_PULL_TO_REFRESH);
            collaspHeaderAnimator();
        }
    }

    private boolean canChildScrollUp() {
//        View firstChild = contentView.getChildAt(0);
//        if(firstChild ==null) {
//            return true;
//        }
//        int childAdapterPosition = contentView.getChildAdapterPosition(firstChild);
//        if(childAdapterPosition != 0) {
//            return true;
//        }
//
//        if(contentView.getPaddingTop() == firstChild.getY()) {
//            return false;
//        }
//        return true;

        return contentView != null && contentView.canScrollVertically(-1);
    }


    private void reset() {
        isTriggerChildTouch = false;
    }

    private void scrollHeader(float dy) {
        headerView.setVisibility(VISIBLE);
        if (dy > 0) {
            dy *= 0.5f;
        }
        float result = headerView.getY() + dy;

        if (result < minHeaderY) {
            result = minHeaderY;
        }
        if (result > maxHeaderY) {
            result = maxHeaderY;
        }

        if (result >= maxHeaderY - GAP_DISTANCE) {
            setRefreshTitle(TIPS_RELEASE_TO_REFRESH);
        } else {
            setRefreshTitle(TIPS_PULL_TO_REFRESH);
        }

        if (headerView.getY() != result) {
            headerView.setY(result);
            contentView.setPadding(contentView.getPaddingLeft(), (int) (result + headerHeight), contentView.getPaddingLeft(), contentView.getPaddingRight());
            isTriggerChildTouch = true;
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
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
                    RefreshLayout.this.refreshing = false;
                    setRefreshTitle(TIPS_REFRESH_COMPLETED);
                    collaspHeaderAnimator();
                }
            }, DEFAULT_ANIMATOR_DURATION);
        }
    }

    //展开刷新header的动画
    private void expandHeaderAnimator(int duration) {
        headerView.setVisibility(VISIBLE);
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        setRefreshTitle(TIPS_REFRESHING);
        animatorSet = new AnimatorSet();

        animatorSet.playTogether(
                ObjectAnimator.ofFloat(headerView, "y", maxHeaderY),
                getContentViewPaddingTopAnimator(maxHeaderY + headerHeight, true)
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

        contentView.setPadding(contentView.getPaddingLeft(), (int) (maxHeaderY + headerHeight), contentView.getPaddingRight(), contentView.getPaddingBottom());

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
                ObjectAnimator.ofFloat(headerView, "y", minHeaderY),
                getContentViewPaddingTopAnimator(maxHeaderY, false)
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
        contentView.setPadding(contentView.getPaddingLeft(), maxHeaderY, contentView.getPaddingRight(), contentView.getPaddingBottom());
        headerView.setY(minHeaderY);
        animatorRunning = false;
        refreshing = false;
        headerView.setVisibility(INVISIBLE);
    }

    @NonNull
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
//        int min = max - headerHeight;
//
//        animatorSet.playTogether(
//                getContentViewPaddingTopAnimator(min),
//                ObjectAnimator.ofFloat(headerView, "y", headerView.getY(), min - headerHeight)
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
        if (titleTv != null) {
            titleTv.setText(title);
        }
    }
}
