package io.github.xiewinson.smoothrefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefreshlayout.library.ScreenUtil;
import io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.DefaultRefreshHeaderWrapper;

public class NestedScrollViewActivity extends BaseActivity {

    private LinearLayout linearLayout;
    private SmoothRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nestedscroll_view);
        initActionBar("NestedScrollView");
        linearLayout = (LinearLayout) findViewById(R.id.container);
        refreshLayout = (SmoothRefreshLayout) findViewById(R.id.refreshLayout);


        for (int i = 0; i < 30; i++) {
            TextView tv = new TextView(this);
            tv.setTextColor(Color.WHITE);
            int padding = ScreenUtil.getPxByDp(this, 16);
            tv.setPadding(0, padding, 0, 0);
            tv.setBackgroundColor(Color.RED);
            tv.setText("item" + i);
            linearLayout.addView(tv);
        }

        refreshLayout.setRefreshHeader(new DefaultRefreshHeaderWrapper(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        });
        refreshLayout.setRefreshing(true);

    }

}
