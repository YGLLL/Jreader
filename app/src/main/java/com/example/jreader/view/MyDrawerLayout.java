package com.example.jreader.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Switch;

/**
 * Created by Lxq on 2016/4/16.
 */
public class MyDrawerLayout extends DrawerLayout {

    private View currentDrawerView;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;

    public MyDrawerLayout(Context context) {
        this(context, null);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final ViewConfiguration configuration = ViewConfiguration
                         .get(getContext());
                 mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

      /**  boolean intercepted = false;
        intercepted = super.onInterceptTouchEvent(ev);
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        if(ev.getAction() == MotionEvent.ACTION_MOVE) {
          if(isDrawerVisible(Gravity.LEFT)||isDrawerOpen(Gravity.LEFT)) {
            //  intercepted = true;
          }else {
            //  intercepted = false;
          }
        }else {
            //  intercepted = false;
        }

        return intercepted;    */
        try {
                final float x = ev.getX();
                final float y = ev.getY();

                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastMotionX = x;
                        mLastMotionY = y;

                      /**  int xDiff = (int) Math.abs(x - mLastMotionX);
                        int yDiff = (int) Math.abs(y - mLastMotionY);
                        final int x_yDiff = xDiff * xDiff + yDiff * yDiff;

                        boolean xMoved = x_yDiff > mTouchSlop * mTouchSlop;


                        if (xDiff >20) {
                                Log.d("myDrawerlayout oninter", "44444");
                            return true;  //自身消费该事件

                        } else {
                            return false;
                        }        */


                    case MotionEvent.ACTION_MOVE:

                        break;

                      default:
                      break;
                }
           // return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
        }
       // return false;
        return super.onInterceptTouchEvent(ev);
    }


    public void setDrawerViewWithoutIntercepting(View view) {
        this.currentDrawerView = view;
    }


}
