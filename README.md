# SmoothRefreshLayout

### 原理
> * 目前我见过的很多 Android 下拉刷新控件实现方式，一种是通过把 RecyclerView 分为不同 itemType 然后第一个 item 就是展示刷新的那个 View (以下就称为 Refresh Header 吧)；还有一种比较普遍即是通过 NestedScrolling 机制实现。前者的缺点是把 RecyclerView 的 Adapter 搞得很复杂，使用和定制起来也有点不太方便；后者的缺点是 NestedScrolling 机制在滑动展示刷新中正在转圈的 Refresh Header 时很钝，特别是当你进行快滑的时候，一下就过去了没有丝滑的感觉，参见微博的刷新就是这种感觉，其实也还好，但是强迫症多滑几次就会觉得好卡好顿，就想 Refresh Header 就能像是列表的一部分一样，滑起来很流畅。
> * RecyclerView 和 ListView 的 setClipToPadding 的作用想必不用多说。这个下拉刷新的思想主要就是在下拉时不断增加列表的 paddingTop，Refresh Header 通过监听列表的滑动事件对比 paddingTop 的变化调整 translationY 以达到隐藏/显示的目的

![RecyclerView](https://github.com/xiewinson/SmoothRefreshLayout/blob/master/screenshots/recyclerview.gif)
### 使用方式
* 目前只支持 RecyclerView 和 ListView，在 xml 中包裹 RecyclerView/ListView 
```java
<io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout
    android:id="@+id/refreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:clipChildren="false">
    <android.support.v7.widget.RecyclerView
        android:id="@+id/recyclerview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:padding="16dp" />
</io.github.xiewinson.smoothrefreshlayout.library.SmoothRefreshLayout>    
```
* 在 java 中设置 RefreshHeader 和刷新监听，setRefreshing在切换开始刷新/完成刷新状态。DefaultRefreshHeaderWrapper 实现了默认的 View 效果，若需要自定义 RefreshHeader 
```java
refreshLayout.setRefreshHeader(new DefaultRefreshHeaderWrapper(this));
refreshLayout.setOnRefreshListener(new OnRefreshListener() {
    @Override
    public void onRefresh() {
        // todo
    }
});
refreshLayout.setRefreshing(true);
```
