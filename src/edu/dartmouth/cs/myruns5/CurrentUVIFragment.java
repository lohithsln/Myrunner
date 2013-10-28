package edu.dartmouth.cs.myruns5;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Messenger;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CurrentUVIFragment extends Fragment {
	Messenger myService = null;
	boolean isBound;
	private Timer timer;
  	private UVIBroadcastReciever reciever;
  	private IntentFilter filter;
  	
  	private TimerTask updateUVITask = new TimerTask() {
     @Override
     public void run() 
     {
    	 //Call the UVI Service to grab the current UVI
    	 updateUVIWidget();
     }
   };
   final int updateInterval = 15;//minutes
   
   private void updateUVIWidget(){
	   final Intent currentUVIIntent = new Intent(getActivity(), UltravioletIndexService.class);
  	 currentUVIIntent.setAction(UltravioletIndexService.CURRENT_UV_INDEX);
      getActivity().startService(currentUVIIntent);
   }
   
   //Recieves the current UVI broadcast updates from the Service
   class UVIBroadcastReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			float uvi = arg1.getExtras().getFloat(UltravioletIndexService.CURRENT_UV_INDEX);
			TextView currentUVIView = (TextView) getView().findViewById(R.id.current_uvi);
			currentUVIView.setText(Float.toString(uvi));
			

	        Calendar now = Calendar.getInstance();
	        int minute = now.get(Calendar.MINUTE);//24 hr format
	        long firstExecutionDelay = (updateInterval - (minute % updateInterval)) * Globals.ONE_MINUTE;
	        
	        //Schedule the next execution for the next 15 minutes
	        timer = new Timer();
	        timer.schedule(updateUVITask, firstExecutionDelay);
		}
	}

   private void registerReciever(){
       filter = new IntentFilter(UltravioletIndexService.CURRENT_UV_INDEX);
       reciever = new UVIBroadcastReciever();
       getActivity().registerReceiver(reciever,filter);
   }
   
   @Override
   public void onResume() {
       super.onResume();
       registerReciever();       
   }

   @Override
   public void onPause() {
       super.onPause();
       if (reciever != null) {
    	   getActivity().unregisterReceiver(reciever);
           reciever = null;
           filter = null;
       }
   }
   

   private ExecutorService executorService;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		executorService = Executors.newSingleThreadExecutor();
		registerReciever();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				updateUVIWidget();
	        }
		});
		return inflater.inflate(R.layout.uvg_fragment_current_uvi, container, false);
	}
}
