package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import io.github.xiewinson.smoothrefresh.library.listener.OnListScrollListener;

/**
 * Created by winson on 2017/10/10.
 */

public class ListViewWrapper extends ListWrapper {

    private ListView listView;

    public ListViewWrapper(ListView listView) {
        super(listView);
        this.listView = listView;
    }

    @Override
    public void setOnListScrollListener(final OnListScrollListener onListScrollListener) {
        super.setOnListScrollListener(onListScrollListener);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE && listView.getLastVisiblePosition() == listView.getCount() - 1) {
                    onListScrollListener.onReachBottom();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (onListScrollListener != null) {

                    View topChild = listView.getChildAt(0);
                    onListScrollListener.onFirstItemScroll(firstVisibleItem == 0 && topChild != null ? topChild.getTop() : 0);

                    View bottomChild = listView.getChildAt(listView.getChildCount() - 1);
                    onListScrollListener.onBottomItemScroll(listView.getLastVisiblePosition() == totalItemCount - 1
                            && bottomChild != null ? bottomChild.getBottom() : listView.getBottom());
                }
            }
        });

    }

    @Override
    public void recycle() {
        super.recycle();
        listView.setOnScrollListener(null);
    }

    @Override
    public boolean topChildIsFirstItem() {
        return listView.getFirstVisiblePosition() == 0;
    }

    @Override
    public void smoothScrollVerticalToTop() {
        listView.smoothScrollToPosition(0);
    }

    @Override
    public boolean hasListItemChild() {
        return !listView.getAdapter().isEmpty();
    }
}
