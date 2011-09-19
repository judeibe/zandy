package org.zotero.client.data;

import org.zotero.client.R;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Exposes collection to be displayed by a ListView
 * @author ajlyon
 *
 */
public class CollectionAdapter extends ResourceCursorAdapter {
	private static final String TAG = "org.zotero.client.data.CollectionAdapter";

	private Database db;
	public Context context;
	private ItemCollection parent;
		
	public CollectionAdapter(Context context, Cursor cursor) {
		super(context, R.layout.list_collection, cursor, false);
		this.context = context;
	}
	
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        LayoutInflater li = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return li.inflate(R.layout.list_collection, parent, false);
    }

    /**
     * Call this when the data has been updated-- it refreshes the cursor and notifies of the change
     */
    public void notifyDataSetChanged() {
    	super.notifyDataSetChanged();
    }
    
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		//Log.d(TAG, "bindView view is: " + view.getId());
		TextView tvTitle = (TextView)view.findViewById(R.id.collection_title);
		TextView tvDirty = (TextView)view.findViewById(R.id.collection_dirty);
	
		if (cursor == null) {
			Log.e(TAG, "cursor is null in bindView");
		}
		ItemCollection collection = ItemCollection.load(cursor);
		if (collection == null) {
			Log.e(TAG, "collection is null in bindView");
		}
		if (tvTitle == null) {
			Log.e(TAG, "tvTitle is null in bindView");
		}
		tvTitle.setText(collection.getTitle());

		//tvDirty.setText(collection.dirty);
	}

	/**
	 * Requeries the database for top-level collections
	 */
	public void refresh() {
		if (this.getCursor() != null)
			this.getCursor().close();
		String[] args = { "false" };			
		Cursor cursor = db.query("collections", Database.COLLCOLS, "collection_parent=?", args, null, null, "collection_name", null);
		if (cursor == null) {
			Log.e(TAG, "cursor is null");
		}
		this.changeCursor(cursor);
	}
	
	/**
	 * Requeries the database for the specified collection
	 */
	public void refresh(ItemCollection parent) {
		this.parent = parent;
		if (this.getCursor() != null)
			this.getCursor().close();
		String[] args = { parent.getKey() };
		Cursor cursor = db.query("collections", Database.COLLCOLS, "collection_parent=?", args, null, null, "collection_name", null);
		if (cursor == null) {
			Log.e(TAG, "cursor is null");
		}
		this.changeCursor(cursor);
	}
	
	/**
	 * Requery/refresh one level up
	 */
	public void goUp() {
		if (this.parent == null) {
			// do nothing
		} else {
			ItemCollection grandparent = this.parent.getParent();
			if (grandparent == null) {
				this.parent = null;
				refresh();
			} else {
				refresh(grandparent);
			}
		}
	}
	
	/**
	 * Gives an adapter for top-level collections
	 * @param context
	 * @return
	 */
	public static CollectionAdapter create(Context context) {
		Database db = new Database(context);
		String[] args = { "false" };
		Cursor cursor = db.query("collections", Database.COLLCOLS, "collection_parent=?", args, null, null, "collection_name", null);
		if (cursor == null) {
			Log.e(TAG, "cursor is null");
		}
		Log.e(TAG, "created collectionadapter");
		CollectionAdapter adapter = new CollectionAdapter(context, cursor);
		adapter.db = db;
		return adapter;
	}

	/**
	 * Gives an adapter for child collections of a given parent
	 * @param context
	 * @param parent
	 * @return
	 */
	public static CollectionAdapter create(Context context, ItemCollection parent) {
		Database db = new Database(context);
		String[] args = { parent.getKey() };
		Cursor cursor = db.query("collections", Database.COLLCOLS, "collection_parent=?", args, null, null, "collection_name", null);
		if (cursor == null) {
			Log.e(TAG, "cursor is null");
		}
		Log.e(TAG, "created collectionadapter for child");
		CollectionAdapter adapter = new CollectionAdapter(context, cursor);
		adapter.parent = parent;
		adapter.db = db;
		return adapter;
	}

}
