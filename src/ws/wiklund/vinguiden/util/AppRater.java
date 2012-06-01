package ws.wiklund.vinguiden.util;

import ws.wiklund.vinguiden.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppRater {
    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 7;
    
    public static void app_launched(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("apprater", 0);
        if (!prefs.getBoolean("dontshowagain", false)) {
            SharedPreferences.Editor editor = prefs.edit();
            
            // Increment launch counter
            long launch_count = prefs.getLong("launch_count", 0) + 1;
            editor.putLong("launch_count", launch_count);

            // Get date of first launch
            Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
            if (date_firstLaunch == 0) {
                date_firstLaunch = System.currentTimeMillis();
                editor.putLong("date_firstlaunch", date_firstLaunch);
            }
            
            // Wait at least n days before opening
            if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
                if (System.currentTimeMillis() >= date_firstLaunch + 
                        (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                    showRateDialog(context, editor);
                }
            }
            
            editor.commit();
        }
    }   
    
    public static void showRateDialog(final Context context, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(context);
        
        dialog.setTitle(context.getString(R.string.rating) + " " + context.getString(R.string.app_name));

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        TextView tv = new TextView(context);
        tv.setText(String.format(context.getString(R.string.rating_message), context.getString(R.string.app_name)));
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);
        
        Button b1 = new Button(context);
        b1.setText(context.getString(R.string.rating) + " " + context.getString(R.string.app_name));
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ws.wiklund.vinguiden"));
            	marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            	context.startActivity(marketIntent);
                dialog.dismiss();
            }
        });        
        ll.addView(b1);

        Button b2 = new Button(context);
        b2.setText(context.getString(R.string.later));
        b2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(context);
        b3.setText(context.getString(R.string.nothanx));
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);        
        dialog.show();        
    }
}