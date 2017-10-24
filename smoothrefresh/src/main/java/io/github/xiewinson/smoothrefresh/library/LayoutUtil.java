package io.github.xiewinson.smoothrefresh.library;

import android.view.View;

/**
 * Created by winson on 2017/10/24.
 */

public class LayoutUtil {
    public static void layoutView(View view, int top) {
        view.layout(view.getPaddingLeft(),
                top,
                view.getPaddingLeft() + view.getMeasuredWidth(),
                top + view.getMeasuredHeight());
    }
}
