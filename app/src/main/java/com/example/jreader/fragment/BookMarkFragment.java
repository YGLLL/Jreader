package com.example.jreader.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
public class BookMarkFragment extends Fragment implements AdapterView.OnItemClickListener {
    private List<BookMarks> bookMarksList;
    private ListView markListview;
    private static int begin;
    private String bookpath;
    private String mArgument;
    public static final String ARGUMENT = "argument";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                              Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mark,container,false);
        markListview = (ListView) view.findViewById(R.id.marklistview);
        markListview.setOnItemClickListener(this);
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

        begin = bookMarksList.get(arg2).getBegin();
        bookpath = bookMarksList.get(arg2).getBookpath();
        Intent intent = new Intent();
        intent.setClass(getActivity(), ReadActivity.class);
        intent.putExtra("bigin", begin);
        intent.putExtra("bookpath", bookpath);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public static BookMarkFragment newInstance(String argument)
    {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, argument);
        BookMarkFragment bookMarkFragment = new BookMarkFragment();
        bookMarkFragment.setArguments(bundle);
        return bookMarkFragment;
    }
    /**  markListview = (ListView) findViewById(R.id.marklistview);
     button_back = (ImageButton) findViewById(R.id.back);
     title = (TextView) findViewById(R.id.bookname);
     Intent intent = getIntent();
     bookpath_intent = intent.getStringExtra("bookpath");
     bookMarksList = new ArrayList<>();
     bookMarksList = DataSupport.where("bookpath = ?", bookpath_intent).find(BookMarks.class);
     Log.d("MarkActivity", "数据库数据null");
     for (int i = 0; i < bookMarksList.size(); i++) {
     String a = bookMarksList.get(i).getText();
     Log.d("MarkActivity", "是否取出全部数据库数据" + a);

     }
     MarkAdapter markAdapter = new MarkAdapter(MarkActivity.this, bookMarksList);
     markListview.setAdapter(markAdapter);
     markListview.setOnItemClickListener(this);
     markListview.setOnItemLongClickListener(this);  */

    }
