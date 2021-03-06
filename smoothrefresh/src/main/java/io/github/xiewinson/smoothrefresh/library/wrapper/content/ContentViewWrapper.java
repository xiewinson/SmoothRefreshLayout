package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by winson on 2017/10/3.
 */

public class ContentViewWrapper implements IContentViewWrapper {
    private ViewGroup viewGroup;

    public ContentViewWrapper(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
        this.viewGroup.setClipToPadding(false);
        this.viewGroup.setMotionEventSplittingEnabled(false);
    }

    public static class Factory {
        public static IContentViewWrapper getInstance(View viewGroup) {
            if (viewGroup instanceof RecyclerView) {
                return new RecyclerViewWrapper((RecyclerView) viewGroup);
            } else if (viewGroup instanceof ListView) {
                return new ListViewWrapper((ListView) viewGroup);
            } else if (viewGroup instanceof NestedScrollingParent) {
                return new ContentViewWrapper((ViewGroup) viewGroup);
            }
            throw new IllegalArgumentException("only support nestedScrollParent and listView");
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
    public void moveContentView(int top) {
        viewGroup.offsetTopAndBottom(-viewGroup.getTop() + top);
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

    @Override
    public boolean hasListItemChild() {
        return false;
    }
}
