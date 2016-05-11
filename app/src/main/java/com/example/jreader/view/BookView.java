package com.example.jreader.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.jreader.MainActivity;
import com.example.jreader.R;
import com.example.jreader.animation.ContentScaleAnimation;
import com.example.jreader.animation.Rotate3DAnimation;
/**
 * Created by Administrator on 2016/2/5.
 */
public class BookView extends TextView implements Animation.AnimationListener {
    public static boolean mIsOpen;
    private WindowManager mWindowManager;
    private AbsoluteLayout wmRootView;
    private PopupWindow pop;

    private static TextView cover;
    private static ImageView content;

    private float scaleTimes;
    public static final int ANIMATION_DURATION = 1000;
    private int[] location = new int[2];

    private static ContentScaleAnimation contentAnimation;
    private static Rotate3DAnimation coverAnimation;

    private static boolean isFirstload = true;
    private int animationCount=0;  //动画加载计数器  0 默认  1一个动画执行完毕   2二个动画执行完毕
    //todo  动画播完后要进行处理

    private boolean animationIsEnd = false;
    public BookView(Context context) {
        this(context, null);
    }

    public BookView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BookView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        initView();

       // openBookIn();
        initListener();
    }

    void initView() {

        pop=new PopupWindow(wmRootView, AbsoluteLayout.LayoutParams.MATCH_PARENT,AbsoluteLayout.LayoutParams.MATCH_PARENT,false);
        wmRootView = new AbsoluteLayout(getContext());
       // mWindowManager.addView(wmRootView, getDefaultWindowParams());
    }

    void initListener() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsOpen) {
                    openBook();
                }
            }
        });
    }

      void initAnimation() {
        AccelerateInterpolator interpolator=new AccelerateInterpolator();
        //// 一个控件在其父窗口中的坐标位置 x y
        getLocationInWindow(location);  //此处为imageview

        float scale1 = MainActivity.getWindowWidth() / (float) getWidth();
        float scale2 = MainActivity.getWindowHeight() / (float) getHeight();
        scaleTimes = scale1 > scale2 ? scale1 : scale2;  //计算缩放比例

        contentAnimation = new ContentScaleAnimation(location[0], location[1],scaleTimes, false);
        contentAnimation.setInterpolator(interpolator);
        contentAnimation.setDuration(ANIMATION_DURATION);
        contentAnimation.setFillAfter(true);//view动画结束后停在最后的位置
        contentAnimation.setAnimationListener(this);


        coverAnimation = new Rotate3DAnimation(0, -180, location[0], location[1], scaleTimes, false);
        coverAnimation.setInterpolator(interpolator);
        coverAnimation.setDuration(ANIMATION_DURATION);
        coverAnimation.setFillAfter(true);
        coverAnimation.setAnimationListener(this);

    }

    public  void  openBook() {


        initAnimation();



        mWindowManager.addView(wmRootView, getDefaultWindowParams());

        //wmRootView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

        cover = new TextView(getContext());
       // cover.setScaleType(getScaleType());
        cover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_txt));

        content = new ImageView(getContext());
        //  content.setScaleType(getScaleType());
        //   content.setBackground(getResources().getDrawable(R.drawable.content));
        content.setBackgroundDrawable(getResources().getDrawable(R.drawable.content));


        AbsoluteLayout.LayoutParams params =new AbsoluteLayout.LayoutParams(getLayoutParams()) ;
        params.x=location[0];
        params.y=location[1];
        Log.d("location 0 =",""+params.x);
        Log.d("location 1 =",""+params.y);
        wmRootView.addView(content, params);
        wmRootView.addView(cover, params);

        //        cover.setX(location[0]);
        //        cover.setY(location[1]);
        //        content.setX(location[0]);
        //        content.setY(location[1]);



        //一个不合理的方案，把关闭书本动画放到这
          wmRootView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
        if(mIsOpen){
        closeBook();
        }
        }
        });

       if (contentAnimation.getMReverse()) {
            contentAnimation.reverse();
        }

        if (coverAnimation.getMReverse()) {
            coverAnimation.reverse();
        }

        content.clearAnimation();
        content.startAnimation(contentAnimation);
        cover.clearAnimation();
        cover.startAnimation(coverAnimation);
    }



    public static void closeBook() {



        if (!contentAnimation.getMReverse()) {
            contentAnimation.reverse();
        }

        if (!coverAnimation.getMReverse()) {
            coverAnimation.reverse();
        }

        content.clearAnimation();
        content.startAnimation(contentAnimation);
        cover.clearAnimation();
        cover.startAnimation(coverAnimation);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private WindowManager.LayoutParams getDefaultWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0, 0,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888);

        return params;
    }


    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if(!mIsOpen){
            animationCount++;

            if(animationCount>=2) {
                mIsOpen = true;
             //   Intent intent = new Intent(getContext(), ReadActivity.class);
             //   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             //   getContext().startActivity(intent);

            }

        }else{
            animationCount--;

            if(animationCount<=0) {
                mIsOpen = false;

                wmRootView.removeView(content);
                wmRootView.removeView(cover);
                mWindowManager.removeView(wmRootView);

            }
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public static boolean getIsOpen(){
        return mIsOpen;
    }
}
