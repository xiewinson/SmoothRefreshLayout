package io.github.xiewinson.smoothrefreshlayout;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.DefaultRefreshHeaderWrapper;

public class ListViewActivity extends BaseActivity {
    private ListView listView;
    private SmoothRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        initActionBar("ListView");
        listView = (ListView) findViewById(R.id.listView);
        refreshLayout = (SmoothRefreshLayout) findViewById(R.id.refreshLayout);

        List<String> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add("listView item" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item, R.id.tv, data);
        listView.setAdapter(adapter);

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
