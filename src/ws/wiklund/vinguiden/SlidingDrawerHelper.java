package ws.wiklund.vinguiden;

import ws.wiklund.vinguiden.activities.WineListActivity;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class SlidingDrawerHelper {
    public void openWineList(Activity activity, View view) {
    	Intent intent = new Intent(view.getContext(), WineListActivity.class);
    	activity.startActivityForResult(intent, 0);
    }
    
    public void searchWine(Activity activity, View view) {
        // Kabloey
    }
}
