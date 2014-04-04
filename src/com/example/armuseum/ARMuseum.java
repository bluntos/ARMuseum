package com.example.armuseum;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ARMuseum extends Activity implements OnClickListener {
	
	static final String LOGTAG = "ARMuseum";

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mMenuTitles;

	private Button scanBtn;
	private TextView formatTxt, contentTxt;
	
	private ImageView mSplashScreen = null;
	
	
	//Add by Lei Wang for AR Scan View
	private Button arScanBtn;
	
	private ScheduledExecutorService scheduleTaskExecutor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		Log.d(LOGTAG, "onCreate");
		
		mTitle = mDrawerTitle = getTitle();
		mMenuTitles = getResources().getStringArray(R.array.menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
        
        scanBtn = (Button) findViewById(R.id.scan_button);
		formatTxt = (TextView) findViewById(R.id.scan_format);
		contentTxt = (TextView) findViewById(R.id.scan_content);
		
		arScanBtn = (Button) findViewById(R.id.ar_button);
//		arScanBtn.setOnClickListener(this);
		
		mSplashScreen = (ImageView)findViewById(R.id.imageSplash);
		
		Log.d(LOGTAG, "Leaving onCreate");
		scheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduleTaskExecutor.schedule(new DismissSplash(this), 5, TimeUnit.SECONDS);
	}

	class DismissSplash implements Runnable
	{
		private final ARMuseum mMainActivity;

		public DismissSplash(ARMuseum mainActivity)
		{
			mMainActivity = mainActivity;
		}

		public void run()
		{
			mMainActivity.runOnUiThread(new DoDismissSplash());
		}
		
		class DoDismissSplash implements Runnable
		{
			public void run()
			{
				mMainActivity.onSplashDone();
			}
		}
	}
	
	public void onSplashDone()
	{
		Log.d(LOGTAG, "onSplashDone");
		mSplashScreen.setVisibility(View.GONE);
		
		// scanBtn.setOnClickListener(this);
				
	}

	

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanningResult != null) {
			// we have a result
			String scanContent = scanningResult.getContents();
			String scanFormat = scanningResult.getFormatName();

			formatTxt.setText("FORMAT: " + scanFormat);
			contentTxt.setText("CONTENT: " + scanContent);

		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"No scan data received!", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
    @Override
	public void onClick(View v) {
		
		switch (v.getId()) {
			case R.id.scan_button:
				// scan
				Log.d(LOGTAG, "scan_button pressed");
				IntentIntegrator scanIntegrator = new IntentIntegrator(this);
				scanIntegrator.initiateScan();
				break;
				
			case R.id.ar_button:
				// start activity to AR scan and play video
				Log.d(LOGTAG, "ar_button pressed");
				//startARActivity();
				break;
		}

	}
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	         // The action bar home/up action should open or close the drawer.
	         // ActionBarDrawerToggle will take care of this.
	        if (mDrawerToggle.onOptionsItemSelected(item)) {
	            return true;
	        }
	        else{
	            return super.onOptionsItemSelected(item);
	        }
	    }
	
	 /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new MenuFragment();
        Bundle args = new Bundle();
        args.putInt(MenuFragment.ARG_MENU_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a  menu
     */
    public static class MenuFragment extends Fragment {
        public static final String ARG_MENU_NUMBER = "menu_number";

        public MenuFragment() {
            // Empty constructor required for fragment subclasses
        }

    	// Starts the AR Scan activity
        private void startARActivity()
        {
            Intent itent = new Intent();
            
            itent.setClassName("com.qualcomm.vuforia.samples.VideoPlayback.app.", "VideoPlayback");
            
            startActivity(itent);
        }
        
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
            int i = getArguments().getInt(ARG_MENU_NUMBER);
            String menu = getResources().getStringArray(R.array.menu_array)[i];

            if(i == 1 ){
            	Log.d(LOGTAG, "scan_button selected");
            	IntentIntegrator scanIntegrator = new IntentIntegrator(this);
				scanIntegrator.initiateScan();
            }
            
            if(i == 2 ){
            	Log.d(LOGTAG, "AR scan_button selected");
            	startARActivity();
            }
            
            getActivity().setTitle(menu);
            return rootView;
        }
    }

}
