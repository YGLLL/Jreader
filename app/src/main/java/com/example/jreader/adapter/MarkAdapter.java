package com.example.jreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jreader.R;
import com.example.jreader.database.BookMarks;
import com.example.jreader.util.BookPageFactory;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Administrator on 2016/1/3.
 */
public class MarkAdapter extends BaseAdapter {
    private Context mContext;
    // private ArrayList<HashMap<String, String>> aList = null;
    private List<BookMarks> list ;
    private ListView markList;
  //  private MarkHelper markhelper;
     public MarkAdapter(Context c, List<BookMarks> list) {
         mContext = c;
         this.list = list;

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
     //   View layout = inflater.inflate(R.layout.item_mark, null);
      //  TextView markText1 = (TextView) layout.findViewById(R.id.markText1);
     //   TextView markText2 = (TextView) layout.findViewById(R.id.markText2);
     //   markText1.setText(list.get(position).getText());
        // long begin = list.get(position).getBegin();
     //   int page = list.get(position).getPage();
      //  long count = list.get(position).getCount();
    //    markText2.setText("[" + page + "/" + count + "] "
       //         + list.get(position).getTime().substring(0, 16));
     //   ImageView markImage2 = (ImageView) layout.findViewById(R.id.markImage2);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.marklistview_item,null);
            viewHolder.text_mark = (TextView) convertView.findViewById(R.id.text_mark);
            viewHolder.progress1 = (TextView) convertView.findViewById(R.id.progress1);
            viewHolder.mark_time = (TextView) convertView.findViewById(R.id.mark_time);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.text_mark.setText(list.get(position).getText());
        long begin = list.get(position).getBegin();
        float fPercent = (float) (begin * 1.0 / BookPageFactory
                .getM_mbBufLen());
        DecimalFormat df = new DecimalFormat("#0");
        String strPercent = df.format(fPercent * 100) + "%";
        viewHolder.progress1.setText(strPercent);
        viewHolder.mark_time.setText(list.get(position).getTime().substring(0, 16));
        return convertView;
    }

    class ViewHolder {

        TextView text_mark,progress1,mark_time;
    }

}
