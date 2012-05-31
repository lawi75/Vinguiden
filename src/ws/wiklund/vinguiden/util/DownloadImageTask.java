package ws.wiklund.vinguiden.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
	private ImageView imageView;
	private String baseUrl;
	private int width;
	private int height;

	public DownloadImageTask(ImageView imageView, String baseUrl, int width, int height) {
		this.imageView = imageView;
		this.baseUrl = baseUrl;
		this.width = width;
		this.height = height;
	}
	
	@Override
	protected Bitmap doInBackground(String... url) {
		if(url != null && url.length > 0){
			Bitmap bitmap;
			
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(baseUrl + url[0]).getContent());
				if (bitmap != null) {
					return Bitmap.createScaledBitmap(bitmap, width, height, true);
				}
			} catch (IOException e) {
				Log.w(DownloadImageTask.class.getName(), "Failed to load image from " + url[0], e);
			}
		}

		return null;
	}

	@Override
	protected void onPostExecute(Bitmap b) {
		if (b != null) {
			imageView.setImageBitmap(b);
		} else {
			Log.d(DownloadImageTask.class.getName(), "No thumb found, using default");
		}
		
		super.onPostExecute(b);
	}
}
