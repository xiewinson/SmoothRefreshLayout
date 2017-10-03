package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnViewGroupScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public abstract class ViewGroupWrapper implements IViewGroupWrapper {
    private OnViewGroupScrollListener onViewGroupScrollListener;

    public ViewGroupWrapper(ViewGroup viewGroup) {
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
            }
            return null;
        }
    }
}
