package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.DefaultHeaderPosCalculator;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.IHeaderPosCalculator;

/**
 * Created by winson on 2017/10/6.
 */

public abstract class HeaderWrapper implements IHeaderWrapper {
    private View headerView;
    private IHeaderPosCalculator headerPosCalculator;

    public HeaderWrapper() {
    }

    public void setHeaderPosCalculator(IHeaderPosCalculator headerPosCalculator) {
        this.headerPosCalculator = headerPosCalculator;
    }

    @NonNull
    protected abstract View onCreateView(ViewGroup container);

    @NonNull
    @Override
    public final View getView(ViewGroup container) {
        if (headerView == null) {
            headerView = onCreateView(container);
            //解决偶然的第一次下拉时View没显示出来该状态
            onStateChanged(RefreshHeaderState.PULL_TO_REFRESH);
        }
        return headerView;
    }

    @NonNull
    @Override
    public IHeaderPosCalculator getHeaderPosCalculator() {
        if (headerPosCalculator == null) {
            headerPosCalculator = new DefaultHeaderPosCalculator();
        }
        return headerPosCalculator;
    }
}
