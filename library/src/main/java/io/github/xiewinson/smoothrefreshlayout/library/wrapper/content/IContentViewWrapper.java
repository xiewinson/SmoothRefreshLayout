package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public interface IContentViewWrapper {

    void setContentViewScrollListener(OnContentViewScrollListener onViewGroupScrollListener);

    void removeContentViewScrollListener();

    boolean topChildIsFirstItem();

    void scrollVerticalBy(int dy);

    void smoothScrollVerticalToTop();
}
