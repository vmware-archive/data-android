package com.pivotal.cf.mobile.datasdk.sample.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.data.PCFObject;
import com.pivotal.cf.mobile.datasdk.sample.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DataEditorAdapter extends BaseAdapter {

    private static final int[] baseRowColours = new int[]{0xddeeff, 0xddffee};
    private final LayoutInflater inflater;
    private final Activity activity;
    private List<ArrayItem> items = Collections.emptyList();
    private PCFObject pcfObject;

    private View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            ((EditText) v).setCursorVisible(hasFocus);
            if (v.getTag() instanceof ViewHolder) {
                final ViewHolder viewHolder = (ViewHolder) v.getTag();
                if (!hasFocus) {
                    Logger.i("Cell lost focus");
                    saveItemData(viewHolder);
                } else {
                    flashCell(viewHolder);
                }
            }
        }

        private void flashCell(final ViewHolder viewHolder) {
            viewHolder.cell.postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewHolder.cell.setBackgroundColor(viewHolder.backgroundColour);
                }
            }, 100);
        }

        private void saveItemData(ViewHolder viewHolder) {
            // Save data when focus is lost
            final String newKey = viewHolder.editText1.getText().toString();
            final String newValue = viewHolder.editText2.getText().toString();
            if (viewHolder.item != null) {
                // regular key/value item
                String oldKey = viewHolder.item.key;
                if (!oldKey.equals(newKey)) {
                    pcfObject.remove(oldKey);
                }
                pcfObject.put(newKey, newValue);
                viewHolder.item.key = newKey;
                viewHolder.item.value = newValue;
            } else {
                // classname/objectid item
                pcfObject.setClassName(newKey);
                pcfObject.setObjectId(newValue);
            }
        }
    };

    private View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (v.getTag() instanceof ViewHolder) {
                final ViewHolder viewHolder = (ViewHolder) v.getTag();
                if (viewHolder.item != null) {
                    final String key = viewHolder.editText1.getText().toString();
                    final String message = String.format("Are you sure you want to delete the item with key '%s'?", key);
                    final AlertDialog dialog = new AlertDialog.Builder(activity)
                            .setMessage(message)
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    pcfObject.remove(viewHolder.item.key);
                                    items.remove(viewHolder.item);
                                    notifyDataSetChanged();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // no-op
                                }
                            }).setCancelable(true)
                            .create();
                    dialog.show();
                }
            }
            return false;
        }
    };


    private class ArrayItem {
        public String key;
        public String value;
        public boolean giveFocus = false;
    }

    private class ViewHolder {
        public TextView label1;
        public TextView label2;
        public EditText editText1;
        public EditText editText2;
        public ArrayItem item;
        public View cell;
        public int backgroundColour;
    }

    public DataEditorAdapter(Activity activity) {
        this.activity = activity;
        this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public void addItem() {
        final ArrayItem item = new ArrayItem();
        item.key = "";
        item.value = "";
        item.giveFocus = true;
        items.add(0, item);
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

        viewHolder.cell = convertView;
        viewHolder.backgroundColour = getBackgroundColour(position);
        convertView.setBackgroundColor(viewHolder.backgroundColour);
        viewHolder.editText1.setTag(viewHolder);
        viewHolder.editText2.setTag(viewHolder);

        if (position == 0) {
            viewHolder.item = null;
            viewHolder.label1.setText("Class Name");
            viewHolder.label2.setText("Object ID");
            if (pcfObject != null) {
                viewHolder.editText1.setText(pcfObject.getClassName());
                viewHolder.editText2.setText(pcfObject.getObjectId());
            } else {
                viewHolder.editText1.setText("");
                viewHolder.editText2.setText("");
            }
            convertView.setOnLongClickListener(null);
        } else {
            final ArrayItem item = (ArrayItem) getItem(position);
            viewHolder.item = item;
            viewHolder.label1.setText("Key");
            viewHolder.label2.setText("Value");
            viewHolder.editText1.setText(item.key);
            viewHolder.editText2.setText(item.value);
            if (item.giveFocus) {
                viewHolder.editText1.requestFocusFromTouch();
                convertView.setBackgroundColor(0x22bbbbbb);
                item.giveFocus = false;
            }
            convertView.setOnLongClickListener(longClickListener);
        }

        return convertView;
    }

    private ViewHolder setViewHolder(View view) {
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.label1 = (TextView) view.findViewById(R.id.label1);
        viewHolder.label2 = (TextView) view.findViewById(R.id.label2);
        viewHolder.editText1 = (EditText) view.findViewById(R.id.value1);
        viewHolder.editText2 = (EditText) view.findViewById(R.id.value2);
        view.setTag(viewHolder);
        return viewHolder;
    }

    public int getBackgroundColour(int position) {
        int rowColour = position == 0 ? baseRowColours[0] : baseRowColours[1];
        if (position % 2 == 0) {
            rowColour -= 0x00111111;
        }
        return rowColour | 0xff000000;
    }
}
