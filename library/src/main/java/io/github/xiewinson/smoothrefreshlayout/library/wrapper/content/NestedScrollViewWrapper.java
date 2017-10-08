package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/7.
 */

public class NestedScrollViewWrapper extends ContentViewWrapper {

    private NestedScrollView scrollView;

    public NestedScrollViewWrapper(NestedScrollView scrollView) {
        super(scrollView);
        this.scrollView = scrollView;
    }

    @Override
    public void setViewGroupScrollListener(final OnContentViewScrollListener onViewGroupScrollListener) {
        super.setViewGroupScrollListener(onViewGroupScrollListener);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (onViewGroupScrollListener != null) {
                    onViewGroupScrollListener.onScrollRelative(oldScrollY - scrollY);
                }
            }
        });
    }

    @Override
    public void removeContentViewScrollListener() {
        super.removeContentViewScrollListener();
    }

    @Override
    public boolean topChildIsFirstItem() {
        View parent = scrollView.getChildAt(0);
        if (parent instanceof ViewGroup) {
            View child = ((ViewGroup) parent).getChildAt(0);
            return child != null && scrollView.getScrollY() < child.getHeight();

        }
        return false;
    }

    /**
     * 在NestedScrollView上进行收缩动画时使用scrollBy会错位
     *
     * @param dx
     * @param dy
     */
    @Override
    public final void scrollBy(int dx, int dy) {

    }

    @Override
    public void scrollToTop() {
        scrollView.smoothScrollTo(0, 0);
    }
}
