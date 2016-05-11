package com.example.jreader.fragment;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.example.jreader.R;
import com.example.jreader.ReadActivity;
import com.example.jreader.adapter.MarkAdapter;
import com.example.jreader.database.BookMarks;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/11.
 */
public class BookMarkFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener {
    private List<BookMarks> bookMarksList;
    private ListView markListview;
    private static int begin;
    private String bookpath;
    private String mArgument;
    private PopupWindow deleteMarkPop;
    private View delateMarkPopView;
    private int itemPosition;
    public static final String ARGUMENT = "argument";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                              Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mark,container,false);
        markListview = (ListView) view.findViewById(R.id.marklistview);
        initDeleteMarkPop();
        markListview.setOnItemClickListener(this);
        markListview.setOnItemLongClickListener(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mArgument = bundle.getString(ARGUMENT);
        }
        bookMarksList = new ArrayList<>();
        bookMarksList = DataSupport.where("bookpath = ?", mArgument).find(BookMarks.class);
        MarkAdapter markAdapter = new MarkAdapter(getActivity(), bookMarksList);
        markListview.setAdapter(markAdapter);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            if(deleteMarkPop.isShowing()) {
                deleteMarkPop.dismiss();
            }else {
                begin = bookMarksList.get(arg2).getBegin();
                bookpath = bookMarksList.get(arg2).getBookpath();
                Intent intent = new Intent();
                intent.setClass(getActivity(), ReadActivity.class);
                intent.putExtra("bigin", begin);
                intent.putExtra("bookpath", bookpath);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        itemPosition = arg2;
        if(bookMarksList.size() > arg2) {
            showDeleteMarkPop(arg1, arg2);
        }
        Log.d("bookmarkfragment","是否执行到这里");
        return true;
    }

    /**
     * 用于从Activity传递数据到Fragment
     * @param argument
     * @return
     */
    public static BookMarkFragment newInstance(String argument)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, argument);
        BookMarkFragment bookMarkFragment = new BookMarkFragment();
        bookMarkFragment.setArguments(bundle);
        return bookMarkFragment;
    }

    /**
     * 初始化浮动菜单
     */
    private void initDeleteMarkPop() {
        delateMarkPopView = getActivity().getLayoutInflater().inflate(R.layout.delete_mark_pop,null);
        delateMarkPopView.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        deleteMarkPop = new PopupWindow(delateMarkPopView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        deleteMarkPop.setOutsideTouchable(true);
        deleteMarkPop.setAnimationStyle(R.style.popwin_anim_style);
       // deleteMarkPop.setOutsideTouchable(true);
    }

    /**
     * 显示删除浮动菜单
     * @param view
     * @param position
     */
    private void showDeleteMarkPop(View view,int position) {

            TextView deleteMark_TV = (TextView) delateMarkPopView.findViewById(R.id.delete_mark_tv);
            TextView deleteAllMark_TV = (TextView) delateMarkPopView.findViewById(R.id.delte_allmark_tv);
            deleteMark_TV.setOnClickListener(this);
            deleteAllMark_TV.setOnClickListener(this);
            int popHeight = deleteMarkPop.getContentView().getMeasuredHeight();//注意获取高度的方法
            deleteMarkPop.showAsDropDown(view, 0, -view.getHeight() - popHeight);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //删除单个书签
            case R.id.delete_mark_tv:
               int id = bookMarksList.get(itemPosition).getId();
               DataSupport.delete(BookMarks.class,id);
               notifyDataRefresh();
               // Log.d("bookmarkfragment","删除书签");
               deleteMarkPop.dismiss();
               break;
            //删除全部书签
            case R.id.delte_allmark_tv:
                // Log.d("bookmarkfragment","清空书签");
                String bookpath = bookMarksList.get(itemPosition).getBookpath();
                DataSupport.deleteAll(BookMarks.class,"bookpath = ?",bookpath);
                notifyDataRefresh();
                deleteMarkPop.dismiss();
                break;
        }

    }

    /**
     * 删除后重新从数据库获取数据
     */
    private void notifyDataRefresh () {
        bookMarksList = new ArrayList<>();
        bookMarksList = DataSupport.where("bookpath = ?", mArgument).find(BookMarks.class);
        MarkAdapter markAdapter = new MarkAdapter(getActivity(),bookMarksList);
        markListview.setAdapter(markAdapter);
        markAdapter.notifyDataSetChanged();
    }
}
