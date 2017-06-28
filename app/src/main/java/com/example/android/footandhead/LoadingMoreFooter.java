package com.example.android.footandhead;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * //自定义加载更多的Footer布局
 * Created by zengzhi on 2017/6/28.
 */

public class LoadingMoreFooter extends LinearLayout {

    //加载中
    public final static int STATE_LOADING = 0;
    //加载完成
    public final static int STATE_COMPLETE = 1;
    //正常状态
    public final static int STATE_NOMORE = 2;

    private TextView mText;

    private AnimationDrawable mAnimationDrawable;

    private ImageView mIvProgress;


    public LoadingMoreFooter(Context context) {
        super(context);
        initView(context);

    }

    public LoadingMoreFooter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context) {

        LayoutInflater.from(context).inflate(
                R.layout.yun_refresh_footer, this
        );

        //加载文字
        mText = (TextView) findViewById(R.id.msg);
        //图片
        mIvProgress = (ImageView) findViewById(R.id.iv_progress);

        //帧动画
        mAnimationDrawable = (AnimationDrawable) mIvProgress.getDrawable();

        if (!mAnimationDrawable.isRunning()) {

            mAnimationDrawable.start();
        }

        //设置本空间的宽高
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    //设置上拉加载的状态

    public void setState(int state) {

        switch (state) {

            //开始加载，变成加载中
            case STATE_LOADING:

                if (!mAnimationDrawable.isRunning()) {

                    mAnimationDrawable.start();
                }
                mIvProgress.setVisibility(View.VISIBLE);
                mText.setText(getContext().getText(R.string.listview_loading));

                this.setVisibility(View.VISIBLE);
                break;

            //加载完成，FOOTER消失
            case STATE_COMPLETE:

                if (mAnimationDrawable.isRunning()) {
                    mAnimationDrawable.stop();
                }
                mText.setText(getContext().getText(R.string.listview_loading));
                this.setVisibility(View.GONE);
                break;

            //没有更多内容了，加载动画停止，不可见
            case STATE_NOMORE:
                if (mAnimationDrawable.isRunning()) {
                    mAnimationDrawable.stop();
                }
                mText.setText(getContext().getText(R.string.nomore_loading));
                mIvProgress.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;


        }
    }


    public void reSet() {

        this.setVisibility(GONE);
    }































}
