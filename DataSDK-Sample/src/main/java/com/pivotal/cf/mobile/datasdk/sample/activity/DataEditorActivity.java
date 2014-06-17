package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.DataSDK;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationException;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;
import com.pivotal.cf.mobile.datasdk.data.DataException;
import com.pivotal.cf.mobile.datasdk.data.DataListener;
import com.pivotal.cf.mobile.datasdk.data.PCFObject;
import com.pivotal.cf.mobile.datasdk.sample.R;

public class DataEditorActivity extends ActionBarActivity {

    private static final String CLASS_NAME = "objects";
    private static final String OBJECT_ID = "1234";

    private PCFObject pcfObject;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_editor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.data_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        textView = (TextView) findViewById(R.id.textView);

        if (pcfObject == null) {
            AuthorizedResourceClient client = DataSDK.getInstance().getClient(this);
            pcfObject = new PCFObject(client, CLASS_NAME);
            pcfObject.setObjectId(OBJECT_ID);
            setMessage("Fetching object...");

            try {
                pcfObject.fetch(new DataListener() {

                    @Override
                    public void onSuccess(PCFObject object) {
                        setMessage("Fetched object: " + object.keySet().toString());
                    }

                    @Override
                    public void onUnauthorized(PCFObject object) {
                        setMessage("Authorization error fetching object");
                    }

                    @Override
                    public void onFailure(PCFObject object, String reason) {
                        setMessage("Error fetching object: " + reason);
                    }
                });

            } catch (AuthorizationException e) {
                setMessage(e.getLocalizedMessage());
            } catch (DataException e) {
                setMessage(e.getLocalizedMessage());
            }
        }
    }

    private void setMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(message);
            }
        });
    }
}
