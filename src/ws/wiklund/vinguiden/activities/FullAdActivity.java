package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import android.os.Bundle;
import android.view.Window;

public class FullAdActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.fullad);
		
	}

}
