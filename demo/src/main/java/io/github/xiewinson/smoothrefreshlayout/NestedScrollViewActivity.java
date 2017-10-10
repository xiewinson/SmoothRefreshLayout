package io.github.xiewinson.smoothrefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefreshlayout.library.ScreenUtil;
import io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.DefaultRefreshHeaderWrapper;

/**
 *
 * 更改paddingTop的方式在ScrollView上会有跳动，不适合
 */
public class NestedScrollViewActivity extends BaseActivity {

    private LinearLayout linearLayout;
    private SmoothRefreshLayout refreshLayout;
    private NestedScrollView nestedScrollView;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nestedscroll_view);
        initActionBar("NestedScrollView");
        linearLayout = (LinearLayout) findViewById(R.id.container);
        nestedScrollView = (NestedScrollView) findViewById(R.id.scrollView);
        refreshLayout = (SmoothRefreshLayout) findViewById(R.id.refreshLayout);
        nestedScrollView.setFillViewport(true);
        for (int i = 0; i < 30; i++) {
            addItem();
        }

        refreshLayout.setRefreshHeader(new DefaultRefreshHeaderWrapper(this));
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        addItem();
                    }
                }, 1000);
            }
        });
    }

    private void addItem() {
        TextView tv = new TextView(this);
        tv.setTextColor(Color.WHITE);
        int padding = ScreenUtil.getPxByDp(this, 16);
        tv.setPadding(padding, padding, padding, padding);
        tv.setBackgroundColor(Color.RED);
        tv.setText("item" + (i++));
        linearLayout.addView(tv);
    }

}
