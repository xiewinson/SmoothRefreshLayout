package io.github.xiewinson.smoothrefreshlayout;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import io.github.xiewinson.smoothrefresh.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefresh.library.listener.OnLoadMoreListener;
import io.github.xiewinson.smoothrefresh.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.classic.ClassicHeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.page.classic.ClassicPageWrapper;


public class ListViewActivity extends BaseActivity {
    private ListView listView;
    private SmoothRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        initActionBar("ListView");
        listView = findViewById(R.id.listView);
        refreshLayout = findViewById(R.id.refreshLayout);

        final List<String> data = new ArrayList<>();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item, R.id.tv, data);
        listView.setAdapter(adapter);

        refreshLayout.setRefreshHeader(new ClassicHeaderWrapper());
        refreshLayout.setPages(new ClassicPageWrapper());
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        data.clear();
                        for (int i = 0; i < 15; i++) {
                            data.add("ListView Item" + i);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, 1000);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.showErrorFooter();
                    }
                }, 2000);
            }
        });
        refreshLayout.setRefreshing(true);
    }

}
