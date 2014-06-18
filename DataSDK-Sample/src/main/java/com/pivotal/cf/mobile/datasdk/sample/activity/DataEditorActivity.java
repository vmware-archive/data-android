package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.pivotal.cf.mobile.datasdk.DataSDK;
import com.pivotal.cf.mobile.datasdk.client.AuthorizationException;
import com.pivotal.cf.mobile.datasdk.client.AuthorizedResourceClient;
import com.pivotal.cf.mobile.datasdk.data.DataException;
import com.pivotal.cf.mobile.datasdk.data.DataListener;
import com.pivotal.cf.mobile.datasdk.data.PCFObject;
import com.pivotal.cf.mobile.datasdk.sample.R;
import com.pivotal.cf.mobile.datasdk.sample.adapter.DataEditorAdapter;

public class DataEditorActivity extends ActionBarActivity {

    private static final String CLASS_NAME = "objects";
    private static final String OBJECT_ID = "1234";

    private PCFObject pcfObject;
    private DataEditorAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_editor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            // TODO - implement save
            return true;
        } else if (id == R.id.action_fetch) {
            // TODO - implement fetch
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupListView();
        fetchObject();
    }

    private void setupListView() {
        adapter = new DataEditorAdapter(getApplicationContext());
        listView = (ListView) findViewById(R.id.listView);
        listView.setDividerHeight(0);
        listView.setAdapter(adapter);
    }

    private void fetchObject() {
        if (pcfObject == null) {

            AuthorizedResourceClient client = DataSDK.getInstance().getClient(this);
            pcfObject = new PCFObject(client, CLASS_NAME);
            pcfObject.setObjectId(OBJECT_ID);

            try {
                pcfObject.fetch(new DataListener() {

                    @Override
                    public void onSuccess(final PCFObject object) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(DataEditorActivity.this, "Fetched object.", Toast.LENGTH_LONG).show();
                                adapter.setObject(object);
                            }
                        });
                    }

                    @Override
                    public void onUnauthorized(PCFObject object) {
                        showToast("Authorization error fetching object");
                    }

                    @Override
                    public void onFailure(PCFObject object, String reason) {
                        showToast(reason);
                    }
                });

            } catch (AuthorizationException e) {
                showToast(e.getLocalizedMessage());
            } catch (DataException e) {
                showToast(e.getLocalizedMessage());
            }
        }
    }

    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataEditorActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
