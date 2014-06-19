package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.pivotal.cf.mobile.datasdk.data.PCFObject;
import com.pivotal.cf.mobile.datasdk.sample.R;
import com.pivotal.cf.mobile.datasdk.sample.adapter.DataEditorAdapter;

public class DataEditorActivity extends ActionBarActivity {

    private static final String MY_DATA_OBJECT = "MY_DATA_OBJECT";
    private static final String CLASS_NAME = "objects";
    private static final String OBJECT_ID = "123";

    private PCFObject pcfObject;
    private DataEditorAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_editor);
        if (savedInstanceState != null) {
            pcfObject = savedInstanceState.getParcelable(MY_DATA_OBJECT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupListView();
        fetchObject();
    }

    private void setupListView() {
        if (adapter == null) {
            adapter = new DataEditorAdapter(this);
        }
        if (listView == null) {
            listView = (ListView) findViewById(R.id.listView);
            listView.setDividerHeight(0);
            listView.setAdapter(adapter);
        }
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
            saveObject();
            return true;
        } else if (id == R.id.action_fetch) {
            // TODO - implement fetch
            return true;
        } else if (id == R.id.action_add_item) {
            addItem();
        } else if (id == R.id.action_delete_item) {
            toggleDeleteMode();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MY_DATA_OBJECT, pcfObject);
    }

    private void fetchObject() {
        if (pcfObject == null) {

            pcfObject = new PCFObject(CLASS_NAME);
            pcfObject.setObjectId(OBJECT_ID);
            pcfObject.put("Cats", "Dogs");

            // TODO - restore before checking in
//            try {
//                final AuthorizedResourceClient client = DataSDK.getInstance().getClient(this);
//                pcfObject.fetch(client, new DataListener() {
//
//                    @Override
//                    public void onSuccess(final PCFObject object) {
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(DataEditorActivity.this, "Fetched object.", Toast.LENGTH_LONG).show();
//                                adapter.setObject(object);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onUnauthorized(PCFObject object) {
//                        showToast("Authorization error fetching object");
//                    }
//
//                    @Override
//                    public void onFailure(PCFObject object, String reason) {
//                        showToast(reason);
//                    }
//                });
//
//            } catch (AuthorizationException e) {
//                showToast(e.getLocalizedMessage());
//            } catch (DataException e) {
//                showToast(e.getLocalizedMessage());
//            }
        }
        if (adapter != null) {
            adapter.setObject(pcfObject);
        }
    }

    private void saveObject() {
        // TODO - restore before checking in
//        pcfObject.setObjectId("123");
//        adapter.updateObject();
//        try {
//            final AuthorizedResourceClient client = DataSDK.getInstance().getClient(this);
//            pcfObject.save(client, new DataListener() {
//                @Override
//                public void onSuccess(PCFObject object) {
//                    showToast("Object saved successfully");
//                }
//
//                @Override
//                public void onUnauthorized(PCFObject object) {
//                    showToast("Authorization error saving object");
//                }
//
//                @Override
//                public void onFailure(PCFObject object, String reason) {
//                    showToast(reason);
//                }
//            });
//        } catch (Exception e) {
//            showToast(e.getLocalizedMessage());
//        }
    }

    private void addItem() {
        if (pcfObject != null && adapter != null && listView != null) {
            listView.smoothScrollToPosition(0);
            adapter.addItem();
        }
    }

    private void toggleDeleteMode() {
        Toast.makeText(this, "Long-touch the left-side of the key/value pair that you want to delete.", Toast.LENGTH_LONG).show();
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
