package io.github.xiewinson.smoothrefresh.library.wrapper.footer;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by winson on 2017/10/28.
 */

public interface IFooterWrapper {

    @NonNull
    View getEmptyView(ViewGroup container);

    @NonNull
    View getErrorView(ViewGroup container);

    View getEmptyFooterView(ViewGroup container);

    View getErrorFooterView(ViewGroup container);

}
