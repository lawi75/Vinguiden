package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.list.WineListCursorAdapter;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.util.PayPalFactory;
import ws.wiklund.vinguiden.util.Sortable;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class WineListActivity extends CustomListActivity {
	private WineDatabaseHelper helper;
	private SQLiteDatabase db;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;
	
	private String currentSortColumn = "wine.name asc";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.winelist);
        
    	//Bootstrapping
    	PayPalFactory.init(this.getBaseContext());

        if(Integer.valueOf(getString(R.string.version)) != BaseActivity.lightVersion) {
        	findViewById(R.id.adView).setVisibility(View.GONE);
        	findViewById(R.id.adView1).setVisibility(View.GONE);        	
        }
        
        helper = new WineDatabaseHelper(this);
        
        cursor = getNewCursor(currentSortColumn);

        startManagingCursor(cursor);

        // Now create a new list adapter bound to the cursor.
        adapter = new WineListCursorAdapter(this, cursor);

        // Bind to our new adapter.
        setListAdapter(adapter);
        
		ListView list = getListView();
		list.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {				
				ListView listView = WineListActivity.this.getListView();
				final Cursor c = (Cursor) listView.getItemAtPosition(position);

				AlertDialog.Builder alertDialog = new AlertDialog.Builder(WineListActivity.this);
				alertDialog.setMessage(getString(R.string.deleteWine) + " " + c.getString(1) + "?");
				alertDialog.setCancelable(false);
				alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						boolean b = helper.deleteWine(c.getInt(0));

						if (!b) {
							Toast.makeText(WineListActivity.this, getString(R.string.deleteFailed)  + " " + c.getString(1), Toast.LENGTH_LONG).show();
						} else {
							notifyDataSetChanged();
						}
					}
				});
				
				alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//  Action for 'NO' Button
						dialog.cancel();
					}
				});
				
				AlertDialog alert = alertDialog.create();
				
				// Title for AlertDialog
				alert.setTitle(getString(R.string.deleteTitle) + " " + c.getString(1) + "?");
				// Icon for AlertDialog
				alert.setIcon(R.drawable.icon);
				alert.show();				
				
				// Return true to consume the click event. In this case the
				// onListItemClick listener is not called anymore.
				return true;
			}
		});
	}
    
	private Cursor getNewCursor(String sortColumn) {
        db = helper.getReadableDatabase();        
        return db.rawQuery(WineDatabaseHelper.SQL_SELECT_ALL_WINES + " ORDER BY " + sortColumn, null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		new GetWineFromCursorTask().execute(position);		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		notifyDataSetChanged();
	}

	private void notifyDataSetChanged() {
		if(!db.isOpen() || adapter.getCount() == 0) {
			stopManagingCursor(cursor);
			cursor = getNewCursor(currentSortColumn);
			
			startManagingCursor(cursor);
			adapter.changeCursor(cursor);
		}
		
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		stopManagingCursor(cursor);
		
		adapter.getCursor().close();
		
		if (cursor != null) {
			cursor.close();
		}
		
		if (db != null) {
			db.close();
		}
		
		super.onDestroy();
	}
	
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onRestart() {
		notifyDataSetChanged();
		super.onRestart();
	}
	
	@Override
	protected void onResume() {
		notifyDataSetChanged();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.wine_list_menu, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuAbout:
				Intent intent = new Intent(WineListActivity.this.getApplicationContext(), AboutActivity.class);
		    	startActivityForResult(intent, 0);		
				
				return true;
			}
		
		return false;
	}	

	@Override
	void sort(Sortable sortable) {
		currentSortColumn = sortable.getSortColumn();

		cursor = getNewCursor(currentSortColumn);
		adapter.changeCursor(cursor);
		
		adapter.notifyDataSetChanged();
	}
	

	private class GetWineFromCursorTask extends AsyncTask<Integer, Void, Wine> {
		private ProgressDialog dialog;
		private Integer position;

		@Override
		protected Wine doInBackground(Integer... positions) {
			this.position = positions[0];
			
	    	// Get the item that was clicked
			Cursor c = (Cursor) WineListActivity.this.getListAdapter().getItem(position);
			return helper.getWineFromCursor(c);
		}

		@Override
		protected void onPostExecute(Wine wine) {
			dialog.hide();

			Intent intent = new Intent(WineListActivity.this.getApplicationContext(), WineActivity.class);
			intent.putExtra("ws.wiklund.vinguiden.activities.Wine", wine);
			
	    	startActivityForResult(intent, 0);		
			
			super.onPostExecute(wine);
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(WineListActivity.this);
			dialog.setMessage(getString(R.string.wait));
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();

			super.onPreExecute();
		}
		
	}

}
