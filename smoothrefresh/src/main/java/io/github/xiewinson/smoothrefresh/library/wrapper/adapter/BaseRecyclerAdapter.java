package io.github.xiewinson.smoothrefresh.library.wrapper.adapter;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Documented;

import static io.github.xiewinson.smoothrefresh.library.wrapper.adapter.BaseRecyclerAdapter.FooterState.EMPTY;
import static io.github.xiewinson.smoothrefresh.library.wrapper.adapter.BaseRecyclerAdapter.FooterState.ERROR;
import static io.github.xiewinson.smoothrefresh.library.wrapper.adapter.BaseRecyclerAdapter.FooterState.LOADING;
import static io.github.xiewinson.smoothrefresh.library.wrapper.adapter.BaseRecyclerAdapter.FooterState.NONE;
import static io.github.xiewinson.smoothrefresh.library.wrapper.adapter.BaseRecyclerAdapter.FooterState.NO_MORE;

/**
 * Created by winson on 2017/11/4.
 */

public abstract class BaseRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Documented
    @IntDef(value = {NONE, LOADING, ERROR, NO_MORE, EMPTY})
    public @interface FooterState {
        int NONE = -17780;
        int LOADING = -17781;
        int ERROR = -17782;
        int NO_MORE = -17783;
        int EMPTY = -17784;
    }


    private int currentFooterState;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (loadFooterEnable()) {
            return new RecyclerView.ViewHolder(getLoadFooterView(viewType)) {
            };
        }
        return onCreateCustomViewHolder(parent, viewType);
    }

    private View getLoadFooterView(int state) {
        return null;
    }

    protected abstract VH onCreateCustomViewHolder(ViewGroup parent, int viewType);


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (loadFooterEnable() && position == getItemCount() - 1) {

            return;
        }
        onBindCustomViewHolder((VH) holder, position);
    }

    protected abstract void onBindCustomViewHolder(VH holder, int position);

    @Override
    public int getItemCount() {
        return getDataCount() + getHeaderCount() + getFooterCount();
    }

    protected abstract int getDataCount();

    protected int getHeaderCount() {
        return 0;
    }

    protected int getFooterCount() {
        return loadFooterEnable() ? 1 : 0;
    }

    protected boolean loadFooterEnable() {
        return currentFooterState != FooterState.NONE;
    }

    @Override
    public int getItemViewType(int position) {
        if (loadFooterEnable() && position == getItemCount() - 1) {
            return currentFooterState;
        }
        return super.getItemViewType(position);
    }


    protected abstract int getCustomItemViewType(int position);
}
