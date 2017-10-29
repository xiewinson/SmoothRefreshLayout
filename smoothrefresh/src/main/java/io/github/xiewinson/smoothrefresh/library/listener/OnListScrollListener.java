package io.github.xiewinson.smoothrefresh.library.listener;

/**
 * Created by winson on 2017/10/3.
 */

public interface OnListScrollListener {

    void onFirstItemScroll(int firstItemY);

    void onBottomItemScroll(int lastItemY);

    void onReachBottom();
}
