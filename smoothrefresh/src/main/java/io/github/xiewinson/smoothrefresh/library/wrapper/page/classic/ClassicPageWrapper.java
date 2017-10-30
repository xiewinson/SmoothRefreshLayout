package io.github.xiewinson.smoothrefresh.library.wrapper.page.classic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.wrapper.page.PageWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.R;

/**
 * Created by winson on 2017/10/28.
 */

public class ClassicPageWrapper extends PageWrapper {

    @Override
    protected View onCreateLoadingView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.page_loading_classic, container, false);
    }

    @Override
    protected View onCreateEmptyView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.page_empty_classic, container, false);
    }

    @Override
    protected View onCreateErrorView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.page_error_classic, container, false);
    }

    @Override
    protected View onCreateLoadingFooterView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_loading_classic, container, false);
    }

    @Override
    protected View onCreateErrorFooterView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_error_classic, container, false);
    }

    @Override
    protected View onCreateEmptyFooterView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_empty_classic, container, false);
    }
}
