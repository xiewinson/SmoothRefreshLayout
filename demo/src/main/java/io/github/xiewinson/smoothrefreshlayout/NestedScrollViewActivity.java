package io.github.xiewinson.smoothrefreshlayout;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.github.xiewinson.smoothrefresh.library.ScreenUtil;
import io.github.xiewinson.smoothrefresh.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefresh.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.classic.Classic1HeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.page.classic.ClassicPageWrapper;

/**
 * 更改paddingTop的方式在ScrollView上会有跳动，不适合
 */
public class NestedScrollViewActivity extends BaseActivity {

    private LinearLayout linearLayout;
    private SmoothRefreshLayout refreshLayout;
    private NestedScrollView nestedScrollView;
    private int i;
    private boolean first = false;

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
        refreshLayout.setPages(new ClassicPageWrapper() {

            @Override
            protected View onCreateErrorView(ViewGroup container) {
                View v = super.onCreateErrorView(container);
                v.findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshLayout.setRefreshing(true);
                    }
                });
                return v;
            }
        });
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (first) {
                            first = false;
                            refreshLayout.showErrorPage();
                        } else {
//                            refreshLayout.setRefreshing(false);
                        }
                    }
                }, 2000);
            }
        });
//        refreshLayout.setRefreshing(true);
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
