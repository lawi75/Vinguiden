package ws.wiklund.vinguiden.activities;

import java.io.InputStream;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        /*CheckoutButton btn = getCheckoutButton();
        if (btn != null) {
			TableRow row = (TableRow) findViewById(R.id.donate);
			row.addView(btn);
		}*/
        
		TextView eula = (TextView)findViewById(R.id.eula);
        
        try {
            InputStream in = getResources().openRawResource(R.raw.eula);

            byte[] b = new byte[in.available()];
            in.read(b);
            eula.setText(new String(b));
        } catch (Exception e) {
        	Log.w(AboutActivity.class.getName(), "Failed to read EULA", e);
        }
        
        //Version info
        TextView version = (TextView)findViewById(R.id.version);

        try {
            PackageManager manager = getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);			
	        version.setText(getString(R.string.app_version) + " " + info.versionCode + "." + info.versionName + "." + new WineDatabaseHelper(this).getVersion());
        } catch (NameNotFoundException e) {
        	version.setVisibility(View.GONE);
		}
    }

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

}
