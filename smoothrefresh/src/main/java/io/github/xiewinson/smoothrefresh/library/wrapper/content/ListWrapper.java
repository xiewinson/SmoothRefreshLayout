package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.listener.OnListScrollListener;

/**
 * Created by winson on 2017/10/23.
 */

public class ListWrapper extends ContentViewWrapper implements IListWrapper {
    private ViewGroup viewGroup;
    private OnListScrollListener onListScrollListener;

    public ListWrapper(ViewGroup viewGroup) {
        super(viewGroup);
        this.viewGroup = viewGroup;
        viewGroup.setMotionEventSplittingEnabled(false);
        viewGroup.setClipToPadding(false);
    }

    @Override
    public boolean topChildIsFirstItem() {
        return false;
    }

    @Override
    public void moveContentView(int top) {
        super.moveContentView(top);
//        viewGroup.setPadding(viewGroup.getPaddingLeft(),
//                top,
//                viewGroup.getPaddingRight(),
//                viewGroup.getPaddingBottom());
    }

    @Override
    public int getTopOffset() {
        return viewGroup.getTop();
//        return viewGroup.getPaddingTop();
    }

    @Override
    public void setOnListScrollListener(OnListScrollListener onListScrollListener) {
        this.onListScrollListener = onListScrollListener;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public void recycle() {
        this.onListScrollListener = null;
    }
}
