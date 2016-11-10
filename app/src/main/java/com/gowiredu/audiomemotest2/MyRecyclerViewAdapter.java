package com.gowiredu.audiomemotest2;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gowiredu.audiomemo.R;

import java.util.ArrayList;

import static com.gowiredu.audiomemotest2.MainActivity.previewTextToPass;


public class MyRecyclerViewAdapter extends RecyclerView
        .Adapter<MyRecyclerViewAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "MyRecyclerViewAdapter";
    private ArrayList<String> mDataset;
    private static MyClickListener myClickListener;
    //private static MyClickListener myClickListenerLongPress;

    //MainActivity getPreviewText = new MainActivity();
    String previewText = previewTextToPass;

    public class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView label;
        TextView dateTime;

        public DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.memo_title);
            dateTime = (TextView) itemView.findViewById(R.id.memo_preview);
            Log.i(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
            //Log.i(LOG_TAG, "Adding Listener 2");
            //itemView.setOnLongClickListener(this);
        }

        public void changeColor()
        {
            label.setTextColor(Color.BLUE);
            Log.i("COLOR", "Color changed");
        }

        @Override
        public void onClick(View v) {

            myClickListener.onItemClick(getAdapterPosition(), v);
        }

        /*
        @Override
        public boolean onLongClick(View v) {
            //myClickListenerLongPress.onItemLongClick(getAdapterPosition(), v);
            return true;
        }
        */
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    /*
    public void setOnItemLongClickListener(MyClickListener myClickListener) {
        this.myClickListenerLongPress = myClickListener;
    }
    */

    public MyRecyclerViewAdapter(ArrayList<String> myDataset) {

        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {

        //int i = MainActivity.getVariable();

        holder.label.setText(mDataset.get(position));
        holder.dateTime.setText(previewText);

        /*
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("SINGLECLICK", "onClick detected");
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity callDialogBox = new MainActivity();
                callDialogBox.longPressDialogBox();
                Log.i("LONGPRESS", "Longpressed " + position);
                return true;
            }
        });
        */
    }


    public void addItem(String myDataset) {
        mDataset.add(myDataset);
        notifyItemInserted(0);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {

        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }

}

