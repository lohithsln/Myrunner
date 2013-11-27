package edu.dartmouth.cs.myruns5;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Spinner;

public class MainActivity extends Activity {
	public static final String KEY_TAB_INDEX = "tab index";
	public static final String ACTIVITY_TYPE = "activity type";
	public static final String INPUT_TYPE = "input type";
	public static final String TASK_TYPE = "task type";
	public static final String SWEAT_RATE = "sweat rate";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// set up action bar
		 final Intent currentUVIIntent = new Intent(this, UltravioletIndexService.class);
		 currentUVIIntent.setAction(UltravioletIndexService.CURRENT_UV_INDEX);
		 startService(currentUVIIntent);
	    
		
		ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// create fragments
		Fragment startFragment = new StartFragment();
		Fragment historyFragment = new HistoryFragment();
		Fragment settingsFragment = new SettingsFragment();
		Fragment uviFragment = new CurrentUVIFragment();
		
		
		// create tabs
		ActionBar.Tab startTab = bar.newTab().setText(getString(R.string.startTab_title));
		startTab.setTabListener(new MyTabListener(startFragment, getApplicationContext()));
		
		ActionBar.Tab historyTab = bar.newTab().setText(getString(R.string.historyTab_title));
		historyTab.setTabListener(new MyTabListener(historyFragment, getApplicationContext()));
		
		ActionBar.Tab settingsTab = bar.newTab().setText(getString(R.string.settingsTab_title));
		settingsTab.setTabListener(new MyTabListener(settingsFragment, getApplicationContext()));
		
		ActionBar.Tab uviTab = bar.newTab().setText(getString(R.string.uviTab_title));
		uviTab.setTabListener(new MyTabListener(uviFragment, getApplicationContext()));
		
		
		// add tabs
		bar.addTab(startTab);
		bar.addTab(historyTab);
		bar.addTab(settingsTab);
		bar.addTab(uviTab);
		
		// resume state if applicable
		if (savedInstanceState != null){
			bar.setSelectedNavigationItem(savedInstanceState.getInt(KEY_TAB_INDEX));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_TAB_INDEX, getActionBar().getSelectedNavigationIndex());
	}

	public void onStartClicked(View view){
	    // check input type
	    Spinner spinner = (Spinner)findViewById(R.id.Spinner_InputType);
	    int pos = spinner.getSelectedItemPosition();
	    Intent intent;
	    switch(pos){
	    case 0:
		    intent = new Intent(this, ManualInputActivity.class);
		    break;
	    case 1:
	    case 2:
		    intent = new Intent(this, MapDisplayActivity.class);
		    break;
	    default:
		    intent = new Intent(this, MapDisplayActivity.class);
	    }
	    
	    // put extra
	    intent.putExtra(INPUT_TYPE, pos);

	    spinner = (Spinner)findViewById(R.id.Spinner_ActivityType);
	    pos = spinner.getSelectedItemPosition();
	    intent.putExtra(ACTIVITY_TYPE, pos);
	    intent.putExtra(TASK_TYPE, Globals.TASK_TYPE_NEW);
	    
	    // fire intent
	    startActivity(intent);

	}
}

class MyTabListener implements ActionBar.TabListener{
	public Fragment mFragment;
	public Context mContext;
	
	public MyTabListener(Fragment frgmt, Context cntxt){
		mFragment = frgmt; mContext = cntxt;
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft){
		ft.replace(R.id.fragment_container, mFragment);
//		Toast.makeText(mContext, "onTabSelected", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft){
//		Toast.makeText(mContext, "onTabReSelected", Toast.LENGTH_SHORT).show();		
	}
	
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft){
		ft.remove(mFragment);
//		Toast.makeText(mContext, "onTabUnSelected", Toast.LENGTH_SHORT).show();		
	}
}