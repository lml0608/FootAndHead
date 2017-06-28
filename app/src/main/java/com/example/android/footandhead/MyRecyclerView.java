package com.example.android.footandhead;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.os.Build.VERSION_CODES.M;

/**
 * 自定义RecyclerView,添加下拉刷新
 * Created by zengzhi on 2017/6/28.
 */

public class MyRecyclerView extends RecyclerView {

    private LoadingListener mLoadingListener;
    private WrapAdapter mWrapAdapter;
    //头布局集合
    private SparseArray<View> mHeaderViews = new SparseArray<View>();
    //脚布局集合
    private SparseArray<View> mFootViews = new SparseArray<View>();

    private boolean pullRefreshEnabled = true;//允许下拉刷新
    private boolean loadingMoreEnabled = true;//允许上拉加载更多

    private YunRefreshHeader mRefreshHeader;
    private boolean isLoadingData;//是否正在加载数据

    public int previousTotal;
    private boolean isnomore;//没有更多

    private float mLastY = -1;

    private static final float DRAG_RATE = 1.75f;

    //是否添加额外的footerView
    private boolean isOther = false;


    public MyRecyclerView(Context context) {
        this(context, null);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {

        if (pullRefreshEnabled) {

            //头部
            YunRefreshHeader refreshHeader = new YunRefreshHeader(context);
            //添加到头布局的集合里去
            mHeaderViews.put(0, refreshHeader);
            //头布局
            mRefreshHeader = refreshHeader;
        }

        LoadingMoreFooter footView = new LoadingMoreFooter(context);

        addFootView(footView, false);
        //上拉加载更多FootView不可见
        mFootViews.get(0).setVisibility(GONE);
    }

    /**
     * 改为公有。供外添加view使用,使用标识
     * 注意：使用后不能使用 上拉加载，否则添加无效
     * 使用时 isOther 传入 true，然后调用 noMoreLoading即可。
     */

    public void addFootView(final View view, boolean isOther) {


        mFootViews.clear();
        mFootViews.put(0, view);
        this.isOther = isOther;
    }


    /**
     * 相当于加一个空白头布局：
     * 只有一个目的：为了滚动条显示在最顶端
     * 因为默认加了刷新头布局，不处理滚动条会下移。
     * 和 setPullRefreshEnabled(false) 一块儿使用
     * 使用下拉头时，此方法不应被使用！
     */
    public void clearHeader() {

        mHeaderViews.clear();

        final float scale = getContext().getResources().getDisplayMetrics().density;

        int height = (int) (1.0f * scale + 0.5f);

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);

        View view = new View(getContext());
        view.setLayoutParams(params);
        mHeaderViews.put(0, view);
    }

    //添加头布局多个头布局，第0个为刷新头布局
    public void addHeaderView(View view) {


        if (pullRefreshEnabled && !(mHeaderViews.get(0) instanceof YunRefreshHeader)) {

            YunRefreshHeader refreshHeader = new YunRefreshHeader(getContext());
            mHeaderViews.put(0, refreshHeader);
            mRefreshHeader = refreshHeader;
        }

        mHeaderViews.put(mHeaderViews.size(), view);
    }

    //加载完成
    private void loadMoreComplete() {
        //设置为false，表示没有在加载了
        isLoadingData = false;

        View footView = mFootViews.get(0);

        if (previousTotal <= getLayoutManager().getItemCount()) {

            if (footView instanceof LoadingMoreFooter) {

                ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_COMPLETE);
            } else {
                footView.setVisibility(View.GONE);
            }
        } else {

            if (footView instanceof LoadingMoreFooter) {

                ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_NOMORE);
            } else {

                footView.setVisibility(View.GONE);
            }
            isnomore = true;
        }

        previousTotal = getLayoutManager().getItemCount();
    }


    public void noMoreLoading() {

        isLoadingData = false;

        final View footView = mFootViews.get(0);
        isnomore = true;

        if (footView instanceof LoadingMoreFooter) {

            ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_NOMORE);
        } else {
            footView.setVisibility(View.GONE);
        }
        //额外添加的footView
        if (isOther) {
            footView.setVisibility(View.VISIBLE);
        }
    }


    public void refreshComplete() {

        if (isLoadingData) {

            loadMoreComplete();
        } else {
            mRefreshHeader.refreshComplate();
        }
    }


    @Override
    public void setAdapter(Adapter adapter) {

        mWrapAdapter = new WrapAdapter(mHeaderViews, mFootViews, adapter);

        super.setAdapter(adapter);

        //adapter.registerAdapterDataObserver(mDa);
    }


    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);

        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData &&　loadingMoreEnabled) {

            LayoutManager layoutManager = getLayoutManager();

            int lastVisibleItemPosition;

            if (layoutManager instanceof GridLayoutManager) {

                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {

                int[] into = new int[((StaggeredGridLayoutManager)layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {

                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }

            if (layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= layoutManager.getItemCount()
                    && layoutManager.getItemCount() > layoutManager.getChildCount()
                    && !isnomore && mRefreshHeader.getState() < YunRefreshHeader.STATE_REFRESHING) {

                View footView = mFootViews.get(0);
                isLoadingData = true;
                if (footView instanceof LoadingMoreFooter) {

                    ((LoadingMoreFooter) footView).setState(LoadingMoreFooter.STATE_LOADING);
                } else {

                    footView.setVisibility(View.VISIBLE);
                }
                if (isNetWorkConnected(getContext())) {

                    mLoadingListener.onLoadMore();
                } else {

                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingListener.onLoadMore();
                        }
                    }, 1000);
                }

            }
        }
    }


    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }






















































}
