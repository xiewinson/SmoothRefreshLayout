package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.support.v4.widget.NestedScrollView;

import io.github.xiewinson.smoothrefresh.library.listener.OnContentViewScrollListener;

/**
 * Created by winson on 2017/10/7.
 * 更改paddingTop的方式在ScrollView上会有跳动，不适合
 *
 * @hide
 */

public class NestedScrollViewWrapper extends ContentViewWrapper {

    private NestedScrollView scrollView;

    public NestedScrollViewWrapper(NestedScrollView scrollView) {
        super(scrollView);
        this.scrollView = scrollView;
    }

}
