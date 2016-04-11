package com.example.jreader.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.jreader.R;
import com.example.jreader.ReadActivity;
import com.example.jreader.adapter.CatalogueAdapter;
import com.example.jreader.database.BookCatalogue;
import com.example.jreader.database.BookMarks;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/12.
 */
public class CatalogueFragment extends Fragment implements AdapterView.OnItemClickListener {
    private List<BookCatalogue> bookCatalogueList;
    private ListView catalogueListView;
    private String mArgument;
    private int catalogueStartPos;
    private String bookPath;
    public static final String ARGUMENT = "argument";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_catalogue, container, false);
        catalogueListView = (ListView)view.findViewById(R.id.catalogue);
        catalogueListView.setOnItemClickListener(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mArgument = bundle.getString(ARGUMENT);
        }
        bookCatalogueList = new ArrayList<>();
        bookCatalogueList = DataSupport.where("bookpath = ?", mArgument).find(BookCatalogue.class);
        CatalogueAdapter catalogueAdapter = new CatalogueAdapter(getActivity(),bookCatalogueList);
        catalogueListView.setAdapter(catalogueAdapter);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        catalogueStartPos = bookCatalogueList.get(position).getBookCatalogueStartPos();
        bookPath = bookCatalogueList.get(position).getBookpath();
        Intent intent = new Intent();
        intent.setClass(getActivity(), ReadActivity.class);
        intent.putExtra("bigin", catalogueStartPos);
        intent.putExtra("bookpath", bookPath);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public static CatalogueFragment newInstance(String argument) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT, argument);
        CatalogueFragment catalogueFragment = new CatalogueFragment();
        catalogueFragment.setArguments(bundle);
        return  catalogueFragment;
    }
}
