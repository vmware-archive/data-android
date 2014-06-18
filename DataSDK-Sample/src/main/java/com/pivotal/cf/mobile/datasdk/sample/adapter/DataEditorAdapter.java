package com.pivotal.cf.mobile.datasdk.sample.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.pivotal.cf.mobile.datasdk.data.PCFObject;
import com.pivotal.cf.mobile.datasdk.sample.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataEditorAdapter extends BaseAdapter {

    private static final int[] baseRowColours = new int[]{0xddeeff, 0xddffee};
    private final LayoutInflater inflater;
    private List<ArrayItem> items = Collections.emptyList();
    private PCFObject pcfObject;
    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            ((EditText) v).setCursorVisible(hasFocus);
        }
    };

    private class ArrayItem {
        public String key;
        public String value;
    }

    private class ViewHolder {
        public TextView label1;
        public TextView label2;
        public EditText editText1;
        public EditText editText2;
    }

    public DataEditorAdapter(Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setObject(PCFObject pcfObject) {
        this.pcfObject = pcfObject;

        items = new ArrayList<ArrayItem>();

        for(Map.Entry<String, Object> entry : pcfObject.entrySet()) {
            final ArrayItem item = new ArrayItem();
            item.key = entry.getKey();
            item.value = entry.getValue().toString();
            items.add(item);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (pcfObject != null && items != null) {
            return items.size() + 1;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (pcfObject != null && items != null && position > 0) {
            return items.get(position - 1);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.editor_cell, parent, false);

            viewHolder = setViewHolder(convertView);
            viewHolder.editText1.setOnFocusChangeListener(focusChangeListener);
            viewHolder.editText2.setOnFocusChangeListener(focusChangeListener);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        convertView.setBackgroundColor(getBackgroundColour(position));

        if (position == 0) {
            viewHolder.label1.setText("Class Name");
            viewHolder.label2.setText("Object ID");
            if (pcfObject != null) {
                viewHolder.editText1.setText(pcfObject.getClassName());
                viewHolder.editText2.setText(pcfObject.getObjectId());
            } else {
                viewHolder.editText1.setText("");
                viewHolder.editText2.setText("");
            }
        } else {
            ArrayItem item = (ArrayItem) getItem(position);
            viewHolder.label1.setText("Key");
            viewHolder.label2.setText("Value");
            viewHolder.editText1.setText(item.key);
            viewHolder.editText2.setText(item.value);
        }

        return convertView;
    }

    private ViewHolder setViewHolder(View view) {
        final ViewHolder tags = new ViewHolder();
        tags.label1 = (TextView) view.findViewById(R.id.label1);
        tags.label2 = (TextView) view.findViewById(R.id.label2);
        tags.editText1 = (EditText) view.findViewById(R.id.value1);
        tags.editText2 = (EditText) view.findViewById(R.id.value2);
        view.setTag(tags);
        return tags;
    }

    public int getBackgroundColour(int position) {
        int rowColour = position == 0 ? baseRowColours[0] : baseRowColours[1];
        if (position % 2 == 0) {
            rowColour -= 0x00111111;
        }
        return rowColour | 0xff000000;
    }
}
