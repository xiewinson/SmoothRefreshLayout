package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;
import android.util.Log;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnViewGroupScrollListener;

/**
 * Created by winson on 2017/10/7.
 */

public class NestedScrollViewWrapper extends ViewGroupWrapper {

    private NestedScrollView scrollView;

    public NestedScrollViewWrapper(NestedScrollView scrollView) {
        super(scrollView);
        this.scrollView = scrollView;
    }

    @Override
    public void setViewGroupScrollListener(final OnViewGroupScrollListener onViewGroupScrollListener) {
        super.setViewGroupScrollListener(onViewGroupScrollListener);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (onViewGroupScrollListener != null) {
                    onViewGroupScrollListener.onScroll(v.getChildAt(0), true);
                }
            }
        });
    }

    @Override
    public void removeViewGroupScrollListener() {
        super.removeViewGroupScrollListener();
    }

    @Override
    public boolean firstChildIsFirstItem() {
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
}
