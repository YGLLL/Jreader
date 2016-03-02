package com.example.jreader.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.GridView;

import com.example.jreader.R;

/**
 * Created by Administrator on 2015/12/17.
 */
public class MyGridView extends GridView {
    private Bitmap background;

    public MyGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        background = BitmapFactory.decodeResource(getResources(),
                R.drawable.bookshelf_layer_center1);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int count = getChildCount();
        int top = count > 0 ? getChildAt(0).getTop() : 0;
        int backgroundWidth = background.getWidth();
        int backgroundHeight = background.getHeight()+2;
       // int backgroundHeight = background.getHeight();
        int width = getWidth();
        int height = getHeight();

        for (int y = top; y < height; y += backgroundHeight) {
            for (int x = 0; x < width; x += backgroundWidth) {
                canvas.drawBitmap(background, x, y, null);
            }
        }

        super.dispatchDraw(canvas);
    }
}
