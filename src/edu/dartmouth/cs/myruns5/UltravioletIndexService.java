package edu.dartmouth.cs.myruns5;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class UltravioletIndexService extends Service {


  //Geocoder gc = new Geocoder(this);
  private static float HOURLY_UVI_FORECAST[]=new float[13];//UV Hourly Forecast from 6am-6pm
  public static final String CURRENT_UV_INDEX = "CURRENT_UVI";
  public static final String HOURLY_UV_INDEX = "HOURLY_UVI";
  public static final String UVI_RECOMMENDATION = "UVI_RECOMMENDATION";
  
  public static enum DAY_OF_WEEK{SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
      @Override public String toString() {
       //return Upper Case of Day of Week
       String s = super.toString();
       return s.toUpperCase(Locale.getDefault());
     }
  };
  private boolean hasData = false;

  private LocationManager myTracksLocationManager;
  
  // Timer to periodically invoke updateUVITask
  private final Timer timer = new Timer();
  private TimerTask updateUVITask = new TimerTask() {
    @Override
    public void run() 
    {
  
        //Do task of grabbing info here
        Criteria criteria = new Criteria(); 
        criteria.setAccuracy( Criteria.ACCURACY_COARSE); 
        criteria.setPowerRequirement( Criteria.POWER_LOW); 
        //criteria.setAltitudeRequired( false); 
        //criteria.setBearingRequired( false); 
        //riteria.setSpeedRequired( false); 
        //criteria.setCostAllowed( true);
        
        //criteria.setHorizontalAccuracy( Criteria.ACCURACY_MEDIUM); 
        //criteria.setVerticalAccuracy( Criteria.ACCURACY_MEDIUM); 
        //criteria.setBearingAccuracy( Criteria.ACCURACY_LOW); 
        //criteria.setSpeedAccuracy( Criteria.ACCURACY_LOW);
  
        String provider = myTracksLocationManager.getBestProvider(criteria, true);
        Location location = myTracksLocationManager.getLastKnownLocation(provider);
       
        
        //updateHourlyUVI(location);
    }
  };

  private void updateHourlyUVI(Location location){

    if(Geocoder.isPresent())
    {
        //Get the time and day of week
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        
        String responseString=null;
        try 
        {
       	
        	Geocoder gc = new Geocoder(this);
            List<Address> list = gc.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            Address address = list.get(0);
  
            //curl --referer http://www.uvawareness.com/uv-index/uv-index.php?location=ucla 
            
            //URL to the main website
            String website = "http://www.uvawareness.net/s/index.php",
                referrerWebsite = "http://www.uvawareness.com/uv-index/uv-index.php";
            Uri.Builder uri = Uri.parse(referrerWebsite).buildUpon();
            uri.appendQueryParameter("location", address.getPostalCode());
            
            Log.e(";jjj",uri.toString());
                            
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httppost = new HttpGet(website);
            httppost.addHeader("Referer",uri.build().toString());// "http://www.uvawareness.com/uv-index/uv-index.php?location=3450%20sawtelle%20blvd");
            
         // Execute HTTP Post Request
            org.apache.http.HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            InputStreamReader isr = new InputStreamReader(instream);
            BufferedReader rd = new BufferedReader(isr);
            StringBuffer buffer = new StringBuffer();

            try 
            {
                String line = "";
                while ((line = rd.readLine()) != null) {
                    buffer.append(line);
                }
                responseString = buffer.toString();
                isr.close();
            } finally {
                instream.close();
            }
        } catch (ClientProtocolException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        Document doc = Jsoup.parse(responseString);
        Elements content = doc.getElementsByClass("fcDayCon");
        
        for (Element fcDayCon : content) {
            System.out.println(fcDayCon.toString());
            //Found the UVI data for the proper day
            
            Element dayCon = fcDayCon.getElementsByClass("fcDate").first();
            System.out.println(dayCon.toString()+ " | "+ dayCon.html());
            
            String day = dayCon.html().toUpperCase();
            if(day.equals(DAY_OF_WEEK.values()[dayOfWeek-1].name()))
            {
                int it = 0;
                Elements contents = fcDayCon.getElementsByClass("uval");
                //Iteration of UVal values go from 6am,7,8,9,10,11,12pm,1,2,3,4,5,6pm
                for(Element uval : contents){
                    String val=uval.html();
                    System.out.println(val);
                      
                    //val will return an empty string if no value present
                    if(val.equals(""))
                        HOURLY_UVI_FORECAST[it]=1;
                    else
                        HOURLY_UVI_FORECAST[it]=Float.parseFloat(val);
                    it++;
                }
                break;
            }
        }
        hasData = true;
    }
  }

  final int updateInterval = 60;
  @Override
  public void onCreate() {
	myTracksLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	Calendar now = Calendar.getInstance();
    int minute = now.get(Calendar.MINUTE);//24 hr format
    long firstExecutionDelay = (updateInterval - minute) * Globals.ONE_MINUTE;
	timer.scheduleAtFixedRate(updateUVITask, 0, firstExecutionDelay);
    //super.onCreate();
  }
  
  
  private static float getCurrentUVI(){
      float currUVI = 0;
      Calendar now = Calendar.getInstance();
      int hour = now.get(Calendar.HOUR_OF_DAY);//24 hr format
      int nextHour = hour+1;
      
      if(hour < 6)//Hr < 6am
          return currUVI;
      else if(hour > 18)//Hr > 6pm
          return currUVI;
      else {

        long currTime = now.getTimeInMillis();
        Calendar prevHr = Calendar.getInstance();
        prevHr.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), hour, 0, 0);
        
        Calendar currHr = Calendar.getInstance();
        currHr.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),now.get(Calendar.DATE), hour, 0);
     
        Calendar nextHr = Calendar.getInstance();
        nextHr.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),now.get(Calendar.DATE), nextHour, 0);
       
        float dtime = (currTime - prevHr.getTimeInMillis())/(nextHr.getTimeInMillis() - prevHr.getTimeInMillis());
        
      //y=mx+b assuming linear scale time increase
        hour -= 6;nextHour-=6;
        //simple correction for the last index
        if(nextHour == HOURLY_UVI_FORECAST.length)
          nextHour=hour;
        
        if(HOURLY_UVI_FORECAST[nextHour] > HOURLY_UVI_FORECAST[hour])
          return dtime * (HOURLY_UVI_FORECAST[nextHour] - HOURLY_UVI_FORECAST[hour]) + HOURLY_UVI_FORECAST[hour];
        else
          return HOURLY_UVI_FORECAST[hour] - dtime * (HOURLY_UVI_FORECAST[hour] - HOURLY_UVI_FORECAST[nextHour]);
      }
  }
  
  
  @Override
  public int onStartCommand(Intent intent, int flags, int startId){
	  String option = intent.getAction();

	  Toast.makeText(getApplicationContext(), "Option #" + option ,Toast.LENGTH_SHORT).show();
	  if(hasData){
		  if(option.equals(CURRENT_UV_INDEX)){
			  float uvi = UltravioletIndexService.getCurrentUVI();
	
	
			  Toast.makeText(getApplicationContext(), "Inside here #" ,Toast.LENGTH_SHORT).show();
	          Intent i = new Intent(CURRENT_UV_INDEX).putExtra(CURRENT_UV_INDEX,uvi);
	          sendBroadcast(i);
	          
	      }else if(option.equals(HOURLY_UV_INDEX)){
	    	  Intent i = new Intent(HOURLY_UV_INDEX);            
	    	  i.putExtra(CURRENT_UV_INDEX,HOURLY_UVI_FORECAST);
	    	  sendBroadcast(i);
	      }else if (option.equals(UVI_RECOMMENDATION)){
	        
	      }
		}
      return START_STICKY;
  }
  

  /*
   * Handler to handle messages coming in from the android application.
   * The android app binds to the serviec
   */
  class IncomingHandler extends Handler {
      @Override
      public void handleMessage(Message msg) {
          
          Bundle data = msg.getData();
          String option = data.getString("option");
          
          if(option.equals(CURRENT_UV_INDEX)){
              float uvi = UltravioletIndexService.getCurrentUVI();
              Intent i = new Intent(CURRENT_UV_INDEX);            
              i.putExtra(CURRENT_UV_INDEX,uvi);
              sendBroadcast(i);
              
          }else if(option.equals(HOURLY_UV_INDEX)){
            Intent i = new Intent(HOURLY_UV_INDEX);            
            i.putExtra(CURRENT_UV_INDEX,HOURLY_UVI_FORECAST);
            sendBroadcast(i);
          }else if (option.equals(UVI_RECOMMENDATION)){
            
          }
      }
   }
  
  //References an instance of the Messenger object that passes messages
  final Messenger myMessenger = new Messenger(new IncomingHandler());
  
  //When an app binds to the service it gets a reference to the messenger to bind to the service
  
    @Override
    public IBinder onBind(Intent arg0) {
      return myMessenger.getBinder();
    }
  }