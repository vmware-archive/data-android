package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.pivotal.cf.mobile.datasdk.sample.R;

public class ViewJsonActivity extends ActionBarActivity {

	public static final String JSON = "json";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_json_activity);
		setJsonData();
	}

	private void setJsonData() {
		final Intent intent = getIntent();
		final String data = intent.getStringExtra(JSON);
		final TextView textView = (TextView) findViewById(R.id.json_data);
		textView.setText(formatString(data));
	}

	private String formatString(String text){

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
