package io.github.xiewinson.smoothrefresh.library.wrapper.page;

import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.annotation.PageState;

/**
 * Created by winson on 2017/10/28.
 */

public interface IPageWrapper {

    View getView(ViewGroup container, @PageState int state);

}
