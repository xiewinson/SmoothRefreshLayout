package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.listener.OnListScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public class RecyclerViewWrapper extends ListWrapper {
    private RecyclerView recyclerView;
    private RecyclerView.OnScrollListener onScrollListener;


    public RecyclerViewWrapper(RecyclerView recyclerView) {
        super(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void setOnListScrollListener(final OnListScrollListener onListScrollListener) {
        super.setOnListScrollListener(onListScrollListener);
        onScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (onListScrollListener != null) {

                    View topChild = recyclerView.getChildAt(0);
                    onListScrollListener.onFirstItemScroll((topChild == null || !topChildIsFirstItem()) ? 0 : topChild.getTop());

                    View bottomChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
                    int pos = recyclerView.getChildAdapterPosition(bottomChild);
                    if (pos >= recyclerView.getAdapter().getItemCount() - 1) {
                        onListScrollListener.onReachBottom();
                    }
                    onListScrollListener.onBottomItemScroll((int) (bottomChild.getY() + bottomChild.getMeasuredHeight()));

                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    View bottomChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
//                    int pos = recyclerView.getChildAdapterPosition(bottomChild);
//                    if (pos >= recyclerView.getAdapter().getItemCount() - 1) {
//                        onListScrollListener.onReachBottom();
//                        onListScrollListener.onBottomItemScroll((int) (bottomChild.getY() + bottomChild.getMeasuredHeight()));
//                    }
//                }


            }
        };
        recyclerView.addOnScrollListener(onScrollListener);
    }

    @Override
    public void recycle() {
        super.recycle();
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

    public boolean bottomChildIsLastItem() {
        View child = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
        return child != null && recyclerView.getChildAdapterPosition(child) == recyclerView.getAdapter().getItemCount() - 1;
    }

    @Override
    public boolean hasListItemChild() {
        return recyclerView.getAdapter().getItemCount() != 0;
    }
}
