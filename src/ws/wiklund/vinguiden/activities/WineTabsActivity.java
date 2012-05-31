package ws.wiklund.vinguiden.activities;

import ws.wiklund.vinguiden.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

public class WineTabsActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.tabs);
    }
    
}
