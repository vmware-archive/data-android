package com.pivotal.cf.mobile.datasdk.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.pivotal.cf.mobile.common.util.Logger;
import com.pivotal.cf.mobile.datasdk.data.PCFObject;
import com.pivotal.cf.mobile.datasdk.sample.R;
import com.pivotal.cf.mobile.datasdk.sample.view.EditorCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataEditorActivity extends ActionBarActivity {

    private static final String MY_DATA_OBJECT = "MY_DATA_OBJECT";
    private static final String CLASS_NAME = "objects";
    private static final String OBJECT_ID = "123";
    public static final String LABEL_KEY = "Key";
    public static final String LABEL_VALUE = "Value";
    public static final String LABEL_CLASS_NAME = "Class Name";
    public static final String LABEL_OBJECT_ID = "Object ID";

    private PCFObject pcfObject;
    private ScrollView scrollView;
    private LinearLayout container;
    private EditorCell headerCell;
    private List<EditorCell> editorCells;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.setup(this);
        setContentView(R.layout.activity_data_editor);
        if (savedInstanceState != null) {
            pcfObject = savedInstanceState.getParcelable(MY_DATA_OBJECT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
        if (pcfObject == null) {
            fetchObject();
        } else {
            populateContainer();
        }
    }

    private void setupViews() {
        scrollView = (ScrollView) findViewById(R.id.scrollview);
        container = (LinearLayout) findViewById(R.id.container);
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
            deleteModeHint();
        } else if (id == R.id.action_view_json) {
            viewJson();
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewJson() {
        updateObject();
        final Intent i = new Intent(this, ViewJsonActivity.class);
        i.putExtra(ViewJsonActivity.MY_DATA_OBJECT, pcfObject);
        startActivity(i);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateObject();
        outState.putParcelable(MY_DATA_OBJECT, pcfObject);
    }

    private void fetchObject() {

        pcfObject = new PCFObject(CLASS_NAME);
        pcfObject.setObjectId(OBJECT_ID);

        populateContainer();

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

    private void saveObject() {
        // TODO - restore before checking in
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
        if (pcfObject != null) {
            final EditorCell cell = getItemCell(1, "", "");
            editorCells.add(0, cell);
            container.addView(cell, 1);
            scrollView.smoothScrollTo(0, 0);
            fixPositions();
            cell.focus();
        }
    }

    private void fixPositions() {
        int position = 1;
        for(final EditorCell cell : editorCells) {
            cell.setPosition(position);
            position += 1;
        }
    }

    private void populateContainer() {
        container.removeAllViews();
        headerCell = getHeaderCell();
        container.addView(headerCell);
        int position = 1;
        editorCells = new ArrayList<EditorCell>();
        for (final Map.Entry<String, Object> entry : pcfObject.entrySet()) {
            final EditorCell itemCell = getItemCell(position, entry.getKey(), entry.getValue().toString());
            editorCells.add(itemCell);
            container.addView(itemCell);
            position += 1;
        }
    }

    private EditorCell getHeaderCell() {
        final EditorCell cell = getCell(0, LABEL_CLASS_NAME, LABEL_OBJECT_ID, pcfObject.getClassName(), pcfObject.getObjectId());
        cell.setHints("May not be blank.", "May not be blank.");
        return cell;
    }

    private EditorCell getItemCell(int position, String value1, String value2) {
        final EditorCell cell = getCell(position, LABEL_KEY, LABEL_VALUE, value1, value2);
        cell.setHints("May not be blank. Must be unique.", "");
        return cell;
    }

    private EditorCell getCell(int position, String label1, String label2, String value1, String value2) {
        final EditorCell cell = new EditorCell(this);
        cell.setLabels(label1, label2);
        cell.setPosition(position);
        cell.setValue1(value1);
        cell.setValue2(value2);
        return cell;
    }

    private void updateObject() {
        // Updates the PCFObject object to reflect the contents of the data editor cells
        final String className = headerCell.getValue1();
        final String objectId = headerCell.getValue2().toString();
        if (className != null && !className.isEmpty()) {
            pcfObject.setClassName(className);
        }
        if (objectId != null && !objectId.isEmpty()) {
            pcfObject.setObjectId(objectId);
        }
        pcfObject.clear();
        for(final EditorCell cell : editorCells) {
            final String key = cell.getValue1();
            final Object value = cell.getValue2();
            if (key != null && !key.isEmpty() && value != null) {
                pcfObject.put(key, value);
            }
        }
    }

    private void deleteModeHint() {
        showToast("Long-touch the left-side of the key/value pair that you want to delete.");
        // TODO - re-implement.  should actually delete.
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
