package ws.wiklund.vinguiden.activities;

import ws.wiklund.guides.activities.BeverageActivity;
import ws.wiklund.guides.db.BeverageDatabaseHelper;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;

public class WineActivity extends BeverageActivity {

	@Override
	protected BeverageDatabaseHelper getDatabaseHelper() {
		return new WineDatabaseHelper(this);
	}

	@Override
	protected Class<?> getModifyBeverageActivityClass() {
		return ModifyWineActivity.class;
	}
	
}