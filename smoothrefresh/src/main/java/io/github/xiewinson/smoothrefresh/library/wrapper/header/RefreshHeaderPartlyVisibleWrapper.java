package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.IRefreshHeaderPosCalculator;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.RefreshHeaderPartlyVisiblePosCalculator;

/**
 * Created by winson on 2017/10/26.
 */

public abstract class RefreshHeaderPartlyVisibleWrapper extends RefreshHeaderWrapper {
    public RefreshHeaderPartlyVisibleWrapper(Context context) {
        super(context);
    }

    @NonNull
    @Override
    public IRefreshHeaderPosCalculator getRefreshHeaderPosCalculator() {
        return new RefreshHeaderPartlyVisiblePosCalculator() {
            @Override
            public int getRefreshingHeight(View refreshHeaderView) {
                return RefreshHeaderPartlyVisibleWrapper.this.getRefreshingHeight(refreshHeaderView);
            }
        };
    }

    protected abstract int getRefreshingHeight(View refreshHeaderView);
}
