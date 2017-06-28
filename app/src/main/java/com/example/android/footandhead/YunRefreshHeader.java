package com.example.android.footandhead;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.print.PrintAttributes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 头部下拉刷新自定义布局
 * Created by zengzhi on 2017/6/28.
 */

public class YunRefreshHeader extends LinearLayout implements BaseRefreshHeader{

    private Context mContext;
    private AnimationDrawable animationDrawable;
    private TextView msg;
    private int mState = STATE_NORMAL;
    private int mMeasuredHeight;
    private LinearLayout mContainer;

    public YunRefreshHeader(Context context) {
        this(context, null);
    }

    public YunRefreshHeader(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YunRefreshHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initView();
    }

    private void initView() {

        LayoutInflater.from(mContext).inflate(R.layout.kaws_refresh_header, this);
        ImageView img = (ImageView) findViewById(R.id.img);

        //动画
        animationDrawable = (AnimationDrawable) img.getDrawable();

        if (animationDrawable.isRunning()) {
            animationDrawable.stop();
        }
        //文本显示控件
        msg = (TextView) findViewById(R.id.msg);
        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //头布局高度
        mMeasuredHeight = getMeasuredHeight();
        //设置水平居中
        setGravity(Gravity.CENTER_HORIZONTAL);
        //控件容器
        mContainer = (LinearLayout) findViewById(R.id.container);
        //高度设置0，不可见
        mContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        //高度设置WRAP_CONTENT
        this.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    //BaseRefreshHeader接口的4个方法
    @Override
    public void onMove(float delta) {

        //头部的高度大于0，
        if (getVisiableHeight() > 0 || delta > 0) {

            //设置头布局的高度的为(int) delta + getVisiableHeight()
            setVisiableHeight((int) delta + getVisiableHeight());


            if (mState <= STATE_RELEASE_TO_REFRESH) {
                //未处于刷新状态，更新箭头
                if (getVisiableHeight() > mMeasuredHeight) {

                    //下拉的高度大于mMeasuredHeight，在设置为下拉刷新
                    setState(STATE_RELEASE_TO_REFRESH);

                }else {

                    //显示为下拉刷新，不做刷新操作
                    setState(STATE_NORMAL);
                }
            }

        }

    }

    private void setState(int state) {
        if (state == mState) return;
        switch (state) {
            case STATE_NORMAL:
                if (animationDrawable.isRunning()) {
                    animationDrawable.stop();
                }
                msg.setText(R.string.listview_header_hint_normal);
                break;
            case STATE_RELEASE_TO_REFRESH:
                if (mState != STATE_RELEASE_TO_REFRESH) {
                    if (!animationDrawable.isRunning()) {
                        animationDrawable.start();
                    }
                    msg.setText(R.string.listview_header_hint_release);
                }
                break;
            case STATE_REFRESHING:
                msg.setText(R.string.refreshing);
                break;
            case STATE_DONE:
                msg.setText(R.string.refresh_done);
                break;
            default:
        }
        mState = state;
    }

    @Override
    public boolean releaseAction() {
        boolean isOnRefresh = false;
        int height = getVisiableHeight();
        if (height == 0) // not visible.
            isOnRefresh = false;

        if (getVisiableHeight() > mMeasuredHeight && mState < STATE_REFRESHING) {
            setState(STATE_REFRESHING);
            isOnRefresh = true;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mState == STATE_REFRESHING && height <= mMeasuredHeight) {
            //return;
        }
        int destHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mState == STATE_REFRESHING) {
            destHeight = mMeasuredHeight;
        }
        smoothScrollTo(destHeight);

        return isOnRefresh;
    }

    @Override
    public void refreshComplate() {
        setState(STATE_DONE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                reset();
            }
        }, 500);
    }

    public void reset() {
        smoothScrollTo(0);
        setState(STATE_NORMAL);
    }

    private void smoothScrollTo(int destHeight) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisiableHeight(), destHeight);
        animator.setDuration(300).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisiableHeight((int) animation.getAnimatedValue());
            }
        });
        animator.start();
    }

    @Override
    public int getVisiableHeight() {
        //返回头部的高度
        return  mContainer.getHeight();
    }

    //设置可见高度，如果小于0则=0
    private void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
//
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getState() {

        return mState;
    }
}
