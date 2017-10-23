package io.github.xiewinson.smoothrefresh.library.wrapper.header;

import android.content.Context;
import android.view.View;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;

/**
 * Created by winson on 2017/10/6.
 */

public abstract class RefreshHeaderWrapper implements IRefreshHeaderWrapper {
    private View headerView;
    protected Context context;

    public RefreshHeaderWrapper(Context context) {
        this.context = context;
    }

    public abstract View initRefreshHeaderView();

    @Override
    public final View getRefreshHeaderView() {
        if (headerView == null) {
            headerView = initRefreshHeaderView();
            //解决偶然的第一次下拉时View没显示出来该状态
            onStateChanged(RefreshHeaderState.PULL_TO_REFRESH);
        }
        return headerView;
    }


}