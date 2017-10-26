package io.github.xiewinson.smoothrefresh.library.wrapper.config;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by winson on 2017/10/26.
 */

public class DefaultRefreshHeaderPosConfig implements IRefreshHeaderPosConfig {
    private boolean closeToItems = false;
    private boolean overPullEnable = false;

    public DefaultRefreshHeaderPosConfig(boolean overPullEnable) {
        this.overPullEnable = overPullEnable;
    }

    public DefaultRefreshHeaderPosConfig() {
    }

    public boolean isOverPullEnable() {
        return overPullEnable;
    }

    public void setOverPullEnable(boolean overPullEnable) {
        this.overPullEnable = overPullEnable;
    }

    public boolean isCloseToItems() {
        return closeToItems;
    }

    public void setCloseToItems(boolean closeToItems) {
        this.closeToItems = closeToItems;
    }

    @NonNull
    @Override
    public int[] getRefreshHeaderPosition(View refreshHeaderView, int contentViewTop, int contentViewPaddingTop) {
        int params[] = new int[3];
        int headerHeight = refreshHeaderView.getMeasuredHeight();

        if (closeToItems) {
            params[0] = contentViewTop - headerHeight;
            params[0] += contentViewPaddingTop;
        } else {
            params[0] = contentViewTop - headerHeight;
        }
        params[1] = params[0] + headerHeight;
        if (overPullEnable) {
            params[2] = params[1] + headerHeight * 3;
        } else {
            params[2] = params[1];
        }
        return params;
    }
}
