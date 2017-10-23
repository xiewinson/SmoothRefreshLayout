package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.support.v4.view.NestedScrollingChild;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.github.xiewinson.smoothrefresh.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public class ContentViewWrapper implements IContentViewWrapper {
    private ViewGroup viewGroup;
    private OnContentViewScrollListener onViewGroupScrollListener;

    public ContentViewWrapper(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
    }

    public static class Factory {
        public static IContentViewWrapper getInstance(View viewGroup) {
            if (viewGroup instanceof RecyclerView) {
                return new RecyclerViewWrapper((RecyclerView) viewGroup);
            } else if (viewGroup instanceof ListView) {
                return new ListViewWrapper((ListView) viewGroup);
            } else if (viewGroup instanceof NestedScrollView) {
                return new ContentViewWrapper((ViewGroup) viewGroup);
            }
            throw new IllegalArgumentException("view not be supported");
        }
    }

    @Override
    public void smoothScrollVerticalToTop() {
    }

    @Override
    public boolean isSupportNestedScroll() {
        return this.viewGroup instanceof NestedScrollingChild;
    }

    @Override
    public void layout(int top) {
        viewGroup.layout(viewGroup.getLeft(), top, viewGroup.getRight(), viewGroup.getBottom());
    }

    @Override
    public int getTopOffset() {
        return viewGroup.getTop();
    }

    @Override
    public boolean topChildIsFirstItem() {
        return true;
    }

    @Override
    public void recycle() {

    }

    @Override
    public boolean isList() {
        return false;
    }
}
