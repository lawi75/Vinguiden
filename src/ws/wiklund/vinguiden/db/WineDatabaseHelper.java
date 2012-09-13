package ws.wiklund.vinguiden.db;

import ws.wiklund.guides.db.BeverageDatabaseHelper;
import ws.wiklund.guides.db.DatabaseUpgrader;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class WineDatabaseHelper extends BeverageDatabaseHelper {
	private static final long serialVersionUID = 4079857874185816324L;

	public static final String DATABASE_NAME = "wineguide.db";
	private static final int DATABASE_VERSION = WineDatabaseUpgrader.VERSION_5;

	public WineDatabaseHelper(Context context) {
		this(context, DATABASE_NAME);
	}

	//Used for testing so that db can be created and dropped with out destroying dev data
	public WineDatabaseHelper(Context context, String dbName) {
		super(context, dbName, DATABASE_VERSION);
	}

	@Override
	public DatabaseUpgrader getDatabaseUpgrader(SQLiteDatabase db) {
		return new WineDatabaseUpgrader(db);
	}

}
