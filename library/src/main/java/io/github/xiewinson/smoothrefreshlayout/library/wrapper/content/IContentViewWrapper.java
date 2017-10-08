package io.github.xiewinson.smoothrefreshlayout.library.wrapper.content;

import io.github.xiewinson.smoothrefreshlayout.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/3.
 */

public interface IContentViewWrapper {

    void setViewGroupScrollListener(OnContentViewScrollListener onViewGroupScrollListener);

    void removeContentViewScrollListener();

    boolean topChildIsFirstItem();

    void scrollBy(int dx, int dy);

    void scrollToTop();

}
