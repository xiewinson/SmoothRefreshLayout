package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.support.v4.view.NestedScrollingChild;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.github.xiewinson.smoothrefresh.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public abstract class ContentViewWrapper implements IContentViewWrapper {
    private ViewGroup viewGroup;
    private OnContentViewScrollListener onViewGroupScrollListener;

    public ContentViewWrapper(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
    }

    @Override
    public void setContentViewScrollListener(OnContentViewScrollListener onViewGroupScrollListener) {
        this.onViewGroupScrollListener = onViewGroupScrollListener;
    }

    @Override
    public void removeContentViewScrollListener() {
        this.onViewGroupScrollListener = null;
    }

    public static class Factory {
        public static IContentViewWrapper getInstance(View viewGroup) {
            if (viewGroup instanceof RecyclerView) {
                return new RecyclerViewWrapper((RecyclerView) viewGroup);
            } else if (viewGroup instanceof ListView) {
                return new ListViewWrapper((ListView) viewGroup);
            }
            throw new IllegalArgumentException("view must be recyclerView or listView");
        }
    }

    @Override
    public void scrollVerticalBy(int dy) {
        viewGroup.scrollBy(0, dy);
    }

    @Override
    public void smoothScrollVerticalToTop() {
    }

    @Override
    public boolean isSupportNestedScroll() {
        return this.viewGroup instanceof NestedScrollingChild;
    }
}
