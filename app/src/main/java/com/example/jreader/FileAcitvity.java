package com.example.jreader;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jreader.adapter.FileAdapter;
import com.example.jreader.database.BookList;
import com.example.jreader.util.Fileutil;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Administrator on 2015/12/19.
 */
public class FileAcitvity extends AppCompatActivity {
    protected ListView lv;
    protected ArrayList<Map<String, Object>> aList;
    protected int a;
    protected TextView addtips;

    private File root;
    private ListView listView;

    private ImageButton returnBtn;
    private ImageButton fileImage;
    private TextView titleView;
    private int check = 0;// 创建文件的标志

    private static FileAdapter adapter;
    private static HashMap<Integer,Boolean> isSelected;
    public static int checkNum1 = 0; // 记录选中的条目数量

    private Map<String, String> map = null;// 这个用来保存复制和粘贴的路径

    private List<File> list;

    private View view;
    private LinearLayout layout;
    private static Button chooseAllButton, deleteButton, addfileButton;
    protected List<AsyncTask<Void, Void, Boolean>> myAsyncTasks = new ArrayList<AsyncTask<Void, Void, Boolean>>();
    // path的堆栈
    private static Stack<String> pathStack;
    public static ArrayList<String> paths = new ArrayList<String>();
    public static Map<String,Integer> mapin = new HashMap<String, Integer>() ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fileactivity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);                            //toolbar当actionbar使用
        toolbar.setNavigationIcon(R.drawable.return_button);//设置导航图标

        String storageState = Environment.getExternalStorageState();

    /**    if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            root = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath());
        } else {
            finish();
        }   */
        root = Environment.getExternalStorageDirectory();
        initView();
    }


    private void initView() {

      //  layout = (LinearLayout) findViewById(R.id.local_File_lin);

        listView = (ListView) findViewById(R.id.local_File_drawer);

        adapter = new FileAdapter(this, list, isSelected);

        listView.setAdapter(adapter);

        map = new HashMap<String, String>();

        listView.setOnItemClickListener(new DrawerItemClickListener());
     //   registerForContextMenu(listView);// 注册一个上下文菜单

        returnBtn = (ImageButton) findViewById(R.id.local_File_return_btn);
        titleView = (TextView) findViewById(R.id.local_File_title);
        chooseAllButton = (Button) findViewById(R.id.choose_all);
        deleteButton = (Button) findViewById(R.id.delete);
        addfileButton = (Button) findViewById(R.id.add_file);

        searchData(root.getAbsolutePath());
        addPath(root.getAbsolutePath());


        /**
        * 返回上一文件层
        * */
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getLastPath().equals(root.getAbsolutePath())) {
                    return;
                }
                Fileutil.folderNum = 0;
                FileAcitvity.paths.clear();
                mapin.clear();
                removeLastPath();//从栈中移除当前路径，得到上一文件层路径
                searchData(getLastPath());
            }
        });
        /**
        * 全选
         * */
        chooseAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int num = Fileutil.getFileNum(list);
                int j = list.size()-num;  //获得选择时的position
                for ( int k = j ; k < list.size() ; k++) {
                    FileAcitvity.getIsSelected().put(k, true);

                    if(!mapin.containsKey(paths.get(k-j))) {
                        mapin.put(paths.get(k-j),k);
                    }
                }

                checkNum1 = FileAdapter.CheckNum(FileAcitvity.getIsSelected());
                // 刷新listview和TextView的显示
                dataChanged();
            }
        });
        /**
        * 取消选择
        * */
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0 ;i< list.size(); i++) {
                    FileAcitvity.getIsSelected().put(i, false);
                    checkNum1 = FileAdapter.CheckNum(FileAcitvity.getIsSelected());

                    mapin.clear();

                    dataChanged();
                }
            }
        });
        /**
        * 把已经选择的书加入书架
        * */
        addfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String,Integer> map =mapin;

                int addNum = FileAdapter.CheckNum(FileAcitvity.getIsSelected());
                if (addNum>0) {

                   for(String key : map.keySet()) {
                       BookList bookList = new BookList();
                       File file = new File(key);
                       String bookName = file.getName();
                       bookList.setBookname(bookName);
                       bookList.setBookpath(key);
                       saveBooktoSqlite3(bookName,key,bookList);//开启线程存储书到数据库

                   }

                }

            }
        });

    }

    /**
     * 查询调用方法
     */
    public void searchData(String path) {
        searchViewData(path);
        titleView.setText(path);
    }

    /**
     * 异步查询view的数据
     */

    public void putAsyncTask(AsyncTask<Void, Void, Boolean> asyncTask) {
        myAsyncTasks.add(asyncTask.execute());
    }

    public void searchViewData(final String path) {

        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    list = Fileutil.getFileListByPath(path);
                } catch (Exception e) {
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (result) {

                    adapter.setFiles(list);  //list值传到adapter

                    isSelected = new HashMap<Integer, Boolean>();//异步线程后checkBox初始赋值
                    for (int i = 0; i < list.size(); i++) {
                        getIsSelected().put(i, false);
                    }

                    adapter.setSelectedPosition(-1);
                    addfileButton.setText("加入书架(" + 0 + ")项");//异步线程后重新执行初始化
                    adapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(FileAcitvity.this, "查询失败", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }
     /**
      * 添加书本到数据库
      */
    public void saveBooktoSqlite3 (final String bookName,final String key,final BookList bookList ) {

        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                //可以进行界面上的初始化操作
            }

            @Override
            protected Boolean doInBackground(Void... params) {


                try {
                    //   List<BookList> bookLists1 = DataSupport.findAll(BookList.class);
                    String sql = "SELECT id FROM booklist WHERE bookname =? and bookpath =?";
                    Cursor cursor = DataSupport.findBySQL(sql,bookName,key);

                    if (!cursor.moveToFirst()) { //This method will return false if the cursor is empty
                        bookList.save();
                    } else {
                        return false;
                    }

                } catch (Exception e) {
                  //  return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute (Boolean result) {
                if (result) {

                }else {
                    Toast.makeText(getApplicationContext(), bookName+"已在书架了", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
    /**
     * 添加路径到堆栈
     *
     * @param path
     */
    public void addPath(String path) {

        if (pathStack == null) {
            pathStack = new Stack<String>();
        }

        pathStack.add(path);
    }

    /**
     * 获取堆栈最上层的路径
     *
     * @return
     */
    public String getLastPath() {
        return pathStack.lastElement();
    }

    /**
     * 移除堆栈最上层路径
     */
    public void removeLastPath() {
        pathStack.remove(getLastPath());
    }


    private class DrawerItemClickListener implements
            ListView.OnItemClickListener,ListView.OnItemLongClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {

            adapter.setSelectedPosition(position);

            selectItem(position);

        }


        @Override
        public boolean  onItemLongClick (AdapterView<?> parent, View view, int position,
                                         long id) {

            return false;
        }

    }

    private void selectItem(int position) {
        BookList bookList = new BookList();
        String filePath = adapter.getFiles().get(position).getAbsolutePath();
        String fileName = adapter.getFiles().get(position).getName();

        if (adapter.getFiles().get(position).isDirectory()) {
            Fileutil.folderNum = 0;
            mapin.clear();
            FileAcitvity.paths.clear();
            searchData(filePath);
            addPath(filePath);
        } else if (adapter.getFiles().get(position).isFile()) {
            String sql = "SELECT id FROM booklist WHERE bookname =? ";
            Cursor cursor = DataSupport.findBySQL(sql, fileName);
            if (!cursor.moveToFirst()) {
                bookList.setBookname(fileName);
                bookList.setBookpath(filePath);
                bookList.save();//如果没有添加到数据库则添加到数据库
                Intent intent = new Intent();
                intent.setClass(FileAcitvity.this, ReadActivity.class);
                intent.putExtra("bookpath", filePath);
                intent.putExtra("bookname",fileName);
                startActivity(intent);
            }else {
                Intent intent = new Intent();
                intent.setClass(FileAcitvity.this, ReadActivity.class);
                intent.putExtra("bookpath", filePath);
                intent.putExtra("bookname",fileName);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fileactivity, menu);  //加载菜单
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
            return true;
        }
        if (id==android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);//Navigate Up to Parent Activity
            Log.d("FileActivity", "home");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }

    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
        FileAcitvity.isSelected = isSelected;
    }

    public static void dataChanged() {
        // 通知listView刷新
        adapter.notifyDataSetChanged();
        // TextView显示最新的选中数目
        addfileButton.setText("加入书架("+ checkNum1 + ")项");
    }


    @Override
    protected void onRestart () {

        Fileutil.folderNum = 0;
        mapin.clear();
        FileAcitvity.paths.clear();
        super.onRestart();
        //Log.d("FileAcitvity", "onRestart");

    }

    @Override
    protected void onPause () {

        super.onPause();
        Fileutil.folderNum = 0;
        mapin.clear();
        FileAcitvity.paths.clear();
       // Log.d("FileAcitvity", "onPause");

    }


}
