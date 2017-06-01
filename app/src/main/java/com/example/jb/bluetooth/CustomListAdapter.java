package com.example.jb.bluetooth;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CustomListAdapter extends BaseAdapter {



    Context context;
    private int[] number_icon;
    private String[] title;
    private String[] descrip;


    private static LayoutInflater inflater=null;

    public CustomListAdapter(Activity activity, int[] number_icon, String[] title, String[] descrip) {

        context=activity;
        this.number_icon = number_icon;
        this.title = title;
        this.descrip = descrip;

        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return title.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView title;
        TextView descp;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;

        // Referencing the List View layout called Alert_list_view
        rowView = inflater.inflate(R.layout.alert_list_view, null);

        // Telling which is which for every img and title and description.
        holder.img=(ImageView) rowView.findViewById(R.id.icon_img);
        holder.title=(TextView) rowView.findViewById(R.id.title);
        holder.descp= (TextView) rowView.findViewById(R.id.talking);

        // Setting the img, title and description
        holder.img.setImageResource(number_icon[position]);
        holder.title.setText(title[position]);
        holder.descp.setText(descrip[position]);


        /* No need for click listener at the moment
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


            }
        });
        */
        return rowView;
    }

}