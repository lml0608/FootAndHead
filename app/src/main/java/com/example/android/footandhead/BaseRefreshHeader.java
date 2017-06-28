package com.example.android.footandhead;

/**
 * Created by zengzhi on 2017/6/28.
 */

public interface BaseRefreshHeader {

    //正常状态
    int STATE_NORMAL = 0;
    //下拉刷新
    int STATE_RELEASE_TO_REFRESH = 1;
    //正在刷新加载数据
    int STATE_REFRESHING = 2;
    //刷新完成
    int STATE_DONE = 3;



    void onMove(float delta);

    boolean releaseAction();

    void refreshComplate();

    int getVisiableHeight();

}
