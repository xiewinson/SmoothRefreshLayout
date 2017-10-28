package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.IHeaderPosCalculator;

/**
 * Created by winson on 2017/10/3.
 */

public interface IHeaderWrapper {

    @NonNull
    View getHeaderView(ViewGroup container);

    @NonNull
    IHeaderPosCalculator getHeaderPosCalculator();

    void onStateChanged(@RefreshHeaderState int state);

    void onPullRefreshHeader(@FloatRange(from = 0, to = 1.0f) float offset);

}
