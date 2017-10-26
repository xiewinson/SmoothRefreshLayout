package io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by winson on 2017/10/26.
 */

public abstract class RefreshHeaderPartlyVisiblePosCalculator implements IRefreshHeaderPosCalculator {
    @NonNull
    @Override
    public int[] getRefreshHeaderPosition(View refreshHeaderView, int contentViewTop, int contentViewPaddingTop) {
        int params[] = new int[3];
        int headerHeight = refreshHeaderView.getMeasuredHeight();
        params[0] = contentViewTop - headerHeight;
        params[1] = params[0] + getRefreshingHeight(refreshHeaderView);
        params[2] = params[0] + headerHeight;
        return params;
    }

    public abstract int getRefreshingHeight(View refreshHeaderView);
}
