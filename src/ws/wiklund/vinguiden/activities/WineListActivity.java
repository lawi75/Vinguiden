package ws.wiklund.vinguiden.activities;

import java.io.File;

import ws.wiklund.guides.bolaget.SystembolagetParser;
import ws.wiklund.guides.list.BeverageListCursorAdapter;
import ws.wiklund.guides.model.Beverage;
import ws.wiklund.guides.util.AppRater;
import ws.wiklund.guides.util.ExportDatabaseCSVTask;
import ws.wiklund.guides.util.Notifyable;
import ws.wiklund.guides.util.Selectable;
import ws.wiklund.guides.util.Sortable;
import ws.wiklund.guides.util.ViewHelper;
import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.util.GetWineFromCursorTask;
import ws.wiklund.vinguiden.util.SelectableImpl;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WineListActivity extends CustomListActivity implements Notifyable {
	private static final String PRIVATE_PREF = "vinguiden";
	private static final String VERSION_KEY = "version_number";

	private WineDatabaseHelper helper;
	private SQLiteDatabase db;
	private Cursor cursor;
	private SimpleCursorAdapter adapter;

	private String currentSortColumn = "beverage.name asc";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initVersions();
		AppRater.app_launched(this, getString(R.string.app_name), "market://details?id=ws.wiklund.vinguiden");

		// Bootstrapping
		// PayPalFactory.init(this.getBaseContext());
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			startActivityForResult(new Intent(getApplicationContext(), WineFlowActivity.class), 0);
		} else {		
			setContentView(R.layout.winelist);
	
			helper = new WineDatabaseHelper(this);
			cursor = getNewCursor(currentSortColumn);
	
			startManagingCursor(cursor);
	
			// Now create a new list adapter bound to the cursor.
			adapter = new BeverageListCursorAdapter(this, cursor, wineTypes);
	
			// Bind to our new adapter.
			setListAdapter(adapter);
	
			ListView list = getListView();
			list.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
					handleLongClick(position);
					return true;
				}
			});
		}
	}

	private void initVersions() {
		SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
		int currentVersionNumber = 0;
		int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);

		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			currentVersionNumber = pi.versionCode;
		} catch (Exception e) {
		}

		if (currentVersionNumber > savedVersionNumber) {
			doPostUppgrade(currentVersionNumber);
			
			showWhatsNewDialog();

			Editor editor = sharedPref.edit();

			editor.putInt(VERSION_KEY, currentVersionNumber);
			editor.commit();
		}
	}

	private void doPostUppgrade(int currentVersionNumber) {
		switch (currentVersionNumber) {
			case 6:
				//Version 2.0 with new cellar function. Need to update price for all wines
				new PostUpdateTask().execute(new Void[0]);
				break;
			default:
				break;
		}
		
	}

	private void showWhatsNewDialog() {
    	LayoutInflater inflater = LayoutInflater.from(this);		
        View view = inflater.inflate(R.layout.whatsnew, null);
      	
  	  	Builder builder = new AlertDialog.Builder(this);

	  	builder.setView(view).setTitle(getString(R.string.whatsnew)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
	  		@Override
	  		public void onClick(DialogInterface dialog, int which) {
	  			dialog.dismiss();
	  		}
	    });
  	
	  	builder.create().show();
	}

	private Cursor getNewCursor(String sortColumn) {
		db = helper.getReadableDatabase();
		return db.rawQuery(
				WineDatabaseHelper.SQL_SELECT_ALL_BEVERAGES_INCLUDING_NO_IN_CELLAR
						+ " ORDER BY " + sortColumn, null);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Cursor c = (Cursor) WineListActivity.this.getListAdapter().getItem(position);

		new GetWineFromCursorTask(this).execute(c);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		notifyDataSetChanged();
	}

	public void notifyDataSetChanged() {
		if(db == null || adapter == null) {
			return;
		}
		
		if (!db.isOpen() || adapter.getCount() == 0) {
			stopManagingCursor(cursor);
			cursor = getNewCursor(currentSortColumn);

			startManagingCursor(cursor);
			adapter.changeCursor(cursor);
		}

		adapter.notifyDataSetChanged();

		int bottles = helper.getNoBottlesInCellar();
		// Update title with no wines in cellar
		if (bottles > 0) {
			TextView view = (TextView) WineListActivity.this.findViewById(R.id.title);

			String text = view.getText().toString();
			if (text.contains("(")) {
				text = text.substring(0, text.indexOf("(") - 1);
			}

			view.setText(text + " (" + bottles + ")");
		}
	}

	@Override
	protected void onDestroy() {
		stopManagingCursor(cursor);

		if (adapter != null) {
			Cursor c = adapter.getCursor();
			if(c != null) {
				c.close();
			}
		}
		
		if (cursor != null) {
			cursor.close();
		}

		if (db != null) {
			db.close();
		}

		super.onDestroy();
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
		inflater.inflate(R.menu.beverage_list_menu, menu);

		return true;
	}
		
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuStats).setEnabled(hasSomeStats());
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuStats:
			startActivityForResult(new Intent(WineListActivity.this.getApplicationContext(),StatsActivity.class), 0);
			break;
		case R.id.menuExport:
			final AlertDialog alertDialog = new AlertDialog.Builder(WineListActivity.this).create();
			alertDialog.setTitle(getString(R.string.export));
			
			final File exportFile = new File(ViewHelper.getRoot(), "export_guide.csv");
			alertDialog.setMessage(String.format(getString(R.string.export_message), new Object[]{exportFile.getAbsolutePath()}));
			
			alertDialog.setButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
			       new ExportDatabaseCSVTask(WineListActivity.this, helper, exportFile, WineListActivity.this.getListAdapter().getCount(), wineTypes).execute();
				} 
			});
			
			alertDialog.setButton2(getString(android.R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					alertDialog.cancel();
				} 
			});

			alertDialog.setCancelable(true);
			alertDialog.setIcon(R.drawable.export);
			alertDialog.show();
			break;
		case R.id.menuAbout:
			startActivityForResult(new Intent(WineListActivity.this.getApplicationContext(),AboutActivity.class), 0);
			break;
		}

		return true;
	}

	private boolean hasSomeStats() {
		return adapter.getCount() > 0;
	}

	@Override
	void sort(Sortable sortable) {
		currentSortColumn = sortable.getSortColumn();

		cursor = getNewCursor(currentSortColumn);
		adapter.changeCursor(cursor);

		adapter.notifyDataSetChanged();
	}

	@Override
	void select(Selectable selectable, int position) {
		ListView listView = WineListActivity.this.getListView();
		final Cursor c = (Cursor) listView.getItemAtPosition(position);

		((SelectableImpl) selectable).select(this, helper, c.getInt(0), c.getString(1));
	}

	
	private class PostUpdateTask extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog dialog;
		private SQLiteDatabase innerDB;
		
		private Handler progressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
    			Log.d(WineListActivity.class.getName(), "current progress [" + dialog.getProgress() + "]");
    			dialog.incrementProgressBy(1);
            }
        };
        
		@Override
		protected Void doInBackground(Void... v) {
			Cursor c = null;
			
			try {
				c = getDatabase().rawQuery("SELECT _id, no FROM beverage WHERE price IS NULL", null);
				Log.d(WineListActivity.class.getName(), "Will set default price for all wines with data from Systembolaget");
				Log.d(WineListActivity.class.getName(), "Will update [" + c.getCount() + "] wines with price");

				while(c.moveToNext()) {
					try {
						Beverage beverage = SystembolagetParser.parseResponse(c.getString(1), wineTypes);
						Log.d(WineListActivity.class.getName(), "Updating price for: " + beverage);

						if(beverage != null && beverage.hasPrice()) {
							beverage.setId(c.getInt(0));
							helper.updateBeverage(beverage);
							Log.d(WineListActivity.class.getName(), "Price updated");
						}
					} catch (Exception e) {
			        	Log.w(WineListActivity.class.getName(), "Failed to get info for wine with no: " + c.getString(0), e);
					}

					publishProgress(0);
				}
			} finally {
				if (c != null) {
					c.close();
				}
				
				innerDB.close();
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			dialog.hide();
		}
		
		@Override
		protected void onPreExecute() {
			Cursor c = null;
			int count = 0;
			try {
				c = getDatabase().rawQuery("SELECT _id, no FROM beverage WHERE price IS NULL", null);
				count = c.getCount();
			} finally {
				if (c != null) {
					c.close();
				}
				
				innerDB.close();
			}
			
			dialog = new ProgressDialog(WineListActivity.this, R.style.CustomDialog);
			dialog.setMessage(getString(R.string.upgrading));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			dialog.setCancelable(false);
			dialog.setMax(count);
			dialog.setProgress(0);
			dialog.show();

			super.onPreExecute();
		}
		
		private SQLiteDatabase getDatabase() {
			if(innerDB == null || !innerDB.isOpen()) {
				innerDB = helper.getReadableDatabase();
			}
			
			return innerDB;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			progressHandler.sendEmptyMessage(0);
		}
		
	}

}
