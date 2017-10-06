package io.github.xiewinson.smoothrefreshlayout.library.wrapper.header;

import android.content.Context;
import android.view.View;

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
        }
        return headerView;
    }


}
