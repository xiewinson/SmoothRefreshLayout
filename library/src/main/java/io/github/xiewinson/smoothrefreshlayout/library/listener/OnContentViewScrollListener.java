package io.github.xiewinson.smoothrefreshlayout.library.listener;

/**
 * Created by winson on 2017/10/3.
 */

public interface OnContentViewScrollListener {
    void onScrollAbsolute(int firstItemY);

    void onScrollRelative(int dy);
}