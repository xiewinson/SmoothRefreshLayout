package io.github.xiewinson.smoothrefresh.library.wrapper.footer.classic;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.wrapper.footer.FooterWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.R;

/**
 * Created by winson on 2017/10/28.
 */

public class ClassicFooterWrapper extends FooterWrapper {
    @NonNull
    @Override
    protected View onCreateEmptyView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_empty_classic, container, false);
    }

    @NonNull
    @Override
    protected View onCreateErrorView(ViewGroup container) {
        return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_error_classic, container, false);
    }
}
