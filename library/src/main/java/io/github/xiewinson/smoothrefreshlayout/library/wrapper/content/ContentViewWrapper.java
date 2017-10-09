package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public abstract class ContentViewWrapper implements IContentViewWrapper {
    private ViewGroup viewGroup;
    private OnContentViewScrollListener onViewGroupScrollListener;

    public ContentViewWrapper(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
        viewGroup.setClipToPadding(false);
    }

    @Override
    public void setViewGroupScrollListener(OnContentViewScrollListener onViewGroupScrollListener) {
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
            }
            else if (viewGroup instanceof NestedScrollView) {
                return new NestedScrollViewWrapper((NestedScrollView) viewGroup);
            }
            return null;
        }
    }

    @Override
    public void scrollByWhenRefreshHeaderExpand(int dx, int dy) {
    }

    @Override
    public void handleOnCollapseAnimartorEnd() {

    }
}
