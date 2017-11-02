package io.github.xiewinson.smoothrefreshlayout;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.xiewinson.smoothrefresh.library.ScreenUtil;
import io.github.xiewinson.smoothrefresh.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefresh.library.wrapper.header.classic.Classic1HeaderWrapper;
import io.github.xiewinson.smoothrefresh.library.wrapper.page.classic.ClassicPageWrapper;

public class RecyclerViewActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SmoothRefreshLayout refreshLayout;
    private boolean flag = false;
    private boolean flag1 = true;
    private int ii = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        initActionBar("RecyclerView");
        refreshLayout = findViewById(R.id.refreshLayout);
        recyclerView = findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable divider = new ColorDrawable(Color.TRANSPARENT) {
            @Override
            public int getIntrinsicHeight() {
                return ScreenUtil.getPxByDp(RecyclerViewActivity.this, 0);
            }
        };
        decoration.setDrawable(divider);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        final ListAdapter listAdapter = new ListAdapter();
        recyclerView.setAdapter(listAdapter);
        final List<String> data = new ArrayList<>();

        listAdapter.setItems(data);


        refreshLayout.setRefreshHeader(new Classic1HeaderWrapper());
        refreshLayout.setOnRefreshListener(() -> refreshLayout.postDelayed(() -> {
            if (flag) {
                flag = false;
                refreshLayout.showErrorPage();
            } else {
                data.clear();
                ii = 0;
                for (int i = 0; i < 15; i++) {
                    data.add(String.valueOf
                            (ii++));
                }
                listAdapter.setItems(data);
                refreshLayout.setRefreshing(false);
            }

        }, 2000));
        refreshLayout.setOnLoadMoreListener(() -> refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setLoadMore(false);
                List<String> data1 = new ArrayList<>();
                for (int i = 0; i < 15; i++) {
                    data1.add(String.valueOf(ii++));
                }
                listAdapter.addItems(data1);
                if (ii >= 39) {
                    refreshLayout.showNoMoreFooter();
                }
            }
        }, 2000));
        refreshLayout.setPages(new ClassicPageWrapper() {
            @Override
            protected View onCreateErrorView(ViewGroup container) {
                View v = super.onCreateErrorView(container);
                v.findViewById(R.id.btn).setOnClickListener(v1 -> refreshLayout.setRefreshing(true));
                return v;
            }

            @Override
            protected View onCreateErrorFooterView(ViewGroup container) {
                View v = super.onCreateErrorFooterView(container);
                v.findViewById(R.id.btn).setOnClickListener(v12 -> refreshLayout.setLoadMore(true));
                return v;
            }
        });
        refreshLayout.setRefreshing(true);

    }

    private static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private List<String> data = new ArrayList<>();

        public ListAdapter() {
        }


        private void addItems(List<String> items) {
            int oldSize = data.size();
            data.addAll(items);
            notifyItemRangeInserted(oldSize, items.size());
        }

        private void setItems(List<String> list) {
            if (data.size() > 0) {
                data.clear();
            }
            data.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ListAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(ListAdapter.ViewHolder holder, int position) {
            holder.bindData("RecyclerView Item" + data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tv;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = itemView.findViewById(R.id.tv);
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });
            }

            public void bindData(String str) {
                tv.setText(str);
            }
        }
    }


}
