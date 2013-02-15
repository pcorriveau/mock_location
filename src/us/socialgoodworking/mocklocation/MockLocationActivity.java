/*
 * Copyright (C) 2012 Paul Corriveau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.socialgoodworking.mocklocation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import us.socialgoodworking.ezlocation.mock.MockLocation;
import us.socialgoodworking.mocklocation.R;
import us.socialgoodworking.utility.Logging;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;

/**
 * Main activity used to set fake GPS locations returned by the GPS provider for testing purposes.
 * 
 * @author Paul Corriveau  <a href="mailto:paul@socialgoodworking.us?subject=MockLocation">paul@socialgoodworking.us</a>
 * @version 1.0.0
 *
 */
public class MockLocationActivity extends SherlockFragmentActivity implements OnMapClickListener, OnMarkerClickListener, SelectRouteDialog.SelectRouteDialogListener, RecordRouteDialog.RecordRouteDialogListener {

	static final String TAG = "MockLocation";
	static boolean SUPPORTS_JELLY_BEAN = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
	static boolean SUPPORTS_ICE_CREAM_SANDWICH = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	
	private static final String providerName = LocationManager.GPS_PROVIDER;
	private static final int TAP_MODE = 0;
	private static final int PLAYBACK_MODE = 1;
	private static final int RECORDING_MODE = 2;
	private static String routeDir = "routeList";
	private static String routeFile = "routeList.txt";
	
	boolean bSwitchButtonPlacement = !SUPPORTS_ICE_CREAM_SANDWICH;
	private int currentMode;
	private GoogleMap map= null;
	
	// Central park, Manhattan, NY
	private LatLng defaultLatLng = new LatLng (40.76793169992044, -73.98180484771729);
	private LatLng currentLatLng = defaultLatLng;
	private LatLng previousLatLng = defaultLatLng;
	private LatLng startingRecordPoint;
	private Marker marker;
	
	private MockLocation mockLocation;
	private Button btnStartStop;
	
	// Has either a playback or recording started?
	private boolean bStarted = false;
    private Handler handler;
    private long updateInterval = 5000;	// every 5 seconds...
    
    private MenuItem menuItemPlayback;
    private MenuItem menuItemRecord;
    
    private Routes routeList;
    private String currentRoute;
    private int currentRouteLocationIndex;
    
    private boolean mockPermissionEnabled = true;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Logging.debug(TAG, TAG, "onCreate");
        
        getSupportActionBar().setDisplayShowTitleEnabled(true);  
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        Drawable d = getResources().getDrawable(R.drawable.socialgoodworking_ab_back1);
        getSupportActionBar().setBackgroundDrawable(d);

       	btnStartStop = (Button) findViewById(R.id.btnStartStop);
       	btnStartStop.setVisibility(View.GONE);
       	
       	btnStartStop.setOnClickListener(clickListener);
       	btnStartStop.setTextColor(getResources().getColorStateList(R.color.button_colors));
       	
       	currentRouteLocationIndex = 0;
       	
        if(savedInstanceState != null ) {
        	Logging.debug(TAG, "onCreate", "savedInstanceState != null");
        	double lat;
        	double lng;
        	
        	currentMode = savedInstanceState.getInt("mode", TAP_MODE);
        	bStarted = savedInstanceState.getBoolean("started", false);
        	
        	lat = savedInstanceState.getDouble("currentLat", defaultLatLng.latitude);
        	lng = savedInstanceState.getDouble("currentLng", defaultLatLng.longitude);
        	currentLatLng = new LatLng(lat, lng);
        	
        	lat = savedInstanceState.getDouble("previousLat", defaultLatLng.latitude);
        	lng = savedInstanceState.getDouble("previousLng", defaultLatLng.longitude);
        	previousLatLng = new LatLng(lat, lng);
        	
        	currentRoute = savedInstanceState.getString("currentRoute");
        	currentRouteLocationIndex = savedInstanceState.getInt("index", 0);
        }
        else {
        	Logging.debug(TAG, "onCreate", "savedInstanceState =!= null");
           	currentMode = TAP_MODE;
           	currentRoute = "";
        }

       	try {
       		mockLocation = new MockLocation.MockLocationBuilder(getApplicationContext(), providerName).build();
       		mockLocation.removeProvider();
       		mockLocation.setUpMockProvider();
       		Logging.debug(TAG, "onCreate", "mockLocation instance created");
       	}
       	
       	catch (SecurityException se) {
       		Logging.debug(TAG, "onCreate", se.getMessage());
       		mockPermissionEnabled = false;
       		showSecurityErrorDlg();
       	}
       	
       	setUpMapIfNeeded();
       	
	    handler = new Handler();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    	Logging.debug(TAG, TAG, "onResume");

        readRoutes();

        // readRoutes() creates routeList.
        if (currentRoute != null && !currentRoute.isEmpty()) {
    		routeList.setCurrentIndex(currentRoute, currentRouteLocationIndex);
    	}

    }

    @Override
    protected void onPause () {
    	super.onPause();
    	Logging.debug(TAG, TAG, "onPause");
    	if (currentRoute != null && !currentRoute.isEmpty() && currentMode == RECORDING_MODE) {
    		saveRoutes();
    	}
    }
    
    @Override
    protected void onStop () {
    	super.onStop();
    	Logging.debug(TAG, TAG, "onStop");
    }
    
    // Save the activity state in case there's an orientation change...
    @Override
    protected void onSaveInstanceState(Bundle saveState) {
        saveState.putInt("mode", currentMode);
        saveState.putDouble("currentLat", currentLatLng.latitude);
        saveState.putDouble("currentLng", currentLatLng.longitude);
        saveState.putDouble("previousLat", currentLatLng.latitude);
        saveState.putDouble("previousLng", currentLatLng.longitude);
        saveState.putString("currentRoute", currentRoute);
        saveState.putBoolean("started", bStarted);
        
        if (currentRoute != null && !currentRoute.isEmpty()) {
        	saveState.putInt("index", routeList.getCurrentIndex(currentRoute));
        }

        super.onSaveInstanceState(saveState);
    }
    
    @Override
    protected void onDestroy () {
    	Logging.debug(TAG, "onDestroy", "removing mock provider"); 
    	mockLocation.removeProvider();
    	
    	switch (currentMode) {
			case TAP_MODE:
				handler.removeCallbacks(tapTask);
				break;
			
			case PLAYBACK_MODE:
				handler.removeCallbacks(playbackTask);
				if (bStarted) {
					// A bit of a kludge just to save the index of the next position when we're in the middle of a playback
					// so we can continue from where we left off...
					saveRoutes();
				}
				
				break;
				
//			case RECORDING_MODE:
//				if (currentRoute != null && !currentRoute.isEmpty()) {
//					saveRoutes();
//				}
//				
//				break;
    	}

    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	Logging.debug(TAG, TAG, "onCreateOptionsMenu");
    	getSupportMenuInflater().inflate(R.menu.menu_activity_main, menu);
    	menuItemPlayback = menu.findItem(R.id.tap_or_playback);
    	menuItemRecord = menu.findItem(R.id.record);
    	
    	// Restore the activity view here because we need access to the menu items in the action bar...
    	restoreActivityView(currentMode);
    	
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;

    	switch(item.getItemId()) {
    		// Toggle for 'playback' mode...
    		case R.id.tap_or_playback:
    			onTapOrPlayback();
    			break;
    		
    		case R.id.record:
    			onRecord();
    			break;
    			
    		case R.id.about:
				 i = new Intent(this, AboutActivity.class);
				 Logging.debug(TAG, TAG, "onOptionsItemSelected starting About...");
				 startActivity(i);
				 break;
    
    		case R.id.policy:
				 i = new Intent(this, PolicyActivity.class);
				 Logging.debug(TAG, TAG, "onOptionsItemSelected starting About...");
				 startActivity(i);
    			break;
    			
//    		case R.id.legal:
//      			String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
//      			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(this);
//      			LicenseDialog.setTitle("Legal Notices");
//      			LicenseDialog.setMessage(LicenseInfo);
//      			LicenseDialog.show();
//         		break;
 
    		case R.id.contact_us:
    			try {
    				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
    				emailIntent.setType("plain/text");
    				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"paul@socialgoodworking.us"});  
    				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MockLocation Comment");  
    				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "We want your input! Let us know what you think...");  
    				startActivity(Intent.createChooser(emailIntent, "Contact us via:"));  
    			} 
    			catch (Exception ex) {
    				Logging.debug(TAG, "onOptionsItemSelected", ex.getMessage());
    			}
    			break;
    	}

    	return true;
    }
    
    private Runnable playbackTask = new Runnable() {
	   public void run() {
		   previousLatLng = currentLatLng;
		   currentLatLng = routeList.getNext(currentRoute);
		   
		   float speed = mockLocation.getSpeed(previousLatLng.latitude, previousLatLng.longitude, currentLatLng.latitude, currentLatLng.longitude, updateInterval);
		   float bearing = mockLocation.getBearing(previousLatLng.latitude, previousLatLng.longitude, currentLatLng.latitude, currentLatLng.longitude);
				   
		   //Logging.debug(TAG, "playbackTask: ", "speed = " + Float.toString(speed) + ", bearing = " + Double.toString(bearing) + ", update interval = " + updateInterval);

		   mockLocation.setMockLocation(currentLatLng.latitude, currentLatLng.longitude, 0, speed, bearing);
		   marker.setPosition(currentLatLng);
		   map.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
		   handler.postDelayed(this, updateInterval);
	   }
	};
	
    private Runnable tapTask = new Runnable() {
	   public void run() {
		   //Logging.debug(TAG, "tapTask: ", Double.toString(currentLatLng.latitude) + ", " +  Double.toString(currentLatLng.longitude));
		   mockLocation.setMockLocation(currentLatLng.latitude, currentLatLng.longitude, 0, 0, 0);
		   handler.postDelayed(this, 1000);
	   }
	};
	
   private View.OnClickListener clickListener = new View.OnClickListener() {
        public void onClick(View v) {

        	if (v.getId() == R.id.btnStartStop) {
        		if (currentMode == PLAYBACK_MODE && mockPermissionEnabled ) {
	        		if (!bStarted)
	        		{
	        			bStarted = true;
	        			btnStartStop.setText("Stop Playback");
	        			menuItemPlayback.setIcon(R.drawable.ic_action_play_on);
	        			updateInterval = routeList.getUpdateInterval(currentRoute);
	                	Logging.debug(TAG, "onClick", "PLAYBACK_MODE started"); 
	        			handler.postDelayed(playbackTask, 0);
	        		}
	        		else {
	        			bStarted = false;
	        			btnStartStop.setText("Start Playback");
	        			handler.removeCallbacks(playbackTask);
	        			menuItemPlayback.setIcon(R.drawable.ic_action_play);
       				   	marker.setPosition(currentLatLng);
	       				map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
	                	Logging.debug(TAG, "onClick", "PLAYBACK_MODE stopped"); 
        			}
        		}
        		else if (currentMode == RECORDING_MODE) {
        			if ( bStarted) {
        				bStarted = false;
        				// Save the route to file???
	        			btnStartStop.setText("Start Recording");
	        			menuItemRecord.setIcon(R.drawable.ic_action_record);
	        			marker.setPosition(startingRecordPoint);
	        			map.animateCamera(CameraUpdateFactory.newLatLng(startingRecordPoint));
        			}
        			else {
        				bStarted = true;
	        			btnStartStop.setText("Stop Recording");
	        			menuItemRecord.setIcon(R.drawable.ic_action_record_on);
	        			
	        			updateInterval = routeList.getUpdateInterval(currentRoute);
	        			
	        			// Add the current point as the start ????
	        			routeList.addPoint(currentRoute, marker.getPosition());
	        			
	        			// Save the start point 
	        			startingRecordPoint = marker.getPosition();
        			}
        		}
        	}
        }
    };
    
	public void onMapClick(LatLng point) {
 		Logging.debug(TAG, "onMapClick", "routeList = " + (routeList == null? "null" : "not null"));

		switch(currentMode) {
		 	case TAP_MODE:
		    	Logging.debug(TAG, "onMapClick:", Double.toString(point.latitude) + ", " +  Double.toString(point.longitude));
		    	map.moveCamera(CameraUpdateFactory.newLatLng(point));
				marker.setTitle("MockLocation");
				marker.setSnippet(Double.toString(point.latitude) + ", " +  Double.toString(point.longitude));
		    	marker.setPosition(point);
		    	if (mockPermissionEnabled) {
		    		mockLocation.setMockLocation(point.latitude, point.longitude);
		    	}
		    	currentLatLng = point;
		 		break;
		 		
		 	case PLAYBACK_MODE:
		 		// Nothing to do...
		 		break;
		 		
		 	case RECORDING_MODE:
		 		if (bStarted) {
		 			// Add the point to the route...
		 			routeList.addPoint(currentRoute, point);
			    	map.moveCamera(CameraUpdateFactory.newLatLng(point));
					marker.setTitle("MockLocation");
					marker.setSnippet(Double.toString(point.latitude) + ", " +  Double.toString(point.longitude));
			    	marker.setPosition(point);
		 		}
		 		else {
			    	map.moveCamera(CameraUpdateFactory.newLatLng(point));
					marker.setTitle("MockLocation");
					marker.setSnippet(Double.toString(point.latitude) + ", " +  Double.toString(point.longitude));
			    	marker.setPosition(point);
		 		}
		 		
		 		break;
		}
	}

	public boolean onMarkerClick(Marker marker) {
		if ( marker.isInfoWindowShown())
			marker.hideInfoWindow();
		else
			marker.showInfoWindow();
		
		return false;
	}
	
    private void setUpMapIfNeeded() {
        if (map == null) {
        	Logging.debug(TAG, "setUpMapIfNeeded", "map == null"); 
    		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.locationmap)).getMap();
            if (map != null) {
            	map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            	map.setOnMapClickListener(this);
            }
            else {
            	Logging.debug(TAG, "setUpMapIfNeeded", "map == null; getSupportFragmentManager returned null"); 
            	return;
            }
        }
            
    	if ( marker == null ) {
    		marker = map.addMarker(new MarkerOptions().position(defaultLatLng).title("Greenwich observatory").snippet("Airy transit circle").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    	}

    	//if ( savedInstanceState != null ) {
    	//	double lat = savedInstanceState.getDouble("lat");
    	//	double lon = savedInstanceState.getDouble("lon");
			
    	// Apparently affects click sensitivity around marker...
    	if (marker.isDraggable()) {
    		Logging.debug(TAG, "setUpMapIfNeeded", "marker.setDraggable(false)"); 
			marker.setDraggable(false);
    	}
			
    		marker.setTitle("MockLocation");
    		marker.setSnippet(Double.toString(currentLatLng.latitude) + ", " +  Double.toString(currentLatLng.longitude));
    		marker.setPosition(currentLatLng);
    		map.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    	//}
    }
    
    private void onTapOrPlayback() {

		switch(currentMode) {
    	 	case TAP_MODE:
    	 		if (!mockPermissionEnabled) {
    	 			return;
    	 		}
    	 		
    	 		if ( routeList == null || routeList.routes.isEmpty()) {
    	 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    	 			dialog.setTitle("Select a Route");
    	 			dialog.setMessage("There are no saved routes.");
    	 			dialog.setCancelable(false).setPositiveButton(android.R.string.ok, null);
    	 	       	AlertDialog alertDialog = dialog.create();
    	 	       	alertDialog.show();
    	 		}
    	 		else {
	    	 		menuItemRecord.setVisible(false);
	    	 		btnStartStop.setText("Start Playback");
	    	 		currentMode = PLAYBACK_MODE;
	    	 		handler.removeCallbacks(tapTask);
	    			showPlaybackRouteSelectDlg();
    	 		}
    	 		
    	 		break;
    	 		
    	 	case PLAYBACK_MODE:
    	 		currentMode = TAP_MODE;
    	 		handler.removeCallbacks(playbackTask);
    	 		btnStartStop.setText("Start");
    	 		//btnStartStop.setEnabled(false);
    	 		btnStartStop.setVisibility(View.GONE);
    	 		bStarted = false;
    	 		menuItemRecord.setVisible(true);
    	 		menuItemPlayback.setIcon(R.drawable.ic_action_play);
			   	marker.setPosition(currentLatLng);
			   	map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
			   	handler.postDelayed(tapTask, 0);
    	 		return;
    	 }
    }
    
    private void onRecord() {

		switch(currentMode) {
    	 	case TAP_MODE:
    	 		currentMode = RECORDING_MODE;
    			menuItemPlayback.setVisible(false);
    			handler.removeCallbacks(tapTask);
    			showRecordRouteDlg();
    	 		break;
    	 		
    	 	case RECORDING_MODE:
    			currentMode = TAP_MODE;
    			menuItemRecord.setIcon(R.drawable.ic_action_record);
    			menuItemPlayback.setVisible(true);
    			bStarted = false;
    			btnStartStop.setVisibility(View.GONE);
    			
    			if (routeList != null && routeList.routes.size() > 0 && !routeList.getRoute(currentRoute).isEmpty()) {
    				saveRoutes();
    			}
    			else {
    				Logging.debug(TAG, "onRecord", "No route points in " + currentRoute + ". Removing route name from list."); 
    				routeList.deleteRoute(currentRoute);
    			}
    			
    			handler.postDelayed(tapTask, 0);
    			
    	 		break;
    	 }
    }
    
    private void showRecordRouteDlg() {
    	DialogFragment f = new RecordRouteDialog();
    	f.show(getSupportFragmentManager(), "record");
    }
    
    private void showSecurityErrorDlg() {
    	DialogFragment f = new SecurityErrorDialog();
    	f.show(getSupportFragmentManager(), "error");
    }
    
    
    private void showPlaybackRouteSelectDlg() {
    	DialogFragment f = new SelectRouteDialog();
    	Bundle b = new Bundle();
    	b.putStringArray("routes", routeList.getRoutes());
    	f.setArguments(b);
    	f.show(getSupportFragmentManager(), "routes");
    }

	public void onSelectRouteDialogPositiveClick(DialogInterface dialog, String selectedRoute) {
		if (selectedRoute == null || selectedRoute.isEmpty()) {
			dialog.dismiss();
			return;
		}
		
		currentRoute = selectedRoute;
		dialog.dismiss();
		Logging.debug(TAG, "onDialogPositiveClick", " currentRoute = " + currentRoute); 
		btnStartStop.setVisibility(View.VISIBLE);
	}

	public void onSelectRouteDialogNegativeClick(DialogInterface dialog) {
		dialog.dismiss();
		Logging.debug(TAG, "onDialogNegativeClick", " Cancel"); 
		currentRoute = "";
 		currentMode = TAP_MODE;
 		handler.removeCallbacks(playbackTask);
 		btnStartStop.setText("Start");
 		btnStartStop.setVisibility(View.GONE);
 		bStarted = false;
 		menuItemRecord.setVisible(true);
 		menuItemPlayback.setIcon(R.drawable.ic_action_play);
	   	marker.setPosition(currentLatLng);
	   	map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
	}

	public void onRecordRouteDialogPositiveClick(DialogInterface dialog, String routeName, int interval) {
		currentRoute = routeName;
		dialog.dismiss();
		routeList.setUpdateInterval(currentRoute, interval);
 		btnStartStop.setText("Start Recording");
 		btnStartStop.setVisibility(View.VISIBLE);
	}

	public void onRecordRouteDialogNegativeClick(DialogInterface dialog) {
		dialog.dismiss();
		menuItemRecord.setIcon(R.drawable.ic_action_record);
		menuItemPlayback.setVisible(true);
		currentMode = TAP_MODE;
		bStarted = false;
 		btnStartStop.setVisibility(View.GONE);
	}
	
    public void saveRoutes() {
    	final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    	String json = gson.toJson(routeList);
    	Logging.debug(TAG, "saveRoutes", json); 
    	IO.writeFile(getApplicationContext(), routeDir, routeFile, json, false);
    }

    public void readRoutes() {
    	if (routeList == null)
    		routeList = new Routes();

    	String r = IO.readFile(getApplicationContext(), routeDir, routeFile);
    	if (r.length() > 0) {  
    		Logging.debug(TAG, "readRoutes", r); 
    		final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    		routeList = gson.fromJson(r, Routes.class);
    	}
    }
	
    private void restoreActivityView(int mode) {
    	Logging.debug(TAG, TAG, "restoreActivityView" );
    	switch (mode) {
		 	case TAP_MODE:
		 		if (mockPermissionEnabled) {
		 			Logging.debug(TAG, "restoreActivityView", "TAP_MODE start"); 
		 			handler.postDelayed(tapTask, 1000);
		 		}
			   	break;
		
		 	case PLAYBACK_MODE:
	 			menuItemRecord.setVisible(false);
		        
		        if (bStarted == false) {
				    btnStartStop.setText("Start Playback");
		            menuItemPlayback.setIcon(R.drawable.ic_action_play);
		 		    btnStartStop.setVisibility(View.VISIBLE);
				   	marker.setPosition(currentLatLng);
					map.animateCamera(CameraUpdateFactory.newLatLng(currentLatLng));
		        }
		        else {
		            btnStartStop.setText("Stop Playback");
		            btnStartStop.setVisibility(View.VISIBLE);
		            menuItemPlayback.setIcon(R.drawable.ic_action_play_on);
		            handler.postDelayed(playbackTask,1000);   // delay a bit to let view get finished drawing?
		        }
			    break;
		
		 	case RECORDING_MODE:
	 			menuItemPlayback.setVisible(false);
	        
		        if ( bStarted == false) {
			        btnStartStop.setText("Start Recording");
			        btnStartStop.setVisibility(View.VISIBLE);
		            menuItemRecord.setIcon(R.drawable.ic_action_record);
		        }
		        else {
		            btnStartStop.setText("Stop Recording");
		            btnStartStop.setVisibility(View.VISIBLE);
		            menuItemRecord.setIcon(R.drawable.ic_action_record_on);
		            updateInterval = routeList.getUpdateInterval(currentRoute);
		        }
			   	break;
    	}
    }
}
