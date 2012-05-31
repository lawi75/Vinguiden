package ws.wiklund.vinguiden.util;

import ws.wiklund.vinguiden.R;
import ws.wiklund.vinguiden.model.Cellar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras=intent.getExtras();
		String title=extras.getString("title");
		String note=extras.getString("note");
		Cellar cellar = (Cellar) extras.getSerializable("cellar");
		int id = extras.getInt("nextNotificationId");

		NotificationManager manger = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon, "Combi Note", SystemClock.elapsedRealtime());
		PendingIntent contentIntent = PendingIntent.getActivity(context, id, new Intent(context, AlarmReceiver.class), 0);
	
		notification.setLatestEventInfo(context, note, title, contentIntent);
		notification.flags = Notification.FLAG_INSISTENT;
		notification.defaults |= Notification.DEFAULT_SOUND;

		cellar.setNotificationId(id);
		// The PendingIntent to launch our activity if the user selects this notification
		manger.notify(id, notification);
	}

}
