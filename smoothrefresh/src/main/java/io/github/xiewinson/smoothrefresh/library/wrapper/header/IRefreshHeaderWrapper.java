package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.support.annotation.FloatRange;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;

/**
 * Created by winson on 2017/10/3.
 */

public interface IRefreshHeaderWrapper {
    View getRefreshHeaderView();

    void onStateChanged(@RefreshHeaderState int state);

    void onPullRefreshHeader(@FloatRange(from = 0, to = 1.0f) float offset);
}
