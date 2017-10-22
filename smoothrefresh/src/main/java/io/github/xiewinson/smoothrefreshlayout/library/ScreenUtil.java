
package io.github.xiewinson.smoothrefreshlayout.library;


import android.content.Context;
import android.util.TypedValue;

public class ScreenUtil {

    public static int getPxByDp(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
