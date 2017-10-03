package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnViewGroupScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public interface IViewGroupWrapper {
    void setViewGroupScrollListener(OnViewGroupScrollListener onViewGroupScrollListener);
    void removeViewGroupScrollListener();
}
