package edu.dartmouth.cs.myruns5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;

import weka.core.Attribute;
import weka.core.Instance;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import edu.dartmouth.cs.myruns5.util.LocationUtils;


import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class TrackingService extends Service
	implements LocationListener, SensorEventListener
	{
	private File mWekaClassificationFile;
	private Attribute mClassNameForData,meanAttribute,stdAttribute,maxAttribute,minAttribute,meanAbsDeviationAttribute;
	
	public ArrayList<Location> mLocationList;
	
	private int[] mInferenceCount = {0, 0, 0};

	// Location manager
	private LocationManager mLocationManager;
	
	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mMotionUpdateBroadcast;
	
	private int mInputType;
	public int mInferredActivityType;
	
	private FileOutputStream trackFile;
	
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mLightSensor;
	private Sensor mTempSensor;
	
	private float[] mGeomagnetic;
	private static ArrayBlockingQueue<Double> mAccBuffer;
	private static ArrayBlockingQueue<LumenDataPoint> mLightIntensityReadingBuffer;
	// Buffer for temperature readings
	private static ArrayBlockingQueue<Double> mTempBuffer;
	
	private AccelerometerActivityClassificationTask mAccelerometerActivityClassificationTask;
	private LightSensorActivityClassificationTask mLightSensorActivityClassificationTask;
	private TemperatureSensorActivityClassificationTask mTempSensorActivityClassificationTask;
	
	private final IBinder mBinder = new TrackingBinder();

	private float[] mGravity;

	private long pitchReading;

	
	public static final String LOCATION_UPDATE = "location update";
	public static final int NEW_LOCATION_AVAILABLE = 400;
	
	// broadcast 
	public static final String ACTION_MOTION_UPDATE = "motion update";
	public static final String CURRENT_MOTION_TYPE = "new motion type";
	public static final String VOTED_MOTION_TYPE = "voted motion type";
	public static final String ACTION_TRACKING = "tracking action";

	private static final String TAG = "TrackingService";
	
	// Variables added by lohith.
	private double totalAcceleration;

	private double avgAcceleration;
	private double timeDuration;
	private double initialVelocity;
	private String temperature;
	private String humidity;
	
	private static Timer dataCollector;
  	private TimerTask dataCollectorTask = new TimerTask() {
  		@Override
  		public void run() 
  		{
  			dataCollectionEnabled=true;
  		}
  	};
	private static boolean dataCollectionEnabled = true;
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		mLocationList = new ArrayList<Location>();
		mLocationUpdateBroadcast = new Intent();
		mLocationUpdateBroadcast.setAction(ACTION_TRACKING);
		mMotionUpdateBroadcast = new Intent();
		mMotionUpdateBroadcast.setAction(ACTION_MOTION_UPDATE);
		mLightIntensityReadingBuffer = new ArrayBlockingQueue<LumenDataPoint>(Globals.LIGHT_BUFFER_CAPACITY);
		mAccBuffer = new ArrayBlockingQueue<Double>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
		mTempBuffer = new ArrayBlockingQueue<Double>(Globals.TEMPERATURE_BUFFER_CAPCITY);
		mAccelerometerActivityClassificationTask = new AccelerometerActivityClassificationTask();
		mLightSensorActivityClassificationTask = new LightSensorActivityClassificationTask();
		mTempSensorActivityClassificationTask = new TemperatureSensorActivityClassificationTask();
		mInferredActivityType = Globals.ACTIVITY_TYPE_STANDING;
		
		//Start the timer for data collection
		dataCollector = new Timer();
		dataCollector.scheduleAtFixedRate(dataCollectorTask, Globals.DATA_COLLECTOR_START_DELAY, Globals.DATA_COLLECTOR_INTERVAL);
		
		//Added by lohith
		avgAcceleration = 0;
		timeDuration = 5000;
		initialVelocity = 0;
		// the weather variables have to be intialized by the API call only. we cant set them to 0.
		// Get the weather details - this should set the varial
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		File sdCard = Environment.getExternalStorageDirectory();  
		String tempfilename = sdCard.getAbsolutePath()  + "/temp";
		File file = new File(tempfilename);
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Create Weka features.arff file reference
		mWekaClassificationFile = new File(getExternalFilesDir(null), Globals.FEATURE_LIGHT_FILE_NAME);
		Log.d(Globals.TAG, mWekaClassificationFile.getAbsolutePath());
		
		// Read inputType, can be GPS or Automatic.
		mInputType = intent.getIntExtra(MapDisplayActivity.INPUT_TYPE, -1);

		//JERRID I uncommented this
		Toast.makeText(getApplicationContext(), String.valueOf(mInputType), Toast.LENGTH_SHORT).show();
				
		// Get LocationManager and set related provider.
	    mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	    boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	    
	    if (gpsEnabled)
	    	mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Globals.RECORDING_GPS_INTERVAL_DEFAULT, Globals.RECORDING_GPS_DISTANCE_DEFAULT, this);
	    else
	    	mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Globals.RECORDING_NETWORK_PROVIDER_INTERVAL_DEFAULT,Globals.RECORDING_NETWORK_PROVIDER_DISTANCE_DEFAULT, this);
	 
//    	Toast.makeText(getApplicationContext(), "mInputType: "+String.valueOf(mInputType), Toast.LENGTH_SHORT).show();

	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
	    	// init sensor manager
	    	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	    
	    	mAccelerometer = mSensorManager .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	    	// register listener
	    	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	    	mAccelerometerActivityClassificationTask.execute();
	    	
	    	//JERRID: Register light Sensor
			mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
			mSensorManager.registerListener(this, mLightSensor,SensorManager.SENSOR_DELAY_FASTEST);
			
			mTempSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
			mSensorManager.registerListener(this, mTempSensor, SensorManager.SENSOR_DELAY_FASTEST);
			
			mTempSensorActivityClassificationTask.execute();
	    }
	    
		// Using pending intent to bring back the MapActivity from notification center.
		// Use NotificationManager to build notification(icon, content, title, flag and pIntent)
		String notificationTitle = "MyRuns";
		String notificationText = "Tracking Location";
		Intent myIntent = new Intent(this, MapDisplayActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, myIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		Notification notification = new Notification.Builder(this)
	        .setContentTitle(notificationTitle)
	        .setContentText(notificationText).setSmallIcon(R.drawable.greend)
	        .setContentIntent(pendingIntent).getNotification();
		
		NotificationManager notificationManager =  (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
		//notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
				
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
//    	Toast.makeText(getApplicationContext(), "service onDestroy", Toast.LENGTH_SHORT).show();

		// Unregistering listeners
		mLocationManager.removeUpdates(this);
		// Remove notification
	    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    notificationManager.cancelAll();
	    
//    	Toast.makeText(getApplicationContext(), "mInputType: "+String.valueOf(mInputType), Toast.LENGTH_SHORT).show();

	    // unregister listener
	    if (mInputType == Globals.INPUT_TYPE_AUTOMATIC){
//	    	Toast.makeText(getApplicationContext(), "unregister linstener", Toast.LENGTH_SHORT).show();
	    	mSensorManager.unregisterListener(this);
	    }
	    
	    // cancel task
	    mAccelerometerActivityClassificationTask.cancel(true);
	    mTempSensorActivityClassificationTask.cancel(true);
	}
	
	public class TrackingBinder extends Binder{
		public TrackingService getService(){
			return TrackingService.this;
		}
	}

	
	/************ implement LocationLister interface ***********/
	public void onLocationChanged(Location location) {
//    	Toast.makeText(getApplication(), "OnLocationChanged", Toast.LENGTH_SHORT).show();

		//JERRID Adds--------------
		// Check whether location is valid, drop if invalid
	      if (!LocationUtils.isValidLocation(location)) {
	        Log.w(TAG, "Ignore onLocationChangedAsync. location is invalid.");
	        return;
	      }
		
	      //Check whether location reading is accurate
	      if (!location.hasAccuracy() || location.getAccuracy() >= Globals.RECORDING_GPS_ACCURACY_DEFAULT) {
	          Log.d(TAG, "Ignore onLocationChangedAsync. Poor accuracy.");
	          return;
	        }
	      
	      // Fix for phones that do not set the time field
	      if (location.getTime() == 0L) {
	        location.setTime(System.currentTimeMillis());
	      }
	      //------------------
	      
		// update location list
		mLocationList.add(location);

		// Send broadcast saying new location is updated
		mLocationUpdateBroadcast.putExtra(TrackingService.LOCATION_UPDATE, TrackingService.NEW_LOCATION_AVAILABLE);
		sendBroadcast(mLocationUpdateBroadcast);
	}
	
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	private void pauseDataCollection(){
		dataCollectionEnabled = false;
		mGravity = null;
		mGeomagnetic=null;
		//clear the buffer becasue we don't need it anymore
		mAccBuffer.clear();
		mLightIntensityReadingBuffer.clear();
	}
	
	/************ implement SensorEventLister interface ***********/
	public void onSensorChanged(SensorEvent event) {
//		Toast.makeText(getApplicationContext(), "onSensorChanged", Toast.LENGTH_SHORT).show();
		 // Many sensors return 3 values, one for each axis.
		if(event.sensor.getType() == Sensor.TYPE_TEMPERATURE) {
			// Get temperature readings.
			float[] temperatureArr = event.values;
			//  Check for ambient temperature and log the same.
			double temp = temperatureArr[0];
			mTempBuffer.add(temp);
		}

		if(trackFile == null) {
			File sdCard = Environment.getExternalStorageDirectory();  
			String resultPredictor = sdCard + "/Android/data/edu.dartmouth.cs.myruns5/files"  + "/keepTrack.txt";     
			File resultFile;
			try {
				resultFile = new File(resultPredictor);
				resultFile.createNewFile();
				trackFile = new FileOutputStream(resultFile);
				trackFile.write(("Current type is " +event.sensor.getType()).getBytes());
				trackFile.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			try {
				trackFile.write(("Current type is " +event.sensor.getType()).getBytes());
				trackFile.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	      if (event.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD){
	           mGeomagnetic = event.values;
	      }else if(event.sensor.getType() == android.hardware.Sensor.TYPE_LINEAR_ACCELERATION ){
	    	  
              mGravity = event.values;
              double x = mGravity[0];
              double y = mGravity[1];
              //Jerrid: Dont care about z component (for now)
              double z = mGravity[2];				
              double m = Math.sqrt(x*x + y*y + z*z);
	
              // Add m to the mAccBuffer one by one.
              try {
            	  mAccBuffer.add(m);
              } catch (IllegalStateException e) {
            	  ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(2*mAccBuffer.size());
            	  mAccBuffer.drainTo(newBuf);
            	  mAccBuffer = newBuf;
            	  mAccBuffer.add(m);				
              }
	      }else
	      if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
	    	  //JERRID: Light Sensor Reading
	          if (mGravity != null && mGeomagnetic != null)
	          {
	              float R[] = new float[9];
	              float I[] = new float[9];
	              boolean success = android.hardware.SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
	              if (success) 
	              {
	                  float orientation[] = new float[3];
	                  android.hardware.SensorManager.getOrientation(R, orientation);             
	                  pitchReading = Math.round(Math.abs((orientation[1]*180)/Math.PI));
	         
	                  float uvi = 0;
	                  LumenDataPoint intensityReading = new LumenDataPoint(event.timestamp, pitchReading, event.values[0], uvi);
	                    
	                  try {
	                      //JERRID: Add the magnitude reading to the buffer
	                      mLightIntensityReadingBuffer.add(intensityReading);
	                  } catch (IllegalStateException e) {
	                
	                      // Exception happens when reach the capacity.
	                      // Doubling the buffer. ListBlockingQueue has no such issue,
	                      // But generally has worse performance
	                      ArrayBlockingQueue<LumenDataPoint> newBuf = new ArrayBlockingQueue<LumenDataPoint>( mLightIntensityReadingBuffer.size() * 2);
	                      mLightIntensityReadingBuffer.drainTo(newBuf);
	                      mLightIntensityReadingBuffer = newBuf;
	                      mLightIntensityReadingBuffer.add(intensityReading);
	                  }
	              }
	          }
//			Toast.makeText(getApplicationContext(), String.valueOf(mAccBuffer.size()), Toast.LENGTH_SHORT).show();
		} else if(event.sensor.getType() == Sensor.TYPE_TEMPERATURE) {
			// Get temperature readings.
			float[] temperatureArr = event.values;
			//  Check for ambient temperature and log the same.
			double temp = temperatureArr[0];
			mTempBuffer.add(temp);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}


	/************ AsyncTask **************/
	private class LightSensorActivityClassificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

	          /*
	           * The training phase stores the min, max, mean, std, and mean absolute deviation of an intensity window
	           * for each of the 16 light intensity readings (m0..m14), and the label the user provided to the 
	           *  collector (see collector UI later). Collectively, we call these features the 
	           *  feature vector comprises: magnitudes (f0..f14), MAX magnitude, label....
	           */          
			Instance featureInstance = new Instance(Globals.FEAT_NUMBER_FEATURES - 1);
			int blockSize = 0;
			LumenDataPoint[] lightIntensityDataBlock = new LumenDataPoint[Globals.LIGHT_BLOCK_CAPACITY];
			double maxLightMagnitude = Double.MIN_VALUE,
					minLightMagnitude = Double.MAX_VALUE,
					meanLightIntensity = 0,
					varianceIntensity = 0,
					stdLightMagnitude = 0,
					meanAbsoluteDeveationLightIntensity = 0;

	          while (true) 
	          {
	        	  try 
	        	  {
	                  // need to check if the AsyncTask is cancelled or not in the while loop
	                  if (isCancelled () == true)
	                  {
	                      return null;
	                  }

	                  // JERRID: Pops the "head" element from the Blocking Queue one at a time
	                  LumenDataPoint dataPoint = mLightIntensityReadingBuffer.take();
	                  double intensityReading = dataPoint.getIntensity();
	                  lightIntensityDataBlock[blockSize++] = dataPoint;
	                  
	                  //Calculate Mean Intensity Value
	                  if(blockSize <= 1)
	                      meanLightIntensity = intensityReading;
	                  else
	                      meanLightIntensity = (intensityReading + meanLightIntensity*(blockSize-1))/blockSize;
	              
	                  
	                  //JERRID: Once 16 readings are found, identify the MIN, MAX, magnitude
	                  if (blockSize == Globals.LIGHT_BLOCK_CAPACITY) 
	                  {
	                      //Compute the Mean Absolute Deviation since we have a full buffer=
	                      for (LumenDataPoint dp : lightIntensityDataBlock) {
	                          //find mean absolute deviation
	                          double val = dp.getIntensity();
	                          double diff = val - meanLightIntensity;
	                          varianceIntensity += diff * diff;
	                          meanAbsoluteDeveationLightIntensity += Math.abs(diff);
	                          
	                          //Calculate the MIN/MAX (seen so far)
	                          if (maxLightMagnitude < intensityReading) {
	                              maxLightMagnitude = intensityReading;
	                          }
	                          
	                          //find the min intensity
	                          if (minLightMagnitude > intensityReading) {
	                              minLightMagnitude = intensityReading;
	                          }
	                      }
	                      
	                      varianceIntensity = varianceIntensity/Globals.LIGHT_BLOCK_CAPACITY;
	                      stdLightMagnitude = Math.sqrt(varianceIntensity);
	                      meanAbsoluteDeveationLightIntensity = meanAbsoluteDeveationLightIntensity / Globals.LIGHT_BLOCK_CAPACITY;
	                  
	                      featureInstance.setValue(minAttribute,minLightMagnitude);
	                      featureInstance.setValue(maxAttribute,maxLightMagnitude);
	                      featureInstance.setValue(meanAttribute,meanLightIntensity);
	                      featureInstance.setValue(stdAttribute,stdLightMagnitude);
	                      featureInstance.setValue(meanAbsDeviationAttribute,meanAbsoluteDeveationLightIntensity);
	                      
	                      //Classifier
	                      WekaWrapper wrapper = new WekaWrapper();
	                      double prediction = wrapper.classifyInstance(featureInstance);
	                      String classification = featureInstance.classAttribute().value((int) prediction);
	                      
	                      File dir = new File (android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/accelerometer");
	                      dir.mkdirs();
	                      FileWriter sunClassificationFile=null;
	                  
	                      try {
	                          sunClassificationFile = new FileWriter(dir.getAbsolutePath()+"/"+Globals.LIGHT_INTENSITY_FILE_NAME, true);                 
	      
	                          try {                                  
	                        	  long uviReading=0;
								sunClassificationFile.append(System.currentTimeMillis() +"\t" + classification + "\t" + uviReading + "\n");           
	                          } catch (IOException ex){
	                          }
	                          finally{
	                        	  sunClassificationFile.flush();
	                        	  sunClassificationFile.close();
	                          }
	                          
	                      } catch (IOException e) {
	                          e.printStackTrace();
	                      }
	                      
	                      
	                      //Reset the Values
	                      blockSize = 0;
	                      // time = System.currentTimeMillis();
	                      maxLightMagnitude = Double.MIN_VALUE;
	                      minLightMagnitude = Double.MAX_VALUE;
	                      stdLightMagnitude = 0;
	                      varianceIntensity = 0;
	                      meanAbsoluteDeveationLightIntensity = 0;
	                      meanLightIntensity = 0;
	                  }
	              } catch (Exception e) {
	                  e.printStackTrace();
	              }
			}
		}
	}
	
	
	// Timer object to periodically update final type
	Timer updateFinalTypeTimer = new Timer();
	
	/************ AsyncTask **************/

	private int mActivityInference = -1;
	private int mMaxActivityInferenceWindow = 5;//Allow 5 readings to dictate the Voted activity
	int inferenceCount=0;
	Map<Integer,Integer> mInferredActivityTypeMap = new HashMap<Integer,Integer>(Globals.FEAT_NUMBER_FEATURES);
	Map<Integer,Integer> mFinalInferredActivityTypeMap = new HashMap<Integer,Integer>();
	
	private class AccelerometerActivityClassificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			int blockSize = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] re = accBlock;
			double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			
			double max = Double.MIN_VALUE;
			double currentLeadCount = Double.MIN_VALUE;
			int currentTrend = Integer.MIN_VALUE;
			FileOutputStream predictionFile = null;
			FileOutputStream accelerometerValueFileStream = null;
			FileOutputStream accelerometerMedianFileStream = null;
			
			double sumForMean = 0;
			
			// Memory card path.
			File sdCard = Environment.getExternalStorageDirectory();
			// Application path.
    		String resultPredictor = sdCard + "/Android/data/edu.dartmouth.cs.myruns5/files"  + "/predicted.txt";    
    		String accelerometerValueLogger = sdCard + "/Android/data/edu.dartmouth.cs.myruns5/files"  + "/allValues.txt";
    		String accelerometerMedianLogger = sdCard + "/Android/data/edu.dartmouth.cs.myruns5/files"  + "/medianValues.txt";
    		
    		SortedSet accelerometerReadings = new TreeSet();
    		Double[] accelerometerReadingArray;
    		File resultFile;
    		File accelerometerLogFile;
    		File accelerometerMedianFile;
			try {
				resultFile = new File(resultPredictor);
				resultFile.createNewFile();
				predictionFile = new FileOutputStream(resultFile);
				 predictionFile.write("starting write".getBytes());
                 predictionFile.flush();
                 
                 accelerometerLogFile = new File(accelerometerValueLogger);
                 accelerometerLogFile.createNewFile();
                 accelerometerValueFileStream = new FileOutputStream(accelerometerLogFile);
                 
                 accelerometerMedianFile = new File(accelerometerMedianLogger);
                 accelerometerMedianFile.createNewFile();
                 accelerometerMedianFileStream = new FileOutputStream(accelerometerMedianFile);
                 
                  
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Create the timer task implementor class object with an intial state.
			// The various state values are required so that the timer task impelmentor can judiciously determine the dominant activity as per the latest trend.
			UpdateFinalTypeTask updateTask = new UpdateFinalTypeTask(mFinalInferredActivityTypeMap, mMotionUpdateBroadcast, getApplicationContext());
			updateFinalTypeTimer.schedule(updateTask,0,2000);
			while (true) {
				
				try {
					
					// need to check if the AsyncTask is cancelled or not in the while loop
					if (isCancelled () == true){
						
						// Set it as the final inferred type.
						//mMotionUpdateBroadcast.putExtra(VOTED_MOTION_TYPE, finalInferredType);
						//sendBroadcast(mMotionUpdateBroadcast);
						try {
							
							predictionFile.close();
							
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
					
					ArrayList<Double> featVect = new ArrayList<Double>(Globals.ACCELEROMETER_BLOCK_CAPACITY + 1);
					
					// Dumping buffer
					double accelValue = mAccBuffer.take().doubleValue();
					//String temp = String.valueOf(accelValue);
					//String temp2 = temp.substring(0,6);
					//accelerometerValueFile.write((temp2 + ",").getBytes());
					//accelerometerValueFile.flush();
					
					accBlock[blockSize++] = accelValue;
					
					// JERRID: Pops the "head" element from the Blocking Queue one at a time
					if(accelValue > max)
						max = accelValue;
					
					
					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						//Recieved a full block/disable data collection						
						pauseDataCollection();
						
						blockSize = 0;
						
						// time = System.currentTimeMillis();
						/* OLD CODE - jerrid
						max = .0;						 
						for (double val : accBlock) {
							if (max < val) {
								max = val;
							}
						}	
						*/
						fft.fft(re, im);
						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i] * im[i]);
							accelerometerReadings.add(mag);
							String temp = String.valueOf(mag);
							String temp2 = temp.substring(0,6);
							accelerometerValueFileStream.write((temp2 + ",").getBytes());
							accelerometerValueFileStream.flush();
							
							accelerometerReadingArray =
									Arrays.copyOf(
											accelerometerReadings.toArray(),
											accelerometerReadings.toArray().length, 
											Double[].class
											);
							int medianIndex = (accelerometerReadingArray.length)/2;
							double median = accelerometerReadingArray[medianIndex];
							accelerometerMedianFileStream.write(("\n Median was:" + median).getBytes());
							accelerometerMedianFileStream.flush();
							
							sumForMean = sumForMean + mag;
							accelerometerMedianFileStream.write(("\n Mean was:" + (sumForMean/accelerometerReadingArray.length)).getBytes());
							accelerometerMedianFileStream.flush();
							
							accelerometerMedianFileStream.write(("\n Current count is:" + (accelerometerReadingArray.length)).getBytes());
							accelerometerMedianFileStream.flush();
							featVect.add(mag);
							im[i] = .0; // Clear the field
						}
							
						// Append max after frequency component
						featVect.add(max);						
						int value = (int) WekaClassifier.classify(featVect.toArray());
						Log.d("mag", String.valueOf(value));
						

						//JERRID: Infer motion type based upon majority vote------
						int count = mInferredActivityTypeMap.containsKey(value) ? mInferredActivityTypeMap.get(value) : 0;
						int currentCount = count + 1;
						mInferredActivityTypeMap.put(value, currentCount);
						// Increment inference count to handle sliding window mechanism.
						inferenceCount++;
						
						
						// New code.
						// The count is regenerated each cycle.
						// Check which activity has gained lead in the current trend so far.
						// Associated that activity as the current dominant type activity.
						if(currentCount >= currentLeadCount ){
							// This activity has gained lead.Store its count for future comparision.
							currentLeadCount = currentCount;
							//The associated activity is to stored to identify the dominant trend.
							currentTrend = value;
						}
						
						/* JERRID: Old code
						 * This code block performs running mean of Activity inferences. 
						 * This won't work becasue of truncation (eg: 3+3+3+2+2/5 yeilds 2.8 roughly
						 * but 3 should be the inferred
						if(inferenceCount++ == 0){//1st motion value
							
		                	  mActivityInference = value;
						}else{//1..* activity values averaged using running mean
							mActivityInference = (value + mActivityInference * (inferenceCount-1)) / inferenceCount;
						}
						*/
		                  
						//Once the max inference count is reached, do a half sliding window 
						//(ie: the current mean inference value weight counts for 2 of next 5 readings)

	                	//int maxInferenceKey=-1,maxInferenceValue=-1;
	                	
	                	// Finished collection the 5 samples
	                	// Now increase the weight of an activity based on the current dominant trend.
		                if(inferenceCount == mMaxActivityInferenceWindow)  {
		                	try {		
		                		StringBuilder currentActivityBuilder = new StringBuilder();
		                		// List the current time.
		                		currentActivityBuilder.append("\n\nTime:" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + "\n");
		                		// List the dominant activity
		                		currentActivityBuilder.append("Current activity predicted:" + (currentTrend == -1 ? "NA" : Globals.INFERENCE_LIST[currentTrend]) + ".\n");
		                		// List all the activities.
		                		currentActivityBuilder.append("The recorded set of activities in the current cycle is as follows:\n");
		                		for (Map.Entry<Integer, Integer> entry : mInferredActivityTypeMap.entrySet()) {
		                			// List each activity along with count.
		                			currentActivityBuilder.append(Globals.INFERENCE_LIST[entry.getKey()] + " occured " + entry.getValue() + " times. \n");
		                		}
		                        predictionFile.write(currentActivityBuilder.toString().getBytes());
		                        predictionFile.flush();
		                    }
		                    catch (IOException e) {
		                    	predictionFile.close();
		                        Log.e("Exception", "File write failed: " + e.toString());
		                    }
		                	
		                	// Reset the entire map
		                	mInferredActivityTypeMap.clear();
		                	// New code.
		                	// After clearing the entire map increased the weight for the current dominant trend.
		                	// Unless some other activity dominates this activity in the next cycle, this would continue to be the dominant trend.
		                	mInferredActivityTypeMap.put(currentTrend, 2);
		                	//Reset current lead count and the inference count.
		                	currentLeadCount = 0;
		                	inferenceCount = 0;
		                	//Old Code --- inferenceCount = 2;
		                	//iterating over keys only
		                	//int maxIndex = -1;
		                	//for (Map.Entry<Integer, Integer> entry : mInferredActivityTypeMap.entrySet()) {
		                		// Reset the mapping.
		                		// Reset all entries to 0.
		                		//entry.setValue(0);
		                		//int val = entry.getValue();
		                		
		                		//Old code
		                		/*
		                		if(val > maxIndex){
		                			maxIndex = val;
		                			maxInferenceValue = val;
		                			maxInferenceKey = entry.getKey();
		                		}
		                		*/
		                		// Reset instance counts to 0 to avoid a 2nd for loop to clear vector
		                		// All counts have been reset to 0.
		                		// entry.setValue(0);
		                	//}
		                
		                	// Weight the Mode activity with the highest
		                	// mInferredActivityTypeMap.put(maxInferenceKey, 2);
		                	
		                	// New code.
		                	// After clearing the entire map increased the weight for the current dominant trend.
		                	// Unless some other activity dominates this activity in the next cycle, this would continue to be the dominant trend.
		                	//mInferredActivityTypeMap.put(currentTrend, 2);
		                	//Reset current lead count and the inference count.
		                	currentLeadCount = 0;
		                	inferenceCount = 0;
		                	
		                	// The window is done. Write to an output file a prediction indicating the current activity.
		                	
		                }
						
						//int maxIndex = 0;their original code
						//if (mInferenceCount[maxIndex] < mInferenceCount[1]) maxIndex = 1;
						//if (mInferenceCount[maxIndex] < mInferenceCount[2]) maxIndex = 2;their original code
		                
		                // Here specify the current trend.
						mInferredActivityType = Globals.INFERENCE_MAPPING[currentTrend == -1 ? value : currentTrend];//maxIndex];
						int currentActivity = Globals.INFERENCE_MAPPING[value];
						mMotionUpdateBroadcast.putExtra(CURRENT_MOTION_TYPE, currentActivity);
						updateTask.SetCurrentType(currentActivity);

						//mMotionUpdateBroadcast.putExtra(CURRENT_MOTION_TYPE, currentActivity);
						// send broadcast with the CURRENT activity type
						//---------------
						
						// For this activity get the current count in the mapping
						// If the first time put it into a map with a cound of 1.
						 int finalTypeCount = mFinalInferredActivityTypeMap.containsKey(mInferredActivityType)?mInferredActivityTypeMap.get(mInferredActivityType):0;
						 mFinalInferredActivityTypeMap.put(mInferredActivityType, finalTypeCount + 1);
						 
						 // Updates the timer task class with the latest status
						 // This is required for the timer task implementor class to judicious determine the dominant activity as per the latest trend.
						 updateTask.SetFinalInferredActivityTypeMap(mFinalInferredActivityTypeMap);
						 updateTask.SetIntent(mMotionUpdateBroadcast);
						 updateTask.SetContext(getApplicationContext());
						 
						 // Used to update the current type.
						 sendBroadcast(mMotionUpdateBroadcast);
						
						//Reset the max value
						max = Double.MIN_VALUE;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}		
			}
		}
		
		// New code - added by lohith 
		// Calculates final velocity based upong avg accleration and initial velocity.
		// To be called periodically.
		private double CalculateFinalVelocity() {
			avgAcceleration = 0;
			timeDuration = 5000;
			initialVelocity = 0;
			
			double currentVelocity = initialVelocity + (avgAcceleration * timeDuration);
			return currentVelocity;
		}
		
		// Added by lohith
		// Periodically queries the yahoo API to get the weather details.
		// to be called once per minute
		// Get the temperature and humidity.
		private void getWeatherDetails() {
			@SuppressWarnings("deprecation")
			DefaultHttpClient httpclient = new DefaultHttpClient();
		    try {
		      // specify the host, protocol, and port
		      HttpHost target = new HttpHost("weather.yahooapis.com", 80, "http");

		      // specify the get request
		      HttpGet getRequest = new HttpGet("/forecastrss?p=80020&u=c");

		      //System.out.println("executing request to " + target);

		      HttpResponse httpResponse = httpclient.execute(target, getRequest);
		      HttpEntity entity = httpResponse.getEntity();

		      Header[] headers = httpResponse.getAllHeaders();
		   
		      if (entity != null) {
		        //System.out.println(EntityUtils.toString(entity));
		    	  String obtainedData = EntityUtils.toString(entity);
		    	  
		    	  // Get temperature
		    	  int tempIndex = obtainedData.indexOf("temp=");
		    	  temperature = obtainedData.substring(tempIndex+6, tempIndex+8);
		    	  
		    	  // Get humidity
		    	  int humidityIndex = obtainedData.indexOf("humidity=");
		    	  humidity = obtainedData.substring(humidityIndex+10, humidityIndex+12);
		    	  
		      }

		    } catch (Exception e) {
		      e.printStackTrace();
		    } finally {
		      // When HttpClient instance is no longer needed,
		      // shut down the connection manager to ensure
		      // immediate deallocation of all system resources
		      httpclient.getConnectionManager().shutdown();
		    }
		  }
		}
	
	
	private class TemperatureSensorActivityClassificationTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				System.out.println(mTempBuffer.take().doubleValue());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
			}
	}
	
}



 // Utility TimerTask implementor class to periodically update the final time.
// We can configure the interval , between each call to the run method to this class
class UpdateFinalTypeTask extends TimerTask {
	   
	// Activities vs their respective count.
	Map<Integer,Integer> mFinalInferredActivityTypeMap;
	
	//Intent of the calling module
	Intent mMotionUpdateBroadcast;
	
	//Context of the calling module
	Context mAppContext;
	
	//Initially set the current type to standing
	// It could be changed once we process the activity map.
	private int mCurrentType = 13;
	
	//Constructor to be called to initialize the class object.
	public UpdateFinalTypeTask(Map<Integer,Integer> finalInferredActivityTypeMap, Intent motionUpdateBroadcast, Context appContext) {
		mFinalInferredActivityTypeMap = finalInferredActivityTypeMap;
		mMotionUpdateBroadcast = motionUpdateBroadcast;
		mAppContext = appContext;
	}
	
	// Sets the activity vs count map.
	// map used by the worker to choose and set the final type.
	// Set api is required as the activity mapping would change over the course of the user action.(First stand, then run etc)
	public void SetFinalInferredActivityTypeMap(Map<Integer,Integer> finalInferredActivityTypeMap) {
		mFinalInferredActivityTypeMap = finalInferredActivityTypeMap;
	}
	
	// Sets the intent.
	// Required by the worker.
	public void SetIntent(Intent motionUpdateBroadcast) {
		mMotionUpdateBroadcast = motionUpdateBroadcast;
	}
	
	// Sets the context
	// Required by the worker.
	public void SetContext(Context appContext) {
		mAppContext = appContext;
	}
	
	// Sets the current activity type
	// Required by the worker before updating.
	public void SetCurrentType(int currentType) {
		mCurrentType = currentType;
	}

	// Constant element required to update the final type.
	public static final String VOTED_MOTION_TYPE = "voted motion type";
	
	// Constant element required to update the current type.
	public static final String CURRENT_MOTION_TYPE = "new motion type";
	
	// worker method
	   public void run() {
			// Temp element used in getting the maximum value.
			int maxElement = Integer.MIN_VALUE;
			// By default standing. Would be updated as and when we poll the activity map.
			int finalInferredType = 13;
			
			// Take into consideration all the activities which has been tabulated so far.
			// check which activity has run the maximum number of times.
			// Set them as the final activity.
			for (Map.Entry<Integer, Integer> entry : mFinalInferredActivityTypeMap.entrySet()) {
				if(entry.getValue() > maxElement) {
					// Currently this activity has the maximum count.
					maxElement = entry.getValue();
					// Final type is obtained.
					finalInferredType = entry.getKey();		
				}			
			}
			// Set the current type.
			mMotionUpdateBroadcast.putExtra(CURRENT_MOTION_TYPE, mCurrentType);
			// Set the final type.
			mMotionUpdateBroadcast.putExtra(VOTED_MOTION_TYPE, finalInferredType);
			
			// Send the broad cast. It updates the UI.
			mAppContext.sendBroadcast(mMotionUpdateBroadcast);
	   }	   
	}


