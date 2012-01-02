package ws.wiklund.vinguiden.activities;

import java.io.InputStream;

import ws.wiklund.vinguiden.R;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        TextView eula= (TextView)findViewById(R.id.eula);

        try {
            InputStream in = getResources().openRawResource(R.raw.eula);

            byte[] b = new byte[in.available()];
            in.read(b);
            eula.setText(new String(b));
        } catch (Exception e) {
        	Log.w(AboutActivity.class.getName(), "Failed to read EULA", e);
        }        
    }

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

}