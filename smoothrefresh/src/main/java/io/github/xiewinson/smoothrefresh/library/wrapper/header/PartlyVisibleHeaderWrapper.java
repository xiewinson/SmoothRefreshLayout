package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.support.annotation.NonNull;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.IHeaderPosCalculator;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.PartlyVisibleHeaderPosCalculator;

/**
 * Created by winson on 2017/10/26.
 */

public abstract class PartlyVisibleHeaderWrapper extends HeaderWrapper {

    @NonNull
    @Override
    public IHeaderPosCalculator getHeaderPosCalculator() {
        return new PartlyVisibleHeaderPosCalculator() {
            @Override
            public int getRefreshingHeight(View refreshHeaderView) {
                return PartlyVisibleHeaderWrapper.this.getRefreshingHeight(refreshHeaderView);
            }
        };
    }

    protected abstract int getRefreshingHeight(View refreshHeaderView);
}
