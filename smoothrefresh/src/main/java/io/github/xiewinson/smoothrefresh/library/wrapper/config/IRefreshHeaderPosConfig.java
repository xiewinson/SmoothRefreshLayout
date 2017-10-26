package io.github.xiewinson.smoothrefresh.library.wrapper.config;

import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.view.View;

/**
 * Created by winson on 2017/10/26.
 */

public interface IRefreshHeaderPosConfig {
    @NonNull
    @Size(value = 3)
    int[] getRefreshHeaderPosition(View refreshHeaderView,
                                   int contentViewTop,
                                   int contentViewPaddingTop);
}
