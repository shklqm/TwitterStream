package com.bledi.android.TwitterSearch;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import twitter4j.auth.RequestToken;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.bledi.android.Utilities.AlertDialogManager;
import com.bledi.android.Utilities.ConstantValues;
import com.bledi.android.Utilities.TwitterUtil;
import com.bledi.android.twittertest.R;

public class TwitterActivity extends Activity 
{
	private static final String LOG_TAG = null;
	private Button mSignin;
	private AlertDialogManager alert;
	private ActionBar bar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		
		 bar = getActionBar();
	     bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3658A3")));
		
		mSignin = (Button)findViewById(R.id.login_id);
		mSignin.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				checkInternet();
			}
		});
	}  
	
	private void checkInternet(){
            new CheckInternetConnection().execute();
    }
	
	class CheckInternetConnection extends AsyncTask<Void, Void, Boolean>
	{
		@Override
        protected void onPostExecute(Boolean result) 
        {
			if(result)
			{
	        	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		        if (!sharedPreferences.getBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN,false))
		            new TwitterAuthenticateTask().execute(); //If not logged in
		        else
		        {
		            Intent intent = new Intent(TwitterActivity.this, SearchTwitter.class);
		            startActivity(intent);
		        }
			}	
			else
			{
				alert = new AlertDialogManager();
				alert.showAlertDialog(TwitterActivity.this, "Network Connection Problem",
					"Unable to connect to the server", false);
			}
			
        }
		
		@Override
		protected Boolean doInBackground(Void... params) {
			 if (isNetworkAvailable()) {
			        try {
			        	
			        	HttpsURLConnection urls = (HttpsURLConnection) (new URL("https://mobile.twitter.com/").openConnection());
			        	HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
			        	SSLContext context = SSLContext.getInstance("TLS");
			        	context.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
			        	HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			        	
			            return (urls.getResponseCode() == 200);
			        } catch (IOException e) {
			            Log.e(LOG_TAG, "Error checking internet connection", e);
			        } catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (KeyManagementException e) { 
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    } else {
			        Log.d(LOG_TAG, "No network available!");
			    }
			    return false;
		}
	}
	
	public class NullX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            System.out.println();
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            System.out.println();
        }
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
	
	public class NullHostNameVerifier implements HostnameVerifier {

	    public boolean verify(String hostname, SSLSession session) {
	        Log.i("RestUtilImpl", "Approving certificate for " + hostname);
	        return true;
	    }
	}
	
	public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
    class TwitterAuthenticateTask extends AsyncTask<String, String, RequestToken> {
        @Override
        protected void onPostExecute(RequestToken requestToken){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
                startActivity(intent);
        }

        @Override
        protected RequestToken doInBackground(String... params) {
            return TwitterUtil.getInstance().getRequestToken();
        }
    }

}
