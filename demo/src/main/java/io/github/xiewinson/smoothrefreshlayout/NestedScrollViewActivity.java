package io.github.xiewinson.smoothrefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefresh.library.ScreenUtil;
import io.github.xiewinson.smoothrefresh.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefresh.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.classic.Classic1HeaderWrapper;

/**
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
        linearLayout = findViewById(R.id.container);
        nestedScrollView = findViewById(R.id.scrollView);
        refreshLayout = findViewById(R.id.refreshLayout);
        nestedScrollView.setFillViewport(true);
        for (int i = 0; i < 30; i++) {
            addItem();
        }

        refreshLayout.setRefreshHeader(new Classic1HeaderWrapper());
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 5000);
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
