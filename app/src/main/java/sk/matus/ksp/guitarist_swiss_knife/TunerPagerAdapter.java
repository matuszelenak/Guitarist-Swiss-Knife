package sk.matus.ksp.guitarist_swiss_knife;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import java.util.ArrayList;


/**
 * Adapter that enables several visualization of the tuner to be displayed in the same activity
 */
public class TunerPagerAdapter extends PagerAdapter {
    int numberOfPages = 0;
    Context context;

    ArrayList<View>pages = new ArrayList<>();

    public TunerPagerAdapter(Context context) {
        super();
        this.context = context;
    }

    public void addPage(View view){
        pages.add(view);
        numberOfPages ++;
    }

    @Override
    public int getCount() {
        return numberOfPages;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(pages.get(position));
        return pages.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout)object);
    }
}
