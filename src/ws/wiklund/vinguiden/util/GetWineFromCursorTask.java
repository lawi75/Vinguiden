package ws.wiklund.vinguiden.util;

import ws.wiklund.guides.model.Beverage;
import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.activities.WineActivity;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

public class GetWineFromCursorTask extends AsyncTask<Cursor, Void, Beverage> {
	private ProgressDialog dialog;
	private Activity activity;

	public GetWineFromCursorTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected Beverage doInBackground(Cursor... cursors) {
		return new WineDatabaseHelper(activity).getBeverageFromCursor(cursors[0]);
	}

	@Override
	protected void onPostExecute(Beverage bevarage) {
		dialog.hide();

		Intent intent = new Intent(activity, WineActivity.class);
		intent.putExtra("ws.wiklund.guides.model.Beverage", bevarage);

		activity.startActivityForResult(intent, 0);

		super.onPostExecute(bevarage);
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
