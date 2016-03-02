package com.example.jreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jreader.MarkActivity;
import com.example.jreader.R;
import com.example.jreader.database.BookList;

import java.util.List;

/**
 * Created by Administrator on 2015/12/17.
 */
public class ShelfAdapter extends BaseAdapter {
    Context contex;
    List<BookList> bilist;
    private static LayoutInflater inflater = null;
    private View deleteView; //2015.11.27长按删除功能
    private boolean isShowDelete;// 根据这个变量来判断是否显示删除图标，true是显示，false是不显示
    private String booKpath,bookname;

    public ShelfAdapter (Context context,List<BookList> bilist){
        this.contex=context;
        this.bilist=bilist;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ShelfAdapter (Context context){
        this.contex=context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        // return data.length + 5;


        if(bilist.size()<10){
            return 10;
        }else{

            return bilist.size();}
       // return 20;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
       // return arg0;
        return bilist.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup arg2) {
        // TODO Auto-generated method stub
        final ViewHolder viewHolder;
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.shelfitem, null);
            viewHolder = new ViewHolder();
            viewHolder.view = (TextView) contentView.findViewById(R.id.imageView1);
            contentView.setTag(viewHolder);

            }
             else {
            viewHolder = (ViewHolder) contentView.getTag();
            }

           if (bilist.size() == 0) {
               viewHolder.view.setBackgroundResource(R.drawable.cover_txt);
               viewHolder.view.setClickable(false);
               viewHolder.view.setVisibility(View.INVISIBLE);

              //  deleteView.setVisibility(View.INVISIBLE);//20151127
            } else {
                if(bilist.size()>position){

                    viewHolder.view.setBackgroundResource(R.drawable.cover_txt);
                    final String fileName = bilist.get(position).getBookname();
                    final String filePath = bilist.get(position).getBookpath();
                    viewHolder.view.setText(MarkActivity.getFileNameNoEx(fileName));
                    bookname =fileName;
                    booKpath = filePath;
                 //   Log.d("ShelfAdapter", bilist.get(position).getBookname()+position);
                  //  deleteView.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);//20151127
                    for (int i=0;i<bilist.size();i++) {
                        String a = bilist.get(i).getBookname();
                      //  viewHolder.view.setText(a);
                     //   Log.d("MainActivity", "是否取出全部数据库数据" + a);

                    }


                }else {
                    viewHolder.view.setBackgroundResource(R.drawable.cover_txt);
                    viewHolder.view.setClickable(false);
                    viewHolder.view.setVisibility(View.INVISIBLE);

                  //  deleteView.setVisibility(View.INVISIBLE);//20151127
                }


            }


        return contentView;
    }

    class ViewHolder {

      //  BookView view;
        TextView view;

    }

}
