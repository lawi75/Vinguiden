package ws.wiklund.vinguiden.activities;

import ws.wiklund.guides.activities.BaseActivity;
import ws.wiklund.vinguiden.R;
import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;

public class DonateActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.donate);
		
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.donateBig);
        
		CheckoutButton btn = getCheckoutButton(PayPal.BUTTON_294x45);
		if (btn != null) {
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			btn.setLayoutParams(params);
			layout.addView(btn, 0);
		}
	}

}
