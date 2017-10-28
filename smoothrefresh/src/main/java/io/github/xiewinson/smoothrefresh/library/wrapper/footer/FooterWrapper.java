package io.github.xiewinson.smoothrefresh.library.wrapper.footer;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by winson on 2017/10/28.
 */

public abstract class FooterWrapper implements IFooterWrapper {

    private View emptyView;
    private View errorView;
    private View emptyFooterView;
    private View errorFooterView;

    @NonNull
    @Override
    public final View getEmptyView(ViewGroup container) {
        if (emptyView == null) {
            emptyView = onCreateEmptyView(container);
        }
        return emptyView;
    }


    @NonNull
    @Override
    public final View getErrorView(ViewGroup container) {
        if (errorView == null) {
            errorView = onCreateErrorView(container);
        }
        return errorView;
    }


    @Override
    public final View getEmptyFooterView(ViewGroup container) {
        if (emptyFooterView == null) {
            emptyFooterView = onCreateEmptyFooterView(container);
        }
        return emptyFooterView;
    }

    @Override
    public final View getErrorFooterView(ViewGroup container) {
        if (errorFooterView == null) {
            errorFooterView = onCreateErrorFooterView(container);
        }
        return errorFooterView;
    }

    @NonNull
    protected abstract View onCreateEmptyView(ViewGroup container);

    @NonNull
    protected abstract View onCreateErrorView(ViewGroup container);

    protected View onCreateEmptyFooterView(ViewGroup container) {
        return null;
    }

    protected View onCreateErrorFooterView(ViewGroup container) {
        return null;
    }
}
