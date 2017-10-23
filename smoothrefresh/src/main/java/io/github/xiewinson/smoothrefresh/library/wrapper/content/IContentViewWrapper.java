package io.github.xiewinson.smoothrefresh.library.wrapper.content;

/**
 * Created by winson on 2017/10/3.
 */

public interface IContentViewWrapper {

    boolean topChildIsFirstItem();

    void smoothScrollVerticalToTop();

    boolean isSupportNestedScroll();

    boolean isList();

    void layout(int top);

    int getTopOffset();

    void recycle();

}
