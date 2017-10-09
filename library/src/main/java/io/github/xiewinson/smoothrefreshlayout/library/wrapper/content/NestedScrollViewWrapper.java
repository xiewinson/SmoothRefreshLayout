package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;

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
        return true;
    }

    @Override
    public void scrollToTop() {
        scrollView.smoothScrollTo(0, 0);
    }

    @Override
    public void handleOnCollapseAnimartorEnd() {
//        try {
//            Field field = View.class.getDeclaredField("mScrollY");
//            field.setAccessible(true);
//            field.setInt(scrollView, 0);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
        scrollView.scrollTo(0, 0);
    }
}
