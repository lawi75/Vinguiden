package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.db.CellarProvider;
import ws.wiklund.vinguiden.db.DatabaseUpgrader;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.list.WineListCursorAdapter;
import ws.wiklund.vinguiden.model.Wine;
import ws.wiklund.vinguiden.util.Selectable;
import ws.wiklund.vinguiden.util.Sortable;
import ws.wiklund.vinguiden.util.ViewHelper;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
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
import android.widget.Toast;

public class WineListActivity extends CustomListActivity {
	private static final String PRIVATE_PREF = "vinguiden";
	private static final String VERSION_KEY = "version_number";

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

		// Bootstrapping
		// PayPalFactory.init(this.getBaseContext());

		ViewHelper viewHelper = new ViewHelper(this);

		if (!viewHelper.isLightVersion()) {
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
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				handleLongClick(position);
				return true;
			}
		});

		initVersions();

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
				WineDatabaseHelper.SQL_SELECT_ALL_WINES_INCLUDING_NO_IN_CELLAR
						+ " ORDER BY " + sortColumn, null);
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
			TextView view = (TextView) WineListActivity.this
					.findViewById(R.id.title);

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
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menuStats).setEnabled(hasSomeStats());
		
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuStats:
			startActivityForResult(
					new Intent(
							WineListActivity.this.getApplicationContext(),
							StatsActivity.class), 0);
			break;
		case R.id.menuAbout:
			startActivityForResult(
					new Intent(
							WineListActivity.this.getApplicationContext(),
							AboutActivity.class), 0);

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

		final int wineId = c.getInt(0);
		final String name = c.getString(1);

		switch (selectable.getAction()) {
		case Selectable.ADD_ACTION:
			// TODO create add wine to cellar activity
			// Step 1, simple only add one bottle to cellar on click
			ContentValues values = new ContentValues();
			values.put("wine_id", wineId);
			values.put("no_bottles", 1);
			values.put("added_to_cellar", SystemClock.elapsedRealtime());
			getContentResolver().insert(CellarProvider.CONTENT_URI, values);

			Log.d(WineListActivity.class.getName(), "Added one bottle of "
					+ name + " to cellar");
			// Step 2, create activity to be able to add multiple bottles, set
			// reminder, set location

			notifyDataSetChanged();
			break;
		case Selectable.REMOVE_ACTION:
			// TODO
			// Step 1, just remove one bottle
			Cursor c1 = getContentResolver().query(CellarProvider.CONTENT_URI,
					null, "wine_id = ?", new String[] { String.valueOf(wineId) },
					null);

			if (c1.moveToFirst()) {
				int noBottles = c1.getInt(2);
				if (noBottles == 1) {
					int rows = getContentResolver().delete(
							CellarProvider.CONTENT_URI, "_id = ?",
							new String[] { String.valueOf(c1.getInt(0)) });

					if (rows < 1) {
						Toast.makeText(WineListActivity.this,
								getString(R.string.deleteFailed) + " " + name,
								Toast.LENGTH_LONG).show();
					} else if (rows > 1) {
						Log.e(WineListActivity.class.getName(),
								"Fatal error removed more then one row from cellar");
					}
				} else {
					// TODO add support for this when it is possible to add more
					// then one bottle per row
				}
			}

			// Step 2, create remove wine from cellar dialog if wines has been
			// added with different dates or on different locations

			notifyDataSetChanged();
			break;
		case Selectable.DELETE_ACTION:
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					WineListActivity.this);
			
			alertDialog.setMessage(String.format(getString(R.string.deleteWine), name));
			alertDialog.setCancelable(false);
			alertDialog.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							boolean b = helper.deleteWine(wineId);

							if (!b) {
								Toast.makeText(
										WineListActivity.this,
										getString(R.string.deleteFailed) + " "
												+ name, Toast.LENGTH_LONG)
										.show();
							} else {
								notifyDataSetChanged();
							}
						}
					});

			alertDialog.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// Action for 'NO' Button
							dialog.cancel();
						}
					});

			AlertDialog alert = alertDialog.create();

			// Title for AlertDialog
			alert.setTitle(getString(R.string.deleteTitle) + " " + name + "?");
			// Icon for AlertDialog
			alert.setIcon(R.drawable.icon);
			alert.show();

			// Return true to consume the click event. In this case the
			// onListItemClick listener is not called anymore.
			break;
		default:
			break;
		}
	}

	private class GetWineFromCursorTask extends AsyncTask<Integer, Void, Wine> {
		private ProgressDialog dialog;
		private Integer position;

		@Override
		protected Wine doInBackground(Integer... positions) {
			this.position = positions[0];

			// Get the item that was clicked
			Cursor c = (Cursor) WineListActivity.this.getListAdapter().getItem(
					position);

			return helper.getWineFromCursor(c);
		}

		@Override
		protected void onPostExecute(Wine wine) {
			dialog.hide();

			Intent intent = new Intent(
					WineListActivity.this.getApplicationContext(),
					WineTabsActivity.class);
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
	
	
	private class PostUpdateTask extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog dialog;
		private SQLiteDatabase innerDB;
		
		private Handler progressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
    			Log.d(DatabaseUpgrader.class.getName(), "current progress [" + dialog.getProgress() + "]");
    			dialog.incrementProgressBy(1);
            }
        };
        
		@Override
		protected Void doInBackground(Void... v) {
			Cursor c = null;
			
			try {
				c = getDatabase().rawQuery("SELECT _id, no FROM wine WHERE price IS NULL", null);
				Log.d(DatabaseUpgrader.class.getName(), "Will set default price for all wines with data from Systembolaget");
				Log.d(DatabaseUpgrader.class.getName(), "Will update [" + c.getCount() + "] wines with price");

				while(c.moveToNext()) {
					try {
						Wine wine = SystembolagetParser.parseResponse(c.getString(1));
						Log.d(DatabaseUpgrader.class.getName(), "Updating price for: " + wine);

						if(wine != null && wine.hasPrice()) {
							ContentValues values = new ContentValues();
							values.put("price", wine.getPrice());
							getDatabase().update(WineDatabaseHelper.WINE_TABLE, values, "_id=?", new String[] {String.valueOf(c.getInt(0))});
							Log.d(DatabaseUpgrader.class.getName(), "Price updated");
						}
					} catch (Exception e) {
			        	Log.w(DatabaseUpgrader.class.getName(), "Failed to get info for wine with no: " + c.getString(0), e);
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
				c = getDatabase().rawQuery("SELECT _id, no FROM wine WHERE price IS NULL", null);
				count = c.getCount();
			} finally {
				if (c != null) {
					c.close();
				}
				
				innerDB.close();
			}
			
			dialog = new ProgressDialog(WineListActivity.this);
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
				innerDB = helper.getWritableDatabase();
			}
			
			return innerDB;
		}
		
		protected void onProgressUpdate(Integer... progress) {
			progressHandler.sendEmptyMessage(0);
		}
		
	}

}
