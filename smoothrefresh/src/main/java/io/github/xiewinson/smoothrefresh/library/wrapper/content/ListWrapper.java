package io.github.xiewinson.smoothrefresh.library.wrapper.content;

import android.view.ViewGroup;

/**
 * Created by winson on 2017/10/23.
 */

public class ListWrapper extends ContentViewWrapper {

    public ListWrapper(ViewGroup viewGroup) {
        super(viewGroup);
        viewGroup.setMotionEventSplittingEnabled(false);
        viewGroup.setClipToPadding(false);
    }

    @Override
    public boolean topChildIsFirstItem() {
        return false;
    }
}
