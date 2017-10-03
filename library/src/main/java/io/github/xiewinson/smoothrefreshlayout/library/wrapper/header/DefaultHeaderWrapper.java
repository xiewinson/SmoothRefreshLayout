package io.github.xiewinson.smoothrefreshlayout.library.wrapper.header;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefreshlayout.library.DeviceUtil;

/**
 * Created by winson on 2017/10/3.
 */

public class DefaultHeaderWrapper implements IHeaderWrapper {
    private View refreshHeaderView;
    private Context context;

    public DefaultHeaderWrapper(Context context) {
        this.context = context;
    }

    @Override
    public View getRefreshHeaderView() {
        LinearLayout refreshHeaderView = new LinearLayout(context);
        refreshHeaderView.setBackgroundColor(Color.BLUE);
        refreshHeaderView.setGravity(Gravity.CENTER);

        ImageView iv = new ImageView(context);
        int size = DeviceUtil.getPxByDp(context, 24);
        refreshHeaderView.addView(iv, new LinearLayout.LayoutParams(size, size));

        TextView titleTv = new TextView(context);
        titleTv.setText("哈哈哈哈哈哈");
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        titleTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        tvParams.leftMargin = DeviceUtil.getPxByDp(context, 8);
        refreshHeaderView.addView(titleTv, tvParams);

        refreshHeaderView.setPadding(size, size, size, size);
        return refreshHeaderView;
    }

    @Override
    public void onStateChanged(int state) {

    }
}
