package com.bledi.android.TwitterSearch;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.StatusDeletionNotice;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import com.bledi.android.ListAdapter.ExampleAdapter;
import com.bledi.android.ListAdapter.ExampleViewModel;
import com.bledi.android.Utilities.AlertDialogManager;
import com.bledi.android.Utilities.ConstantValues;
import com.bledi.android.Utilities.TwitterUtil;
import com.bledi.android.twittertest.R;

public class SearchTwitter extends Activity {
	private static final int QUEUESIZE = 1000;
	private int MySpeed = 2500;
    private String myString="";
    private ListView listView;
    private List<ExampleViewModel> viewModels;
    private ExampleViewModel myRow;
    private ExampleAdapter adapter;
    public static ConfigurationBuilder confBuilder;
	public static Configuration config ;
	private Thread myThread = null;
	private TwitterStream twitterstream = null;
	private final Context context = this;
	private SearchView searchView = null;
	private ActionBar bar;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        
        bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3658A3")));
        
        findViewById(R.id.loadingPanel).setVisibility(View.GONE); //Remove ProgressBar
        initControl();
    }
    
    @Override
	public void onBackPressed() {
	    moveTaskToBack(true);
	}
    
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_actions, menu);
 
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Take appropriate action for each action item click
		switch (item.getItemId()) 
		{
			case R.id.action_search:
				return true;
			case R.id.speedup_tag:	
				speedUpStreaming();
				return true;
			case R.id.slowdown_tag:
				slowDownStreaming();
				return true;
			case R.id.mykeyword_tag:
				showMyKeyWordDialog();
				return true;
			case R.id.stream_tag:
				startStreaming();
				return true;
			case R.id.questions_tag:
				openFAQDialog();
				return true;
			case R.id.about_tag:
				openAboutDialog();
				return true;
			case R.id.log_out_from_twitter:
				logOut();
				return true;	
			default:
				return super.onOptionsItemSelected(item);
		}	
	}
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	myString = intent.getStringExtra(SearchManager.QUERY);
            searchView.setQuery("", false);
            searchView.clearFocus();
            
            new CheckInternetConnection().execute();
        }
    }
    
    private void speedUpStreaming()
    {
    	if(MySpeed != 1000 && myThread != null)
    	{	
    		MySpeed = MySpeed - 500;
    		float myFloat = (float) MySpeed / 1000;
    		Toast.makeText(getApplicationContext(), "Speed changed to: " + myFloat + " seconds", Toast.LENGTH_SHORT).show();
    	}
    }
    
    private void slowDownStreaming()
    {
    	if(MySpeed != 5000 && myThread != null)
    	{
    		MySpeed = MySpeed + 500;
    		float myFloat = (float) MySpeed / 1000;
    		Toast.makeText(getApplicationContext(), "Speed changed to: " + myFloat + " seconds", Toast.LENGTH_SHORT).show();
    	}	
    }
    
    private void showMyKeyWordDialog()
    {
    	AlertDialogManager alert = new AlertDialogManager();
    	
    	if(isNullOrWhitespace(myString))
    		alert.showAlertDialog(this, "MyKeyWord", "You have not entered any keyword yet", false);
    	else
    		alert.showAlertDialog(this, "MyKeyWord", myString, false);
    }
    
    private void openFAQDialog()
    {
    	// custom dialog
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.faq);
		dialog.setTitle("FAQ");
		
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOKfaq);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
    }
    
    private void openAboutDialog()
    {
    	// custom dialog
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.about);
		dialog.setTitle("About");
		
		Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOKabout);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
    }
    
	class CheckInternetConnection extends AsyncTask<Void, Void, Boolean>{
		@Override
        protected void onPostExecute(Boolean result){
			if(result)
				searchTweets(myString);		// If Internet connection is present, go ahead
			else{
				AlertDialogManager alert = new AlertDialogManager();
				alert.showAlertDialog(SearchTwitter.this, "Network Connection Problem",
					"Unable to connect to the server", false);
			}
        }
		
		@Override
		protected Boolean doInBackground(Void... params) {
			 if (isNetworkAvailable()) {
			        try {
			        	HttpsURLConnection urls = (HttpsURLConnection) (new URL("https://mobile.twitter.com/").openConnection());	//test from twitter mobile
			        	HttpsURLConnection.setDefaultHostnameVerifier(new NullHostNameVerifier());
			        	SSLContext context = SSLContext.getInstance("TLS");
			        	context.init(null, new X509TrustManager[]{new NullX509TrustManager()}, new SecureRandom());
			        	HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
			            return (urls.getResponseCode() == 200);
			        } catch (IOException e) {
			            Log.e("Internet error", "Error checking internet connection", e);
			        } catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (KeyManagementException e) { 
						e.printStackTrace();
					}
			    } else {
			        Log.d("Internet", "No network available!");
			    }
			    return false;
		}
	}
    
    private void searchTweets(String query)
    {
    	if(twitterstream != null)
    	{
    		twitterstream.cleanUp();
    		twitterstream.shutdown();
    		twitterstream = null;
    	}
    	if(myThread != null)
    	{	
    		myThread.interrupt();
    		myThread = null;
    	}
    	
    	findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE); //Add ProgressBar
    	
    	new SearchAndDisplay().execute(query);
    } 
	
    private void startStreaming()
    {
    	if(isNullOrWhitespace(myString))	// Handle the case where Stream button is pressed before searching
    		return;
    	
    	if(myThread == null)
    		startStream2();					//Stream in case myThread has not initially been started
    	else
    		Log.d("Starting", "Thread not started");
    }
    
    private class SearchAndDisplay extends AsyncTask<String, Void, Void> {
    	Elements all;
    	
        @Override
        protected Void doInBackground(String... params) {
            try { 
            	String  xQuery = null;
            	if(params[0].charAt(0) == '#')
            	{
            		xQuery = params[0].substring(0,0) + params[0].substring(1);
            		xQuery = "%23" + xQuery;		//replace # with %23 code
            	}	
            	else
            		xQuery = params[0];	//Safeguard
            		
                Document document = Jsoup.connect("https://mobile.twitter.com/search?q=" + xQuery)
                		.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/31.0.1650.63 Chrome/31.0.1650.63 Safari/537.36")
                		.get();
                
                 all = document.select(".tweet");
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
 
        @Override
        protected void onPostExecute(Void result) 
        {
        	listView = ( ListView ) findViewById(R.id.listview);
        	viewModels = new ArrayList<ExampleViewModel>();
        	
        	for(Element el : all)									//extract top tweets
    		{
    			String imageUrl = el.select("img").attr("src");
    			String userInfo = el.select(".fullname").text();
    			String userName = el.select(".username").text();
    			String tweetText = el.select(".tweet-text").text();
    			 
    			myRow = new ExampleViewModel(userInfo + " " + userName + "\n" +  tweetText, imageUrl);
    			viewModels.add(myRow);
    		}
        	 
            adapter = new ExampleAdapter(getApplicationContext(), viewModels);
            findViewById(R.id.loadingPanel).setVisibility(View.GONE);	//Remove ProgressBar
            listView.setAdapter(adapter);
        }
    }
    
    public void startStream2()
    {
        	final Queue<ExampleViewModel> viewModelsNew = new LinkedList<ExampleViewModel>();
        	
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String accessTokenString = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
            String accessTokenSecret = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
            if (!isNullOrWhitespace(accessTokenString) && !isNullOrWhitespace(accessTokenSecret)) 
            { 
                
                confBuilder = new twitter4j.conf.ConfigurationBuilder();
                confBuilder.setDebugEnabled(true);
                confBuilder.setOAuthConsumerKey(ConstantValues.TWITTER_CONSUMER_KEY);
                confBuilder.setOAuthConsumerSecret(ConstantValues.TWITTER_CONSUMER_SECRET);
                confBuilder.setOAuthAccessToken(accessTokenString);
                confBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
                config = confBuilder.build();
            	 
            	twitterstream = new TwitterStreamFactory(config).getInstance();
            	
           
            	twitter4j.StatusListener listener = new twitter4j.StatusListener() {
            		@Override
                    public void onStatus(twitter4j.Status status) { 
            			
            			if(viewModelsNew.size() < QUEUESIZE) 	//queue only 1000 tweets
                    	{
                    		String imageUrl = status.getUser().getProfileImageURL().toString();
                			String userInfo = status.getUser().getName();
                			String userName = "@" + status.getUser().getScreenName();
                			String tweetText = status.getText();
                    		
                			myRow = new ExampleViewModel(userInfo + " " + userName + "\n" +  tweetText, imageUrl);
                			viewModelsNew.add(myRow); 
                    	}
                    }

                    @Override
                    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }

                    @Override
                    public void onTrackLimitationNotice(int numberOfLimitedStatuses) { }

                    @Override
                    public void onScrubGeo(long userId, long upToStatusId) { }

                    @Override
                    public void onStallWarning(StallWarning warning) { }

                    @Override
                    public void onException(Exception ex) {
                        ex.printStackTrace();
                    }
                };
            	
                FilterQuery filterQuery = new FilterQuery();
                
                String[] query = {myString};
                filterQuery.track(query);
                
                twitterstream.addListener(listener);
                twitterstream.filter(filterQuery); 
                
            }
           
             myThread =  new Thread() {
            	int i; 
		        public void run() {
		            while (true) {
		                try {
		                    runOnUiThread(new Runnable() {
		                        @Override
		                        public void run() 
		                        {
		                        	if(viewModels.size() == 0 && viewModelsNew.size() != 0)	//when there are no tweets displayed
		                        	{
			                        	viewModels.add(viewModelsNew.peek());				//add new item in the beginning
			                        	viewModelsNew.remove();								//remove new added item from the queue
			                        	adapter.notifyDataSetChanged();						//set new GUI
		                        	}
		                        	else if(viewModels.size() != 0 && viewModels.size() < 21 && viewModelsNew.size() != 0) //don't remove last item
		                        	{	
			                        	viewModels.add(0, viewModelsNew.peek());			//add new item in the beginning
			                        	viewModelsNew.remove();								//remove new added item from the queue
			                        	adapter.notifyDataSetChanged();						//set new GUI
		                        	}
		                        	else if(viewModels.size() != 0 && viewModelsNew.size() != 0)
		                        	{	
			                        	adapter.viewmodels().remove(viewModels.size() - 1);	//remove last item
			                        	viewModels.add(0, viewModelsNew.peek());			//add new item in the beginning
			                        	viewModelsNew.remove();								//remove new added item from the queue
			                        	adapter.notifyDataSetChanged();						//set new GUI
		                        	}
		                        	
		                        	i++;
		                        }
		                    });
		                    Thread.sleep(MySpeed);
		                } catch (InterruptedException e) {
		                    e.printStackTrace();
		                    viewModelsNew.clear(); //TODO ?
		                    return;
		                }
		                
		                if(i%7 == 0)			//clear cache
		                {
		                	try { 
				       	         trimCache(getApplicationContext());
				       	      } catch (Exception e) {
				       	         e.printStackTrace();
				       	      }
		                }
		            }
		        }
		    };
		     myThread.start();
    } 	 

    public static void trimCache(Context context) {
      try {
         File dir = context.getCacheDir();
         if (dir != null && dir.isDirectory()) {
            deleteDir(dir);
         }
      } catch (Exception e) {
    	  e.printStackTrace();
      }
   }

    public static boolean deleteDir(File dir) {
		  if (dir != null && dir.isDirectory()) {
			 String[] children = dir.list();
			 for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
				   return false;
				}
			 }
		  }

	      return dir.delete();
	   }
  
    private void initControl() 
    {
        Uri uri = getIntent().getData();
        if (uri != null && uri.toString().startsWith(ConstantValues.TWITTER_CALLBACK_URL)) {
            String verifier = uri.getQueryParameter(ConstantValues.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
            new TwitterGetAccessTokenTask().execute(verifier);
        } else
            new TwitterGetAccessTokenTask().execute("");
    }

    private void logOut() 
    {
		//Stop the stream and the running thread

    	if(twitterstream != null)
    	{
    		twitterstream.cleanUp();
    		twitterstream.shutdown();
    		twitterstream = null;
    	}
    	if(myThread != null)
    	{	
    		myThread.interrupt();
    		myThread = null;
    	}
    	
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
        editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
        editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, false);
        editor.commit();
        TwitterUtil.getInstance().reset();
        
        Intent intent = new Intent(SearchTwitter.this, TwitterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    class TwitterGetAccessTokenTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String userName) {
        }

        @Override
        protected String doInBackground(String... params) {

            Twitter twitter = TwitterUtil.getInstance().getTwitter();
            RequestToken requestToken = TwitterUtil.getInstance().getRequestToken();
            if (!isNullOrWhitespace(params[0])) {
                try {
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, params[0]);
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, accessToken.getToken());
                    editor.putString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, accessToken.getTokenSecret());
                    editor.putBoolean(ConstantValues.PREFERENCE_TWITTER_IS_LOGGED_IN, true);
                    editor.commit();
                    return twitter.showUser(accessToken.getUserId()).getName();
                } catch (TwitterException e) {
                    e.printStackTrace();  
                }
            } 
            else 
            {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String accessTokenString = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN, "");
                String accessTokenSecret = sharedPreferences.getString(ConstantValues.PREFERENCE_TWITTER_OAUTH_TOKEN_SECRET, "");
                AccessToken accessToken = new AccessToken(accessTokenString, accessTokenSecret);
                try 
                {
                    TwitterUtil.getInstance().setTwitterFactory(accessToken);
                    return TwitterUtil.getInstance().getTwitter().showUser(accessToken.getUserId()).getName();
                } catch (TwitterException e) {
                    e.printStackTrace();  
                }
            }

            return null;  
        }
    }

    public boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
    
    public static boolean isNullOrWhitespace(String s) {
    	if (s == null)
    		return true;
    	for (int i = 0; i < s.length(); i++) {
    		if (!Character.isWhitespace(s.charAt(i))) {
    			return false;
    		}
    	}
    	return true;
    }
}