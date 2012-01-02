package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class CustomListActivity extends ListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.winelist);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
	}
	
	public void addWine(View view) {
    	Intent intent = new Intent(view.getContext(), AddWineActivity.class);
    	startActivityForResult(intent, 0);
    }

}
