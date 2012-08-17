package ws.wiklund.vinguiden.util;

import ws.wiklund.guides.util.Notifyable;
import ws.wiklund.guides.util.Selectable;
import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineCellarProvider;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class SelectableImpl implements Selectable {
	private String header;
	private int drawable;
	private int action;

	public SelectableImpl(String header, int drawable, int action) {
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
				values.put("beverage_id", id);
				values.put("no_bottles", 1);
				values.put("added_to_cellar", System.currentTimeMillis());
				context.getContentResolver().insert(WineCellarProvider.CONTENT_URI, values);
	
				Log.d(SelectableImpl.class.getName(), "Added one bottle of " + name + " to cellar");
				// Step 2, create activity to be able to add multiple bottles, set
				// reminder, set location
	
				((Notifyable)context).notifyDataSetChanged();
				break;
			case Selectable.REMOVE_ACTION:
				// TODO
				// Step 1, just remove one bottle
				Cursor c1 = context.getContentResolver().query(WineCellarProvider.CONTENT_URI,
						null, "beverage_id = ?", new String[] { String.valueOf(id) },
						null);
	
				if (c1.moveToFirst()) {
					int noBottles = c1.getInt(2);
					if (noBottles == 1) {
						int rows = context.getContentResolver().delete(WineCellarProvider.CONTENT_URI, "_id = ?", new String[] { String.valueOf(c1.getInt(0)) });
	
						if (rows < 1) {
							Toast.makeText(context, context.getString(R.string.deleteFailed) + " " + name, Toast.LENGTH_LONG).show();
						} else if (rows > 1) {
							Log.e(SelectableImpl.class.getName(), "Fatal error removed more then one row from cellar");
						}
						
						((Notifyable)context).notifyDataSetChanged();
					} else {
						// TODO add support for this when it is possible to add more
						// then one bottle per row
					}
				}
	
				// Step 2, create remove beverage from cellar dialog if beverages has been
				// added with different dates or on different locations
				break;
			case Selectable.DELETE_ACTION:
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
				
				alertDialog.setMessage(String.format(context.getString(R.string.deleteWine), name));
				alertDialog.setCancelable(false);
				alertDialog.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id1) {
								boolean b = helper.deleteBeverage(id);
	
								if (!b) {
									Toast.makeText(context, context.getString(R.string.deleteFailed) + " " + name, Toast.LENGTH_LONG).show();
								}
								
								((Notifyable)context).notifyDataSetChanged();
							}
						});
	
				alertDialog.setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id1) {
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
