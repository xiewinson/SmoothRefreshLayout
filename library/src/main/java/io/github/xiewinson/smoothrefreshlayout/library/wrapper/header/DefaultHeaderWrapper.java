package io.github.xiewinson.smoothrefreshlayout.library.wrapper.header;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefreshlayout.library.ScreenUtil;
import io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState;

/**
 * Created by winson on 2017/10/3.
 */

public class DefaultHeaderWrapper extends RefreshHeaderWrapper {
    private LinearLayout refreshHeaderView;
    private TextView titleTv;

    public DefaultHeaderWrapper(Context context) {
        super(context);
    }


    @Override
    public View initRefreshHeaderView() {
        refreshHeaderView = new LinearLayout(context);
//        refreshHeaderView.setBackgroundColor(Color.BLUE);
        refreshHeaderView.setGravity(Gravity.CENTER);

        ImageView iv = new ImageView(context);
        int size = ScreenUtil.getPxByDp(context, 24);
        refreshHeaderView.addView(iv, new LinearLayout.LayoutParams(size, size));

        titleTv = new TextView(context);
        titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        titleTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        tvParams.leftMargin = ScreenUtil.getPxByDp(context, 8);
        refreshHeaderView.addView(titleTv, tvParams);

        refreshHeaderView.setPadding(size, size, size, size);
        return refreshHeaderView;
    }

    @Override
    public void onStateChanged(@RefreshHeaderState int state) {
        switch (state) {
            case RefreshHeaderState.PULL_TO_REFRESH:
                titleTv.setText("下拉进行刷新");
                break;
            case RefreshHeaderState.RELEASE_TO_REFRESH:
                titleTv.setText("放开开始刷新");
                break;
            case RefreshHeaderState.REFRESHING:
                titleTv.setText("正在进行刷新");
                break;
            case RefreshHeaderState.REFRESH_COMPLETED:
                titleTv.setText("刷新已经完成");
                break;
        }
    }

    @Override
    public void onPullRefreshHeader(@FloatRange(from = 0, to = 1.0f) float offset) {

    }
}
