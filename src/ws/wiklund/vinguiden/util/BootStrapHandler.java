package ws.wiklund.vinguiden.util;

import ws.wiklund.guides.util.BasicBootStrapHandler;
import ws.wiklund.vinguiden.R;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BootStrapHandler {
	public static void init(Context context, String preferenceKey) {
		BasicBootStrapHandler.init(context, preferenceKey);
		
		/*if (runShowPromotionOnce(context)) {
			showPromotion(context);
		}*/
	}

	private static boolean runShowPromotionOnce(Context context) {
		return BasicBootStrapHandler.runOnce(context, "show_promo");
	}
	
	private static void showPromotion(final Context context) {
        final Dialog dialog = new Dialog(context, R.style.CustomDialog);
        
        dialog.setTitle(context.getString(R.string.promo_title));

        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        
        TextView tv = new TextView(context);
        tv.setTextColor(context.getResources().getColor(android.R.color.white));
        tv.setText(context.getString(R.string.promo_msg));
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);
        
        Button b1 = new Button(context);
        b1.setText(context.getString(R.string.promo_get));
        b1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ws.wiklund.vinguiden_pro"));
            	marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            	context.startActivity(marketIntent);
                dialog.dismiss();
            }
        });        
        ll.addView(b1);

        Button b3 = new Button(context);
        b3.setText(context.getString(R.string.nothanx));
        b3.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);        
        dialog.show();        		
	}

}
