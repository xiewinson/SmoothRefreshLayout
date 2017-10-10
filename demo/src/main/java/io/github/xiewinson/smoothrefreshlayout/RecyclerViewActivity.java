package io.github.xiewinson.smoothrefreshlayout;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.xiewinson.smoothrefreshlayout.library.ScreenUtil;
import io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;
import io.github.xiewinson.smoothrefreshlayout.library.wrapper.header.DefaultRefreshHeaderWrapper;

public class RecyclerViewActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SmoothRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        initActionBar("RecyclerView");
        refreshLayout = (SmoothRefreshLayout) findViewById(R.id.refreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration decoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Drawable divider = new ColorDrawable(Color.TRANSPARENT) {
            @Override
            public int getIntrinsicHeight() {
                return ScreenUtil.getPxByDp(RecyclerViewActivity.this, 8);
            }
        };
        decoration.setDrawable(divider);
        recyclerView.addItemDecoration(decoration);

        final ListAdapter listAdapter = new ListAdapter();
        recyclerView.setAdapter(listAdapter);
        final List<String> data = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            data.add(String.valueOf(i));
        }
        listAdapter.setItems(data);

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                        Collections.shuffle(data);
                        listAdapter.setItems(data);
                    }
                }, 1000);
            }
        });
        refreshLayout.setRefreshHeader(new DefaultRefreshHeaderWrapper(this));
        refreshLayout.setRefreshing(true);
    }

    private static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private List<String> data = new ArrayList<>();

        public ListAdapter() {
        }


        private void addItems(String item) {
            data.add(item);
            notifyItemRangeInserted(0, 1);
        }

        private void setItems(List<String> list) {
            if(data.size() > 0) {
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
                tv = (TextView) itemView.findViewById(R.id.tv);
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
