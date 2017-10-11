package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public class RecyclerViewWrapper extends ContentViewWrapper {
    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener onScrollListener;

    public RecyclerViewWrapper(RecyclerView recyclerView) {
        super(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void setContentViewScrollListener(final OnContentViewScrollListener onViewGroupScrollListener) {
        super.setContentViewScrollListener(onViewGroupScrollListener);
        onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (onViewGroupScrollListener != null) {
                    View topChild = recyclerView.getChildAt(0);
                    onViewGroupScrollListener.onFirstItemScroll((topChild == null || !topChildIsFirstItem()) ? 0 : (int) topChild.getY());
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        };
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void removeContentViewScrollListener() {
        super.removeContentViewScrollListener();
        recyclerView.removeOnScrollListener(onScrollListener);
    }

    @Override
    public void smoothScrollVerticalToTop() {
        super.smoothScrollVerticalToTop();
//        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public boolean topChildIsFirstItem() {
        View child = recyclerView.getChildAt(0);
        return child != null && recyclerView.getChildAdapterPosition(child) == 0;
    }
}
