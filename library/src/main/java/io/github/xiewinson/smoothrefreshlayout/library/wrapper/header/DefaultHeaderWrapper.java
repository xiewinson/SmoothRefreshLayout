package io.github.xiewinson.smoothrefreshlayout.library.wrapper.header;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.xiewinson.smoothrefreshlayout.library.R;
import io.github.xiewinson.smoothrefreshlayout.library.annotation.RefreshHeaderState;

/**
 * Created by winson on 2017/10/3.
 */

public class DefaultHeaderWrapper extends RefreshHeaderWrapper {
    private TextView titleTv;
    private ImageView iconIv;

    public DefaultHeaderWrapper(Context context) {
        super(context);
    }


    @Override
    public View initRefreshHeaderView() {
        View view = LayoutInflater.from(context).inflate(R.layout.header_default_refresh, null, false);
        titleTv = (TextView) view.findViewById(R.id.title_tv);
        iconIv = (ImageView) view.findViewById(R.id.icon_iv);
        return view;
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
        iconIv.setRotation(offset * 180);
    }
}
