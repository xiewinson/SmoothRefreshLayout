package io.github.xiewinson.smoothrefresh.library.wrapper.page;

import android.view.View;
import android.view.ViewGroup;

import java.util.WeakHashMap;

import io.github.xiewinson.smoothrefresh.library.annotation.PageState;

/**
 * Created by winson on 2017/10/28.
 */

public abstract class PageWrapper implements IPageWrapper {

    private WeakHashMap<Integer, View> viewMap = new WeakHashMap<>();

    @Override
    public final View getView(ViewGroup container, @PageState int state) {
        View view = viewMap.get(state);
        if (view == null) {
            view = onCreateView(container, state);
            viewMap.put(state, view);
        }
        return view;
    }

    protected abstract View onCreateView(ViewGroup container, @PageState int state);
}
