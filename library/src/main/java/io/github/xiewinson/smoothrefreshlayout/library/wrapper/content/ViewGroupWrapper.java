package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnViewGroupScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public abstract class ViewGroupWrapper implements IViewGroupWrapper {
    private ViewGroup viewGroup;
    private OnViewGroupScrollListener onViewGroupScrollListener;

    public ViewGroupWrapper(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
        viewGroup.setClipToPadding(false);
    }

    @Override
    public void setViewGroupScrollListener(OnViewGroupScrollListener onViewGroupScrollListener) {
        this.onViewGroupScrollListener = onViewGroupScrollListener;
    }

    @Override
    public void removeViewGroupScrollListener() {
        this.onViewGroupScrollListener = null;
    }

    public static class Factory {
        public static IViewGroupWrapper getInstance(View viewGroup) {
            if (viewGroup instanceof RecyclerView) {
                return new RecyclerViewWrapper((RecyclerView) viewGroup);
            } else if (viewGroup instanceof NestedScrollView) {
                return new NestedScrollViewWrapper((NestedScrollView) viewGroup);
            }
            return null;
        }
    }

    @Override
    public void scrollBy(int dx, int dy) {
        viewGroup.scrollBy(dx, dy);
    }
}
