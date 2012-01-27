package ws.wiklund.vinguiden.util;

import android.content.Context;
import android.os.AsyncTask;

import com.paypal.android.MEP.PayPal;

public class PayPalFactory {
	private static PayPal payPal;

	public static synchronized void init(Context context) {
        new InitPayPalTask().execute(context);
	}
	
	public static PayPal getPayPal() {
		return payPal;
	}
	

	private static class InitPayPalTask extends AsyncTask<Context, Void, Void> {
		@Override
		protected Void doInBackground(Context... params) {
	        payPal = PayPal.initWithAppID(params[0], "APP-6D2315055F7988243", PayPal.ENV_LIVE);
	        //payPal = PayPal.initWithAppID(params[0], "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
	        
			return null;
		}

	}

}
