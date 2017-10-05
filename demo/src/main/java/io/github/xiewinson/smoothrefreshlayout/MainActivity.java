package io.github.xiewinson.smoothrefreshlayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout;
import io.github.xiewinson.smoothrefreshlayout.library.listener.OnRefreshListener;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SmoothRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        refreshLayout = (SmoothRefreshLayout) findViewById(R.id.refreshLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ListAdapter listAdapter = new ListAdapter();
        recyclerView.setAdapter(listAdapter);
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            data.add(String.valueOf(i));
        }
        listAdapter.addItems(data);

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
//        refreshLayout.addRefreshHeaderView(new IHeaderWrapper() {
//            @Override
//            public View getRefreshHeaderView() {
//                Button button = new Button(MainActivity.this);
//                button.setText("啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦");
//                return button;
//            }
//
//            @Override
//            public void onStateChanged(int state) {
//
//            }
//        });
    }

    private static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
        private List<String> data = new ArrayList<>();

        public ListAdapter() {
        }


        private void addItems(List<String> list) {
            data.addAll(list);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
             holder.bindData(data.get(position));
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
                        Toast.makeText(view.getContext(), "点击事件", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void bindData(String str) {
                tv.setText(str);
            }
        }
    }
}
