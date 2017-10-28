package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.widget.AbsListView;
import android.widget.ListView;

import io.github.xiewinson.smoothrefresh.library.listener.OnContentViewScrollListener;

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
    public void setContentViewScrollListener(final OnContentViewScrollListener onContentViewScrollListener) {
        super.setContentViewScrollListener(onContentViewScrollListener);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (onContentViewScrollListener != null && firstVisibleItem == 0 && listView.getChildAt(0) != null) {
                    onContentViewScrollListener.onFirstItemScroll((int) listView.getChildAt(0).getY());
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
