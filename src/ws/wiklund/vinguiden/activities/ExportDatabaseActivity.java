package ws.wiklund.vinguiden.activities;

import java.io.File;
import java.io.IOException;

import ws.wiklund.guides.activities.BaseActivity;
import ws.wiklund.guides.util.ViewHelper;
import ws.wiklund.vinguiden.R;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class ExportDatabaseActivity extends BaseActivity {
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export);
        
        File dbFile = new File(Environment.getDataDirectory() + "/data/ws.wiklund.vinguiden/databases/wineguide.db");
        File exportDir = new File(Environment.getExternalStorageDirectory(), "");
        
        if (exportDir.exists() || exportDir.mkdirs()) {
	        File file = new File(exportDir, "export.db");
	
	        try {
	        	file.createNewFile();
	        	ViewHelper.copyFile(dbFile, file);
	        } catch (IOException e) {
				Log.e(ExportDatabaseActivity.class.getName(), "Failed to export Database", e);
				Toast.makeText(getApplicationContext(), getString(R.string.failedToExport), Toast.LENGTH_SHORT).show();  		
	        }
        } else {
			Log.e(ExportDatabaseActivity.class.getName(), "Failed to export Database, couldn't create directories");
			Toast.makeText(getApplicationContext(), getString(R.string.failedToExport), Toast.LENGTH_SHORT).show();  		
        }
        
		finish();
    }

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
	
}
