package com.example.jreader;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.jreader.adapter.ShelfAdapter;
import com.example.jreader.animation.ContentScaleAnimation;
import com.example.jreader.animation.Rotate3DAnimation;
import com.example.jreader.database.BookList;
import com.example.jreader.database.BookMarks;
import com.example.jreader.util.BookInformation;
import com.example.jreader.view.DragGridView;
import com.example.jreader.view.MyDrawerLayout;


import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener {
    private DragGridView bookShelf;
    private GridView gv;
    private ArrayList<BookInformation> bilist;
    private MyDrawerLayout drawerLayout;
    private NavigationView navigationView;
    private List<BookList> bookLists;
    private boolean isShowDelete = false;  //是否显示删除图标
    private static View rootView;

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
    private int bookViewPosition;
    private LinearLayout linearLayout;
    private ShelfAdapter adapter;
    private itemMoveToFirst mitemMoveToFirst;
    private ReadActivity readActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_main1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);                            //toolbar当actionbar使用
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);//设置导航图标

        mWindowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        wmRootView = new AbsoluteLayout(this);
        pop=new PopupWindow(wmRootView, AbsoluteLayout.LayoutParams.MATCH_PARENT,AbsoluteLayout.LayoutParams.MATCH_PARENT,false);
        rootView = getWindow().getDecorView();
        linearLayout = (LinearLayout) findViewById(R.id.bookItemLinearLayout);


        SQLiteDatabase db = Connector.getDatabase();  //初始化数据库
        ReadActivity.sp = getSharedPreferences("config", MODE_PRIVATE);//在这里初始化preferences防止未打开过书就删除书报的错误
        bookShelf = (DragGridView) findViewById(R.id.bookShelf);
        ActivityManager activityManager=(ActivityManager)MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = activityManager.getMemoryClass();// 返回的就是本机给每个app分配的运行内存

        bookLists = new ArrayList<>();
        bookLists = DataSupport.findAll(BookList.class);
        for (int i=0;i<bookLists.size();i++) {
            String a = bookLists.get(i).getBookname();
         //   Log.d("MainActivity","是否取出全部数据库数据"+a);
        }
        adapter = new ShelfAdapter(MainActivity.this,bookLists);
        bookShelf.setAdapter(adapter);
        initNavigationView();

        bookShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bookLists.size() > position) {
                    if (isShowDelete) {
                        String bookpath = bookLists.get(position).getBookpath();

                        DataSupport.deleteAll(BookList.class, "bookpath = ?", bookpath);
                        DataSupport.deleteAll(BookMarks.class, "bookpath = ?", bookpath);

                        if (ReadActivity.sp.contains(bookpath + "begin")) {
                            ReadActivity.editor.remove(bookpath + "begin");//删除该书在本地存储的值
                            ReadActivity.editor.commit();
                        }

                        bookLists = new ArrayList<BookList>();
                        bookLists = DataSupport.findAll(BookList.class);
                        final ShelfAdapter adapter = new ShelfAdapter(MainActivity.this, bookLists);
                        bookShelf.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        //  openBookIn();
                        //  if(mIsOpen) {
                        setBookViewPosition(position);
                        adapter.setItemToFirst(position);
                        String bookpath = bookLists.get(position).getBookpath();
                        String bookname = bookLists.get(position).getBookname();
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, ReadActivity.class);
                        intent.putExtra("openposition", position);
                        intent.putExtra("bookpath", bookpath);
                        intent.putExtra("bookname", bookname);
                        startActivity(intent);
                        //  }

                    }
                }
            }
        });

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                Log.d("mainactivity", "drawerslide");
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.d("mainactivity", "drawerOpened");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.d("mainactivity", "DrawerClosed");
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                Log.d("mainactivity", "stateChanged");
            }
        });

    }

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
           Log.d("MainActivity","home");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart(){

        super.onRestart();
         closeBookIn();
        //   DataBaseConnection dbc = new DataBaseConnection(MainActivity.this);   //创建数据库
        //   dbc.getWritableDatabase();//打开数据库，返回一个可对数据库进行读写操作的对象
        isShowDelete = false;//防止在长按事件下进入FileActivity后再回到MainActivity监听仍然存在
        bookLists = new ArrayList<>();

        bookLists = DataSupport.findAll(BookList.class);

        ShelfAdapter adapter = new ShelfAdapter(MainActivity.this,bookLists);
        for (int i=0;i<bookLists.size();i++) {
            String a = bookLists.get(i).getBookname();
          //  Log.d("MainActivity","是否取出全部数据库数据"+a);

        }
        bookShelf.setAdapter(adapter);

        adapter.notifyDataSetChanged();
        DragGridView.setIsShowDeleteButton(false);
        Log.d("MainActivity", "onResatart");
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

        if (isShowDelete && keyCode == KeyEvent.KEYCODE_BACK) {
            isShowDelete=false;
            /**   AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("你确定退出吗？")
             .setCancelable(false)
             .setPositiveButton("确定",
             new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog,
             int id) {
             finish();
             }
             })
             .setNegativeButton("返回",
             new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog,
             int id) {
             dialog.cancel();
             }
             });
             AlertDialog alert = builder.create();
             //  alert.show();   */
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }


    void initAnimation() {
        AccelerateInterpolator interpolator=new AccelerateInterpolator();

       // TextView textView = new TextView(this);
        cover = new TextView(this);
       // textView.setBackgroundResource(R.drawable.cover_txt);
        //// 一个控件在其父窗口中的坐标位置 x y
        cover.getLocationInWindow(location);  //此处为imageview

        float scale1 = MainActivity.getWindowWidth() / (float)cover.getWidth();
        float scale2 = MainActivity.getWindowHeight() / (float)cover.getHeight();
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

    public void openBookIn(){
        if (!mIsOpen) {
            openBook();
        }
    }

    public static void closeBookIn(){
        if(mIsOpen) {
            closeBook();
        }
    }

    public  void  openBook() {
        if(isFirstload){
            isFirstload=false;

            initAnimation();
        }


        mWindowManager.addView(wmRootView, getDefaultWindowParams());

        //wmRootView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));

      //  cover = new ImageView(getContext());
        // cover.setScaleType(getScaleType());
        //  cover.setImageDrawable(getDrawable());
        cover = new TextView(getApplicationContext());
      //  cover = (TextView) findViewById(R.id.imageView1);
        cover.setBackgroundResource(R.drawable.cover_txt);
        content = new ImageView(this);

        //  content.setScaleType(getScaleType());
        //   content.setBackground(getResources().getDrawable(R.drawable.content));
        content.setBackgroundDrawable(getResources().getDrawable(R.drawable.content));


        AbsoluteLayout.LayoutParams params =new AbsoluteLayout.LayoutParams(wmRootView.getLayoutParams()) ;
        params.x=location[0];
        params.y=location[1];
        wmRootView.addView(content, params);
        wmRootView.addView(cover, params);

        //        cover.setX(location[0]);
        //        cover.setY(location[1]);
        //        content.setX(location[0]);
        //        content.setY(location[1]);




        /**  wmRootView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
        if(mIsOpen){
        closeBook();
        }
        }
        });  */

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
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.RGBA_8888);

        return params;
    }

    void initView() {

        wmRootView = new AbsoluteLayout(this);
        pop=new PopupWindow(wmRootView, AbsoluteLayout.LayoutParams.MATCH_PARENT,AbsoluteLayout.LayoutParams.MATCH_PARENT,false);
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
                   String bookpath = bookLists.get(getBookViewPosition()).getBookpath();
                   String bookname = bookLists.get(getBookViewPosition()).getBookname();
                   Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                   intent.putExtra("bookpath", bookpath);
                   intent.putExtra("bookname", bookname);
                   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(intent);
            }

        }else{
            animationCount--;

            if(animationCount<=0) {
                mIsOpen = false;

                mWindowManager.removeView(wmRootView);

            }
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    public void setBookViewPosition(int position) {
        this.bookViewPosition = position;
    }

    public int getBookViewPosition () {
        return bookViewPosition;
    }


}
