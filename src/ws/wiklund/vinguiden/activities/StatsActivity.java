package ws.wiklund.vinguiden.activities;

import ws.wiklund.guides.activities.BeverageStatsActivity;
import ws.wiklund.guides.db.BeverageDatabaseHelper;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;

public class StatsActivity extends BeverageStatsActivity {

	@Override
	protected BeverageDatabaseHelper getDatabaseHelper() {
		return new WineDatabaseHelper(this);
	}

}
