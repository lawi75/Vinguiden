package ws.wiklund.vinguiden.activities;

import java.io.IOException;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addwine);
        
        if(Integer.valueOf(getString(R.string.version)) != BaseActivity.lightVersion) {
    		findViewById(R.id.adView).setVisibility(View.GONE);
        }

        helper = new WineDatabaseHelper(this);
        searchStr = (EditText)findViewById(R.id.EditNo);
        
        searchStr.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER) {
					searchWine(v);
				}
				
				return false;
			}
		});
        
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
			if(!exists(no)) {
				return true;
			} else {
				Toast.makeText(getApplicationContext(), "Du har redan lagt till vin med nummer " + no, Toast.LENGTH_SHORT).show();  		
			}
		} else {
			Toast.makeText(getApplicationContext(), "Du måste ange ett nummer", Toast.LENGTH_SHORT).show();  		
		}
		
		return false;
	}

	private boolean exists(String no) {
		return helper.getWineIdFromNo(Integer.valueOf(no)) != -1;
	}


	private class DownloadWineTask extends AsyncTask<String, Void, Wine> {
		private ProgressDialog dialog;
		private String no;
		
		private String errorMsg;

		@Override
		protected Wine doInBackground(String... no) {
			this.no = no[0];
	    	//http://www.systembolaget.se/70989
	        try {
	    		Document doc = Jsoup.connect(SystembolagetParser.BASE_URL + "/" + this.no).get();

				if(isValidResponse(doc)) {
					return SystembolagetParser.parseResponse(doc, this.no);
				}
			} catch (IOException e) {
	        	Log.w(AddWineActivity.class.getName(), "Failed to get info for wine with no: " + this.no, e);
	        	errorMsg = "Misslyckades med att hämta information från www.systembolaget.se, var god försök igen";
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
				Toast.makeText(getApplicationContext(), errorMsg == null ? "Nummer " + this.no + " finns inte hos Systembolaget" : errorMsg, Toast.LENGTH_SHORT).show();
				errorMsg = null;
			}
			
			dialog.hide();

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
		
		private boolean isValidResponse(Document doc) {
			return doc.select("div.top_exception_message").first() == null;
		}

	}
    
}
