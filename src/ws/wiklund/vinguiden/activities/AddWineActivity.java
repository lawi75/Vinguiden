package ws.wiklund.vinguiden.activities;

import java.util.regex.Pattern;

import ws.wiklund.guides.activities.BaseActivity;
import ws.wiklund.guides.util.DownloadBeverageTask;
import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.db.WineDatabaseHelper;
import ws.wiklund.vinguiden.util.WineTypes;
import android.content.Intent;
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
		super.onPause();
	}

	public void searchWine(View view) {    	
		search(searchStr.getText().toString());
    }
    
    private void search(String no) {
    	if(isValidNo(no)) {
    		new DownloadBeverageTask(this, ModifyWineActivity.class, new WineTypes()).execute(no);
    	}
    }
	
	public void addWineManually(View view) {    	
		Intent intent = new Intent(AddWineActivity.this.getApplicationContext(), ModifyWineActivity.class);
    	startActivityForResult(intent, 0);
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
		return helper.getBeverageIdFromNo(Integer.valueOf(no)) != -1;
	}

}
