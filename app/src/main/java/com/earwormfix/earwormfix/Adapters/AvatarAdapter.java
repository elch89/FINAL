package com.earwormfix.earwormfix.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.earwormfix.earwormfix.R;

public class AvatarAdapter extends ArrayAdapter<String> {
    private String[] spinnerTitles;
    private int[] spinnerImages;
    private Context mContext;

    public AvatarAdapter(@NonNull Context context, String[] titles, int[] images) {
        super(context, R.layout.row);
        this.spinnerTitles = titles;
        this.spinnerImages = images;
        this.mContext = context;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return spinnerTitles.length;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.row, parent, false);
            mViewHolder.mFlag = (ImageView) convertView.findViewById(R.id.imageView1);
            mViewHolder.mName = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        mViewHolder.mFlag.setImageResource(spinnerImages[position]);
        mViewHolder.mName.setText(spinnerTitles[position]);

        return convertView;
    }

    private static class ViewHolder {
        ImageView mFlag;
        TextView mName;
    }
}
