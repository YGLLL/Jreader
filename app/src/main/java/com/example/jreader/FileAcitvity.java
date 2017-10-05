package com.example.jreader;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
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
import com.example.jreader.database.BookCatalogue;
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
 * Created by LXQ on 2015/12/19.
 */
public class FileAcitvity extends AppCompatActivity {
    protected ArrayList<Map<String, Object>> aList;
    protected int a;
    private File root;
    private ListView listView;
    private ImageButton returnBtn;
    private TextView titleView;
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10 ;
    private static FileAdapter adapter;
    private static HashMap<Integer,Boolean> isSelected;
    public static int checkNum1 = 0; // 记录选中的条目数量
    private Map<String, String> map = null;// 这个用来保存复制和粘贴的路径
    private List<File> listFile;
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
        getWindow().setBackgroundDrawable(null);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);                            //toolbar当actionbar使用
        toolbar.setNavigationIcon(R.drawable.return_button);//设置导航图标
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("导入图书");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, EXTERNAL_STORAGE_REQ_CODE);
        }
        root = Environment.getExternalStorageDirectory();
        initView();
        handledButtonListener();
    }

    /**
     * 初始化view
     */
    private void initView() {

        listView = (ListView) findViewById(R.id.local_File_drawer);
        adapter = new FileAdapter(this, listFile, isSelected);
        listView.setAdapter(adapter);
        map = new HashMap<String, String>();
        listView.setOnItemClickListener(new DrawerItemClickListener());
        listView.setOnItemLongClickListener(new DrawerItemClickListener());//
        returnBtn = (ImageButton) findViewById(R.id.local_File_return_btn);
        titleView = (TextView) findViewById(R.id.local_File_title);
        chooseAllButton = (Button) findViewById(R.id.choose_all);
        deleteButton = (Button) findViewById(R.id.delete);
        addfileButton = (Button) findViewById(R.id.add_file);

        searchData(root.getAbsolutePath());
        addPath(root.getAbsolutePath());

    }

    private void handledButtonListener () {
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

                int num = Fileutil.getFileNum(listFile);
                int j = listFile.size()-num;  //获得选择时的position
                for ( int k = j ; k < listFile.size() ; k++) {
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
                for (int i = 0; i < listFile.size(); i++) {
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
                Map<String, Integer> map = mapin;

                int addNum = FileAdapter.CheckNum(FileAcitvity.getIsSelected());
                if (addNum > 0) {
                    for (String key : map.keySet()) {
                        BookList bookList = new BookList();
                        File file = new File(key);
                        String bookName = Fileutil.getFileNameNoEx(file.getName());
                        bookList.setBookname(bookName);
                        bookList.setBookpath(key);
                        saveBooktoSqlite3(bookName, key, bookList);//开启线程存储书到数据库
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
                    listFile = Fileutil.getFileListByPath(path);
                } catch (Exception e) {
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);

                if (result) {
                    adapter.setFiles(listFile);  //list值传到adapter
                    isSelected = new HashMap<Integer, Boolean>();//异步线程后checkBox初始赋值
                    for (int i = 0; i < listFile.size(); i++) {
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
                    String sql = "SELECT id FROM booklist WHERE bookname =? and bookpath =?";
                    Cursor cursor = DataSupport.findBySQL(sql, bookName, key);
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
            protected void onPostExecute(Boolean result) {
                if (result) {
                } else {
                    //  Toast.makeText(getApplicationContext(), bookName+"已在书架了", Toast.LENGTH_SHORT).show();
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

            return true;
        }

    }

    /**
     * item点击时分文件夹和文件，是文件夹则继续进入，是文件则打开
     * @param position
     */
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
           // Log.d("FileActivity", "home");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //checkBox的选择状态hashMap
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

        super.onRestart();

    }

    private void checkPermission (Activity thisActivity, String permission, int requestCode) {
        //判断当前Activity是否已经获得了该权限
        if(ContextCompat.checkSelfPermission(thisActivity,permission) != PackageManager.PERMISSION_GRANTED) {
            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    permission)) {
                Toast.makeText(this,"添加图书需要此权限，请允许",Toast.LENGTH_SHORT).show();
                //进行权限请求
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{permission},
                        requestCode);
            } else {
                //进行权限请求
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{permission},
                        requestCode);
            }
        } else {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_REQ_CODE: {
                // 如果请求被拒绝，那么通常grantResults数组为空
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功，进行相应操作
                    root = Environment.getExternalStorageDirectory();
                    searchData(root.getAbsolutePath());
                    addPath(root.getAbsolutePath());

                } else {
                    //申请失败，可以继续向用户解释。
                }
                return;
            }
        }
    }
}
