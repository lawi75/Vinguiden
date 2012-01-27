package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class DonateActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.donate);
		
	}

}
