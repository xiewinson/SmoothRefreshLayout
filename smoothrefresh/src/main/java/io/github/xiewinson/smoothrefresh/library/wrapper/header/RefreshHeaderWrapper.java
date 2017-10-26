package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.DefaultRefreshHeaderPosCalculator;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.calculator.IRefreshHeaderPosCalculator;

/**
 * Created by winson on 2017/10/6.
 */

public abstract class RefreshHeaderWrapper implements IRefreshHeaderWrapper {
    private View headerView;
    protected Context context;
    private IRefreshHeaderPosCalculator refreshHeaderPosCalculator;

    public RefreshHeaderWrapper(Context context) {
        this.context = context;
    }

    public void setRefreshHeaderPosCalculator(IRefreshHeaderPosCalculator refreshHeaderPosCalculator) {
        this.refreshHeaderPosCalculator = refreshHeaderPosCalculator;
    }

    @NonNull
    public abstract View initRefreshHeaderView();

    @NonNull
    @Override
    public final View getRefreshHeaderView() {
        if (headerView == null) {
            headerView = initRefreshHeaderView();
            //解决偶然的第一次下拉时View没显示出来该状态
            onStateChanged(RefreshHeaderState.PULL_TO_REFRESH);
        }
        return headerView;
    }

    @NonNull
    @Override
    public IRefreshHeaderPosCalculator getRefreshHeaderPosCalculator() {
        if (refreshHeaderPosCalculator == null) {
            refreshHeaderPosCalculator = new DefaultRefreshHeaderPosCalculator();
        }
        return refreshHeaderPosCalculator;
    }
}
