package ws.wiklund.vinguiden.util;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.CellarProvider;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class Selectable {
	public static final int DELETE_ACTION = 3234;
	public static final int ADD_ACTION = 5435;
	public static final int REMOVE_ACTION = 1554;
	
	private String header;
	private int drawable;
	private int action;

	public Selectable(String header, int drawable, int action) {
		this.header = header;
		this.drawable = drawable;
		this.action = action;
	}

	public String getHeader() {
		return header;
	}

	public int getDrawable() {
		return drawable;
	}
	
	public int getAction() {
		return action;
	}

	public void select(final Context context, final WineDatabaseHelper helper, final int id, final String name) {
		switch (action) {
			case Selectable.ADD_ACTION:
				// TODO create add wine to cellar activity
				// Step 1, simple only add one bottle to cellar on click
				ContentValues values = new ContentValues();
				values.put("wine_id", id);
				values.put("no_bottles", 1);
				values.put("added_to_cellar", SystemClock.elapsedRealtime());
				context.getContentResolver().insert(CellarProvider.CONTENT_URI, values);
	
				Log.d(Selectable.class.getName(), "Added one bottle of " + name + " to cellar");
				// Step 2, create activity to be able to add multiple bottles, set
				// reminder, set location
	
				break;
			case Selectable.REMOVE_ACTION:
				// TODO
				// Step 1, just remove one bottle
				Cursor c1 = context.getContentResolver().query(CellarProvider.CONTENT_URI,
						null, "wine_id = ?", new String[] { String.valueOf(id) },
						null);
	
				if (c1.moveToFirst()) {
					int noBottles = c1.getInt(2);
					if (noBottles == 1) {
						int rows = context.getContentResolver().delete(CellarProvider.CONTENT_URI, "_id = ?", new String[] { String.valueOf(c1.getInt(0)) });
	
						if (rows < 1) {
							Toast.makeText(context, context.getString(R.string.deleteFailed) + " " + name, Toast.LENGTH_LONG).show();
						} else if (rows > 1) {
							Log.e(Selectable.class.getName(), "Fatal error removed more then one row from cellar");
						}
					} else {
						// TODO add support for this when it is possible to add more
						// then one bottle per row
					}
				}
	
				// Step 2, create remove wine from cellar dialog if wines has been
				// added with different dates or on different locations
				break;
			case Selectable.DELETE_ACTION:
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
				
				alertDialog.setMessage(String.format(context.getString(R.string.deleteWine), name));
				alertDialog.setCancelable(false);
				alertDialog.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								boolean b = helper.deleteWine(id);
	
								if (!b) {
									Toast.makeText(context, context.getString(R.string.deleteFailed) + " " + name, Toast.LENGTH_LONG).show();
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
				alert.setTitle(context.getString(R.string.deleteTitle) + " " + name + "?");
				// Icon for AlertDialog
				alert.setIcon(R.drawable.icon);
				alert.show();
	
				// Return true to consume the click event. In this case the
				// onListItemClick listener is not called anymore.
				break;
		}
	}

	@Override
	public String toString() {
		return "Selectable [header=" + header + ", drawable=" + drawable
				+ ", action=" + action + "]";
	}

}
