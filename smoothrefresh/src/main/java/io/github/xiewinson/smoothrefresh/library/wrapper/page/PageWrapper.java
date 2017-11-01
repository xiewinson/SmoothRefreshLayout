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
            switch (state) {
                case PageState.EMPTY:
                    view = onCreateEmptyView(container);
                    break;
                case PageState.LOADING:
                    view = onCreateLoadingView(container);
                    break;
                case PageState.ERROR:
                    view = onCreateErrorView(container);
                    break;
                case PageState.EMPTY_FOOTER:
                    view = onCreateEmptyFooterView(container);
                    break;
                case PageState.ERROR_FOOTER:
                    view = onCreateErrorFooterView(container);
                    break;
                case PageState.LOADING_FOOTER:
                    view = onCreateLoadingFooterView(container);
                    break;

                case PageState.NO_MORE_FOOTER:
                    view = onCreateNoMoreFooterView(container);
                    break;

                case PageState.NONE:
                    view = onCreateLoadingFooterView(container);
                    break;
            }
            if (view != null) {
                viewMap.put(state, view);
            }
        }
        return view;
    }

    protected abstract View onCreateLoadingView(ViewGroup container);

    protected abstract View onCreateEmptyView(ViewGroup container);

    protected abstract View onCreateErrorView(ViewGroup container);


    protected View onCreateLoadingFooterView(ViewGroup container) {
        return null;
    }

    protected View onCreateEmptyFooterView(ViewGroup container) {
        return null;
    }

    protected View onCreateErrorFooterView(ViewGroup container) {
        return null;
    }

    protected View onCreateNoMoreFooterView(ViewGroup container) {
        return null;
    }

}
