package com.example.jreader;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jreader.adapter.ShelfAdapter;
import com.example.jreader.animation.ContentScaleAnimation;
import com.example.jreader.animation.Rotate3DAnimation;
import com.example.jreader.database.BookList;
import com.example.jreader.database.BookMarks;
import com.example.jreader.util.BookInformation;
import com.example.jreader.util.BookPageFactory;
import com.example.jreader.util.CommonUtil;
import com.example.jreader.view.DragGridView;
import com.example.jreader.view.MyDrawerLayout;
import com.example.jreader.view.MyGridView;


import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener {
    private DragGridView bookShelf;
    private MyDrawerLayout drawerLayout;
    private NavigationView navigationView;
    private List<BookList> bookLists;
    private static View rootView;

    private WindowManager mWindowManager;
    private AbsoluteLayout wmRootView;

    private static TextView cover;
    private static ImageView content;
    private Typeface typeface;
    private float scaleTimes;
    public static final int ANIMATION_DURATION = 800;
    private int[] location = new int[2];
    private static ContentScaleAnimation contentAnimation;
    private static Rotate3DAnimation coverAnimation;

    private int animationCount=0;  //动画加载计数器  0 默认  1一个动画执行完毕   2二个动画执行完毕
    private boolean mIsOpen = false;
    private int bookViewPosition;
    private LinearLayout linearLayout;
    private ShelfAdapter adapter;
    private static Boolean isExit = false;
    private ImageButton deleteItem_IB;
    private int itemPosition;
    private TextView itemTextView;
    private TextView firstItemTextView;
    private View itemView;
    private View firstView;
    private Bitmap coverBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
          //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
          //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);                            //toolbar当actionbar使用
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);//设置导航图标

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        wmRootView = new AbsoluteLayout(this);
        rootView = getWindow().getDecorView();
        linearLayout = (LinearLayout) findViewById(R.id.bookItemLinearLayout);

        SQLiteDatabase db = Connector.getDatabase();  //初始化数据库
        ReadActivity.sp = getSharedPreferences("config", MODE_PRIVATE);//在这里初始化preferences防止未打开过书就删除书报的错误
        bookShelf = (DragGridView) findViewById(R.id.bookShelf);
        ActivityManager activityManager=(ActivityManager)MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = activityManager.getMemoryClass();// 返回的就是本机给每个app分配的运行内存
        typeface = Typeface.createFromAsset(getApplicationContext().getAssets(),"font/QH.ttf");

        bookLists = new ArrayList<>();
        bookLists = DataSupport.findAll(BookList.class);
        adapter = new ShelfAdapter(MainActivity.this,bookLists);
        bookShelf.setAdapter(adapter);
        initNavigationView();

        bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bookLists.size() > position) {
                    itemPosition = position;
                    String bookname = bookLists.get(itemPosition).getBookname();
                    itemView = view;
                    itemTextView = (TextView) view.findViewById(R.id.imageView1);
                    itemTextView.getLocationInWindow(location);

                    mWindowManager.addView(wmRootView, getDefaultWindowParams());

                    cover = new TextView(getApplicationContext());
                    cover.setBackgroundDrawable(getResources().getDrawable(R.drawable.cover_default_new));
                    cover.setCompoundDrawablesWithIntrinsicBounds(null,null,null,getResources().getDrawable(R.drawable.cover_type_txt));
                    cover.setText(bookname);
                    cover.setTextColor(getResources().getColor(R.color.read_textColor));
                    cover.setTypeface(typeface);
                    int coverPadding = (int) CommonUtil.convertDpToPixel(getApplicationContext(), 10);
                    cover.setPadding(coverPadding, coverPadding, coverPadding, coverPadding);

                    content = new ImageView(getApplicationContext());
                   // content.setBackgroundDrawable(getResources().getDrawable(R.drawable.open_book_bg));
                    Bitmap contentBitmap = Bitmap.createBitmap(itemTextView.getMeasuredWidth(),itemTextView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    contentBitmap.eraseColor(getResources().getColor(R.color.read_background_paperYellow));
                    content.setImageBitmap(contentBitmap);

                    AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                            itemTextView.getLayoutParams());
                    params.x = location[0];
                    params.y = location[1];

                    wmRootView.addView(content, params);
                    wmRootView.addView(cover, params);

                    initAnimation();

                    if (contentAnimation.getMReverse()) {
                        contentAnimation.reverse();
                    }
                    if (coverAnimation.getMReverse()) {
                        coverAnimation.reverse();
                    }
                    cover.clearAnimation();
                    cover.startAnimation(coverAnimation);
                    content.clearAnimation();
                    content.startAnimation(contentAnimation);
                }
            }
        });


    }

   private void initAnimation() {
        AccelerateInterpolator interpolator = new AccelerateInterpolator();

        float scale1 = MainActivity.getWindowWidth() / (float) itemTextView.getMeasuredWidth();
        float scale2 = MainActivity.getWindowHeight() / (float) itemTextView.getMeasuredHeight();
        scaleTimes = scale1 > scale2 ? scale1 : scale2;  //计算缩放比例

        contentAnimation = new ContentScaleAnimation( location[0], location[1],scaleTimes, false);
        contentAnimation.setInterpolator(interpolator);  //设置插值器
        contentAnimation.setDuration(ANIMATION_DURATION);
        contentAnimation.setFillAfter(true);  //动画停留在最后一帧
        contentAnimation.setAnimationListener(this);

        coverAnimation = new Rotate3DAnimation(0, -180, location[0], location[1], scaleTimes, false);
        coverAnimation.setInterpolator(interpolator);
        coverAnimation.setDuration(ANIMATION_DURATION);
        coverAnimation.setFillAfter(true);
        coverAnimation.setAnimationListener(this);

    }

    /**
     * 初始化drawerLoyout
     */
    private void initNavigationView(){
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        drawerLayout = (MyDrawerLayout) findViewById(R.id.drawer_layout);
        //设置侧滑菜单选择监听事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                //关闭抽屉侧滑菜单
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);  //加载菜单
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this,FileAcitvity.class);
            startActivity(intent);
            return true;
        }
        if (id==android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart(){

        DragGridView.setIsShowDeleteButton(false);
        bookLists = new ArrayList<>();
        bookLists = DataSupport.findAll(BookList.class);
        adapter = new ShelfAdapter(MainActivity.this,bookLists);
        bookShelf.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        closeBookAnimation();
        super.onRestart();
    }

    @Override
    protected void onStop() {
        DragGridView.setIsShowDeleteButton(false);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        DragGridView.setIsShowDeleteButton(false);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawers();
            } else {
                exitBy2Click();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 在2秒内按下返回键两次才退出
     */
    private void exitBy2Click() {
        // press twice to exit
        Timer tExit;
        if (!isExit) {
            isExit = true; // ready to exit
            if(DragGridView.getShowDeleteButton()) {
                DragGridView.setIsShowDeleteButton(false);
                //要保证是同一个adapter对象,否则在Restart后无法notifyDataSetChanged
                adapter.notifyDataSetChanged();
            }else {
            Toast.makeText(
                    this,
                    this.getResources().getString(R.string.press_twice_to_exit),
                    Toast.LENGTH_SHORT).show(); }
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // cancel exit
                }
            }, 2000); // 2 seconds cancel exit task

        } else {
            finish();
            // call fragments and end streams and services
            System.exit(0);
        }
    }



    public void setBookViewPosition(int position) {
        this.bookViewPosition = position;
    }

    public int getBookViewPosition () {
        return bookViewPosition;
    }


    public static int getWindowWidth() {
        return rootView.getMeasuredWidth();
    }

    public static int getWindowHeight() {
        return rootView.getMeasuredHeight();
    }

    private WindowManager.LayoutParams getDefaultWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                0, 0,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,//windown类型,有层级的大的层级会覆盖在小的层级
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888);

        return params;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

        //有两个动画监听会执行两次，所以要判断
        if (!mIsOpen) {
            animationCount++;
            if (animationCount >= 2) {
                mIsOpen = true;
                setBookViewPosition(itemPosition);
                adapter.setItemToFirst(itemPosition);
                String bookpath = bookLists.get(itemPosition).getBookpath();
                String bookname = bookLists.get(itemPosition).getBookname();
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, ReadActivity.class);
                intent.putExtra("bookpath", bookpath);
                intent.putExtra("bookname", bookname);
                startActivity(intent);

            }

        } else {
            animationCount--;
            if (animationCount <= 0) {
                mIsOpen = false;
                wmRootView.removeView(cover);
                wmRootView.removeView(content);
                mWindowManager.removeView(wmRootView);
            }
        }
    }
    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public void closeBookAnimation() {

        if (mIsOpen && wmRootView!=null) {
            //因为书本打开后会移动到第一位置，所以要设置新的位置参数
            contentAnimation.setmPivotXValue(bookShelf.getFirstLocation()[0]);
            contentAnimation.setmPivotYValue(bookShelf.getFirstLocation()[1]);
            coverAnimation.setmPivotXValue(bookShelf.getFirstLocation()[0]);
            coverAnimation.setmPivotYValue(bookShelf.getFirstLocation()[1]);

            AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
                    itemTextView.getLayoutParams());
            params.x = bookShelf.getFirstLocation()[0];
            params.y = bookShelf.getFirstLocation()[1];//firstLocation[1]在滑动的时候回改变,所以要在dispatchDraw的时候获取该位置值
            wmRootView.updateViewLayout(cover,params);
            wmRootView.updateViewLayout(content,params);
            //动画逆向运行
            if (!contentAnimation.getMReverse()) {
                contentAnimation.reverse();
            }
            if (!coverAnimation.getMReverse()) {
                coverAnimation.reverse();
            }
            //清除动画再开始动画
            content.clearAnimation();
            content.startAnimation(contentAnimation);
            cover.clearAnimation();
            cover.startAnimation(coverAnimation);
        }
    }
}
