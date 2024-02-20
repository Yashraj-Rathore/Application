package com.example.myapplication.ui.gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.example.antitheft.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> imagePaths;

    // Constructor to ensure imageUrls is never null
    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths != null ? imagePaths : new ArrayList<>();
    }

    // Method to update image URLs and refresh the grid view
    public void setImageUrls(ArrayList<String> newImagePaths) {
        this.imagePaths = newImagePaths!= null ? newImagePaths : new ArrayList<>();
        notifyDataSetChanged(); // Notify the adapter to refresh the views
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false); // Ensure this layout ID matches your grid item XML file name
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.gridImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String imageUrl = imagePaths.get(position);
        Glide.with(context)
                .load(imageUrl) // Use the download URL directly
                .into(holder.imageView);

        return convertView;
    }

    // ViewHolder pattern for performance optimization
    static class ViewHolder {
        ImageView imageView;
    }
}

