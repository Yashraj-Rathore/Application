package com.example.myapplication.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.antitheft.R;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<ImageItem> imageItems;

    public ImageAdapter(Context context, ArrayList<ImageItem> imageItems) {
        this.context = context;
        this.imageItems = new ArrayList<>();
    }

    public void setImageItems(ArrayList<ImageItem> newImageItems) {
        this.imageItems = newImageItems;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return imageItems.size();
    }

    @Override
    public Object getItem(int position) {
        return imageItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.gridImage);
            holder.timeTextView = convertView.findViewById(R.id.timeTextView); // Assuming you've added this to your item_image.xml
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageItem imageItem = imageItems.get(position);
        Glide.with(context)
                .load(imageItem.getImageUrl())
                .into(holder.imageView);
        holder.timeTextView.setText(imageItem.getUploadTime());

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
        TextView timeTextView; // TextView for the upload time
    }
}

