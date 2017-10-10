package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/7.
 * 更改paddingTop的方式在ScrollView上会有跳动，不适合
 * @hide
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
                    onViewGroupScrollListener.onScroll(-scrollY);
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
        return true;
    }

}
