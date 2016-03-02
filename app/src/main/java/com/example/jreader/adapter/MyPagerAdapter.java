package com.example.jreader.adapter;




import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.jreader.MarkActivity;
import com.example.jreader.fragment.BookMarkFragment;
import com.example.jreader.fragment.CatalogueFragment;
import com.example.jreader.fragment.NotesFragment;


/**
 * Created by Administrator on 2016/1/12.
 */
public class MyPagerAdapter extends FragmentPagerAdapter {
    private BookMarkFragment bookMarkFragment;
    private CatalogueFragment catalogueFragment;
    private NotesFragment notesFragment;
    private final String[] titles = { "书签", "目录", "笔记" };

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (bookMarkFragment == null) {
                    //  bookMarkFragment = new BookMarkFragment();
                    //创建bookMarkFragment实例时同时把需要intent中的值传入
                    bookMarkFragment = BookMarkFragment.newInstance(MarkActivity.getBookpath_intent());
                }
                return bookMarkFragment;

            case 1:
                if (catalogueFragment == null) {
                    catalogueFragment = new CatalogueFragment();
                }
                return catalogueFragment;
            case 2:
                if (notesFragment == null) {
                    notesFragment = new NotesFragment();
                }
                return notesFragment;
            default:
                return null;
        }
    }

}
