package io.github.xiewinson.smoothrefresh.library.wrapper.header.classic;

import android.content.Context;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.github.xiewinson.smoothrefresh.library.annotation.RefreshHeaderState;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.RefreshHeaderPartlyVisibleWrapper;
import io.github.xiewinson.smoothrefreshlayout.library.R;


/**
 * Created by winson on 2017/10/26.
 */

public class Classic1RefreshHeaderWrapper extends RefreshHeaderPartlyVisibleWrapper {
    private Context context;
    private View view;
    private TextView titleTv;
    private ImageView iconIv;

    public Classic1RefreshHeaderWrapper(Context context) {
        super(context);
        this.context = context;
    }

    @NonNull
    @Override
    public View initRefreshHeaderView(ViewGroup container) {
        view = LayoutInflater.from(context).inflate(R.layout.header_refresh_classic1, container, false);
        titleTv = view.findViewById(R.id.title_tv);
        iconIv = view.findViewById(R.id.icon_iv);
        return view;
    }

    @Override
    public void onStateChanged(int state) {
        switch (state) {
            case RefreshHeaderState.PULL_TO_REFRESH:
                titleTv.setText("下拉刷新…");
                break;
            case RefreshHeaderState.RELEASE_TO_REFRESH:
                titleTv.setText("松开刷新…");
                break;
            case RefreshHeaderState.REFRESHING:
                titleTv.setText("正在刷新…");
                break;
            case RefreshHeaderState.REFRESH_COMPLETED:
                titleTv.setText("刷新完成…");
                break;
        }
    }

    @Override
    public void onPullRefreshHeader(@FloatRange(from = 0, to = 1.0f) float offset) {
        iconIv.setRotation(offset * 180);
    }

    @Override
    protected int getRefreshingHeight(View refreshHeaderView) {
        return ((ViewGroup) refreshHeaderView).getChildAt(0).getMeasuredHeight();
    }

}
