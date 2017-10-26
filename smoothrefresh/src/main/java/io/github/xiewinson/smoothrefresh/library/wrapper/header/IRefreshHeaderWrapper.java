package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.IRefreshHeaderPosCalculator;

/**
 * Created by winson on 2017/10/3.
 */

public interface IRefreshHeaderWrapper {

    @NonNull
    View getRefreshHeaderView();

    @NonNull
    IRefreshHeaderPosCalculator getRefreshHeaderPosCalculator();

    void onStateChanged(@RefreshHeaderState int state);

    void onPullRefreshHeader(@FloatRange(from = 0, to = 1.0f) float offset);

//    void initParams(IContentViewWrapper contentViewWrapper);
}
