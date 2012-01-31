package ws.wiklund.vinguiden.util.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.util.Log;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Skeleton base class for RequestListeners, providing default error 
 * handling. Applications should handle these error conditions.
 *
 */
public abstract class BaseRequestListener implements RequestListener {

    public void onFacebookError(FacebookError e, final Object state) {
        Log.e(BaseRequestListener.class.getName(), "onFacebookError", e);
    }

    public void onFileNotFoundException(FileNotFoundException e,
                                        final Object state) {
        Log.e(BaseRequestListener.class.getName(), "onFileNotFoundException", e);
    }

    public void onIOException(IOException e, final Object state) {
        Log.e(BaseRequestListener.class.getName(), "onIOException", e);
    }

    public void onMalformedURLException(MalformedURLException e,
                                        final Object state) {
        Log.e(BaseRequestListener.class.getName(), "onMalformedURLException", e);
    }
    
}