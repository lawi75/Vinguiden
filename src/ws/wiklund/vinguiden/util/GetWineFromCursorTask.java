package ws.wiklund.vinguiden.util;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.activities.WineTabsActivity;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Wine;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

public class GetWineFromCursorTask extends AsyncTask<Cursor, Void, Wine> {
	private ProgressDialog dialog;
	private Activity activity;

	public GetWineFromCursorTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected Wine doInBackground(Cursor... cursors) {
		return new WineDatabaseHelper(activity).getWineFromCursor(cursors[0]);
	}

	@Override
	protected void onPostExecute(Wine wine) {
		dialog.hide();

		Intent intent = new Intent(activity, WineTabsActivity.class);
		intent.putExtra("ws.wiklund.vinguiden.activities.Wine", wine);

		activity.startActivityForResult(intent, 0);

		super.onPostExecute(wine);
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(activity);
		dialog.setMessage(activity.getString(R.string.wait));
		dialog.setIndeterminate(true);
		dialog.setCancelable(false);
		dialog.show();

		super.onPreExecute();
	}

}
