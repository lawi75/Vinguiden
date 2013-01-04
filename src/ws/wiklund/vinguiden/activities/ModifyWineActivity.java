package ws.wiklund.vinguiden.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import ws.wiklund.guides.R;
import ws.wiklund.guides.activities.FullAdActivity;
import ws.wiklund.guides.activities.ModifyBeverageActivity;
import ws.wiklund.guides.db.BeverageDatabaseHelper;
import ws.wiklund.guides.util.ViewHelper;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;

public class ModifyWineActivity extends ModifyBeverageActivity {
	private final static List<String> strengths = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView view = (TextView) findViewById(R.id.Text_category);
		Spinner spinner = (Spinner) findViewById(R.id.Spinner_category);
		
		view.setVisibility(View.GONE);
		spinner.setVisibility(View.GONE);
	}

	@Override
	protected BeverageDatabaseHelper getDatabaseHelper() {
		return new WineDatabaseHelper(this);
	}
	
	@Override
	protected Class<?> getIntentClass() {
		return FullAdActivity.class;
	}
	
	@Override
	protected List<String> getStrengths() {
		if (strengths.isEmpty()) {
			for (Double i = 9.0; i <= 20.0; i += 0.1) {
				strengths.add(ViewHelper.getDecimalStringFromNumber(i) + " %");
			}
		}
		
		return strengths;
	}	
	
}