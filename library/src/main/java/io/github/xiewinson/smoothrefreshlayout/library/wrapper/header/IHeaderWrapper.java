package io.github.xiewinson.smoothrefreshlayout.library.wrapper.header;

import android.view.View;

/**
 * Created by winson on 2017/10/3.
 */

public interface IHeaderWrapper {
    View getRefreshHeaderView();

    void onStateChanged(int state);
}
