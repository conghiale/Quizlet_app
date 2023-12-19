package com.nhom12.ourquizlet.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.nhom12.ourquizlet.R;

public class IntroSliderAdapter extends PagerAdapter {
    Context context;

    int[] images = {
            R.drawable.image_4,
            R.drawable.image_5,
            R.drawable.image_6
    };

    public IntroSliderAdapter(Context context) {
        this.context = context;
    }
    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService (context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate (R.layout.slider_layout, container, false);

        ImageView ivSlider = (ImageView) view.findViewById (R.id.ivSlider);
        ivSlider.setImageResource (images[position]);

        container.addView (view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView ((LinearLayout) object);
    }
}
