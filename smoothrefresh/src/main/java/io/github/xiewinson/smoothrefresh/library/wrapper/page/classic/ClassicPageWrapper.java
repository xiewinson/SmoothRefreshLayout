package io.github.xiewinson.smoothrefresh.library.wrapper.page.classic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.annotation.PageState;
import io.github.xiewinson.smoothrefresh.library.wrapper.page.PageWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.R;

/**
 * Created by winson on 2017/10/28.
 */

public class ClassicPageWrapper extends PageWrapper {

    @Override
    protected View onCreateView(ViewGroup container, @PageState int state) {
        switch (state) {
            case PageState.LOADING:
                return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_loading_classic, container, false);
            case PageState.ERROR:
                return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_error_classic, container, false);
            case PageState.EMPTY:
                return LayoutInflater.from(container.getContext()).inflate(R.layout.footer_empty_classic, container, false);
        }
        return null;
    }
}
