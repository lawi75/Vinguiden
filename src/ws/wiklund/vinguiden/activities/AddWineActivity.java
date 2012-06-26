package ws.wiklund.vinguiden.activities;

import java.io.IOException;
import java.util.regex.Pattern;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.bolaget.SystembolagetParser;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.model.Wine;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.Toast;

public class AddWineActivity extends BaseActivity {
	private WineDatabaseHelper helper;
	private EditText searchStr;
	
	private ProgressDialog dialog;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addwine);
        
        if(!isLightVersion()) {
    		findViewById(R.id.adView).setVisibility(View.GONE);
    		findViewById(R.id.adView1).setVisibility(View.GONE);
        }

        helper = new WineDatabaseHelper(this);
        searchStr = (EditText)findViewById(R.id.EditNo);
        
        searchStr.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER  && event.getAction() == 0) {
					searchWine(v);
					return true;
				}
				
				return false;
			}
		});
        
    }
    
    @Override
	protected void onPause() {
    	if(dialog != null && dialog.isShowing()) {
    		dialog.dismiss();
    	}
    	
		super.onPause();
	}

	public void searchWine(View view) {    	
		search(searchStr.getText().toString());
    }
    
    public void search(String no) {
    	if(isValidNo(no)) {
    		new DownloadWineTask().execute(no);
    	}
    }
	
	private boolean isValidNo(String no) {
		if(no != null && no.length() > 0 && Pattern.matches("^\\d*$", no)) {
			try {
				if(!exists(no)) {
					return true;
				} else {				
					Toast.makeText(getApplicationContext(), getString(R.string.wineExist) + " " + no, Toast.LENGTH_SHORT).show();  		
				}
			} catch (NumberFormatException e) {
	        	Log.d(AddWineActivity.class.getName(), "Invalid search string: " + no);		        	
				Toast.makeText(getApplicationContext(), String.format(getString(R.string.invalidNoError), no), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), getString(R.string.provideNo), Toast.LENGTH_SHORT).show();  		
		}
		
		return false;
	}

	private boolean exists(String no) throws NumberFormatException {
		return helper.getWineIdFromNo(Integer.valueOf(no)) != -1;
	}


	private class DownloadWineTask extends AsyncTask<String, Void, Wine> {
		private String no;
		
		private String errorMsg;

		@Override
		protected Wine doInBackground(String... no) {
			this.no = no[0];

	        try {
				if(this.no == null) {
		        	Log.w(AddWineActivity.class.getName(), "Failed to get info for wine,  no is null");		        	
		        	errorMsg = getString(R.string.genericParseError);
				} else {
					return SystembolagetParser.parseResponse(this.no);
				}
			} catch (IOException e) {
	        	Log.w(AddWineActivity.class.getName(), "Failed to get info for wine with no: " + this.no, e);
	        	errorMsg = getString(R.string.genericParseError);
			}

	        return null;
		}

		@Override
		protected void onPostExecute(Wine wine) {
			Intent intent = new Intent(AddWineActivity.this.getApplicationContext(), ModifyWineActivity.class);

			if (wine != null) {
				intent.putExtra("ws.wiklund.vinguiden.activities.Wine", wine);
		    	startActivityForResult(intent, 0);
			} else {
				Toast.makeText(getApplicationContext(), errorMsg == null ? String.format(getString(R.string.missingNoError), this.no) : errorMsg, Toast.LENGTH_SHORT).show();
				errorMsg = null;
				dialog.dismiss();
			}
			
			super.onPostExecute(wine);
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(AddWineActivity.this);
			dialog.setMessage("Vänligen vänta...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(false);
			dialog.show();

			super.onPreExecute();
		}
		
	}
    
}
