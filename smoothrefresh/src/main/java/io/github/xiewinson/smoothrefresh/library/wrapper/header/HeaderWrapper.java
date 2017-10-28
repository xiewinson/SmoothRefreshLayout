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
    protected Context context;
    private IHeaderPosCalculator refreshHeaderPosCalculator;

    public HeaderWrapper(Context context) {
        this.context = context;
    }

    public void setRefreshHeaderPosCalculator(IHeaderPosCalculator refreshHeaderPosCalculator) {
        this.refreshHeaderPosCalculator = refreshHeaderPosCalculator;
    }

    @NonNull
    public abstract View onCreateView(ViewGroup container);

    @NonNull
    @Override
    public final View getHeaderView(ViewGroup container) {
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
        if (refreshHeaderPosCalculator == null) {
            refreshHeaderPosCalculator = new DefaultHeaderPosCalculator();
        }
        return refreshHeaderPosCalculator;
    }
}
