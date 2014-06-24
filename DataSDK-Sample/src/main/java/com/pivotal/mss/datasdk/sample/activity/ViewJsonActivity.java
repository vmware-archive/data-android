package com.pivotal.mss.datasdk.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pivotal.mss.datasdk.data.DataException;
import com.pivotal.mss.datasdk.data.MSSObject;
import com.pivotal.mss.datasdk.sample.R;
import com.pivotal.mss.datasdk.sample.view.EditorCell;

public class ViewJsonActivity extends ActionBarActivity {

    public static final String MY_DATA_OBJECT = "MY_DATA_OBJECT";
    private MSSObject mssObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_json_activity);
        mssObject = getObject();
    }

    private MSSObject getObject() {
        final Intent intent = getIntent();
        final MSSObject MSSObject = intent.getParcelableExtra(MY_DATA_OBJECT);
        return MSSObject;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
        populateHeaderView(mssObject);
        populateJsonView(mssObject);
    }

    private void setupViews() {
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
        scrollView.setBackgroundColor(EditorCell.getBackgroundColour(1));
    }

    private void populateHeaderView(MSSObject MSSObject) {
        final EditorCell headerView = (EditorCell) findViewById(R.id.header);
        headerView.setLabels("Class Name", "Object ID");
        headerView.setValue1(MSSObject.getClassName());
        headerView.setValue2(MSSObject.getObjectId());
        headerView.setReadOnly(true);
        headerView.setPosition(0);
    }

    private void populateJsonView(MSSObject MSSObject) {
        final TextView textView = (TextView) findViewById(R.id.json_data);
        textView.setBackgroundColor(EditorCell.getBackgroundColour(1));
        try {
            final byte[] jsonBytes = MSSObject.toJson();
            final String jsonString = new String(jsonBytes);
            textView.setText(formatString(jsonString));
        } catch (DataException e) {
            textView.setText("Error JSONizing object: " + e.getLocalizedMessage());
        }
    }

    private String formatString(String text) {

        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append((i == 0 ? "" : "\n") + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }
}
