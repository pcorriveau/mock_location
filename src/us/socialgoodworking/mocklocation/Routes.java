package us.socialgoodworking.mocklocation;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.model.LatLng;

/**
 * <p>Class for storing saved routes.</p>
 * 
 * @author Paul Corriveau  <a href="mailto:paul@socialgoodworking.us?subject=MockLocation">paul@socialgoodworking.us</a>
 * @version 1.0.0
 *
 */
public class Routes {
	
	// A route has a unique name (stored in hashmap), a list of points, and an update interval. The interval determines how frequently
	// the location is updated during play back.
	private class Route {
		long updateInterval;

		// Always starts at 0, then increments to list.size() at which point it just wraps around back to 0.
		private transient int index; 

		ArrayList<LatLng> points = new ArrayList<LatLng>();
	}
	
	public HashMap<String, Route> routes;
	
	public Routes() {
		routes = new HashMap<String, Route>();
	}
	
	/**
	 * 
	 * @param routeName The name of the route
	 * @param interval	How often location is updated during play back, in seconds. Minimum 1 second interval.
	 */
	public void setUpdateInterval(String routeName, long interval) {
		if ( routes.containsKey(routeName))
		{
			if ( interval < 1 )
				routes.get(routeName).updateInterval = 1000;
			else
				routes.get(routeName).updateInterval = interval * 1000;
		}
		else {
			Route r = new Route();
			r.index = 0;
			r.updateInterval = interval * 1000;
			routes.put(routeName, r);
		}
	}
	
	/**
	 * 	
	 * @param routeName
	 * @return
	 * 
	 * @throws IllegalArgumentException if route doesn't exist.
	 */
	public long getUpdateInterval(String routeName) {
		if ( !routes.containsKey(routeName)) {
			throw new IllegalArgumentException("Route " + routeName + " does not exist.");
		}
		
		return routes.get(routeName).updateInterval;
	}
	
	/**
	 * Adds a LatLng to the route identified by routeName. If the route
	 * doesn't exist, the point gets added to a newly created route.
	 * Note that there's no check for duplicate points within a route.
	 * 
	 * @param routeName
	 * @param lat latitude
	 * @param lng longitude
	 */
	public void addPoint(String routeName, double lat, double lng) {
		Route r = null;
		LatLng ll = new LatLng (lat, lng);
		if ( routes.containsKey(routeName))
			r = routes.get(routeName);
		else {
			r = new Route();
			routes.put(routeName, r);
		}

		r.points.add(ll);
	}
	
	/**
	 * Adds a LatLng to the route identified by routeName. If the route
	 * doesn't exist, the point gets added to a newly created route.
	 * Note that there's no check for duplicate points within a route.
	 * 
	 * @param routeName
	 * @param point a LatLng object
	 */
	public void addPoint(String routeName, LatLng point) {
		Route r = null;
		if ( routes.containsKey(routeName))
			r = routes.get(routeName);
		else {
			r = new Route();
			routes.put(routeName, r);
		}

		r.points.add(point);
	}
	
	/**
	 * 
	 * @param routeName
	 * 	
	 * @throws IllegalArgumentException if route doesn't exist.
	 */
	public void deleteRoute(String routeName) {
		if ( !routes.containsKey(routeName)) {
			throw new IllegalArgumentException(routeName + " does not exist.");
		}

		Route r = routes.get(routeName);
		r.points.clear();
		routes.remove(routeName);
	}
	
	/**
	 * 
	 * @param routeName
	 * @return An arrayList of LatLng if routeName exists, an empty ArrayList otherwise.
	 */
	public ArrayList<LatLng> getRoute(String routeName) {
		if ( routes.containsKey(routeName)) {
			return routes.get(routeName).points;
		}
		
		return new ArrayList<LatLng>();
	}

	/**
	 * 
	 * @return
	 */
	public String [] getRoutes() {
		return routes.keySet().toArray(new String[routes.size()]);
	}
	
	
	/**
	 * Gets the current location in a route (points[index]).
	 *  
	 * @param routeName
	 * @return
	 *
	 * @throws IllegalArgumentException if route doesn't exist.
	 */
	public LatLng getCurrent(String routeName) {
		if ( !routes.containsKey(routeName)) {
			throw new IllegalArgumentException(routeName + " does not exist.");
		}
		
		Route r = routes.get(routeName);
		return r.points.get(r.index);
	}
	
	/**
	 * Gets the location following the current location (points[index + 1]). If index == the size of the points list, getNext 
	 * returns the location at index == 0, i.e. it just wraps around back to 0.
	 * 
	 * @param routeName
	 * @return
	 * 
	 * @throws IllegalArgumentException if route doesn't exist.
	 */
	public LatLng getNext(String routeName) {
		if ( !routes.containsKey(routeName)) {
			throw new IllegalArgumentException(routeName + " does not exist.");
		}
		
		Route r = routes.get(routeName);
		r.index++;
		
		if (r.index == r.points.size())
			r.index = 0;
		
		return r.points.get(r.index);
	}
	
	public int getCurrentIndex(String routeName) {	
		if ( !routes.containsKey(routeName)) {
			throw new IllegalArgumentException(routeName + " does not exist.");
		}
		
		Route r = routes.get(routeName);
		return r.index;
	}
	
	public void setCurrentIndex(String routeName, int index) {	
		if ( !routes.containsKey(routeName)) {
			throw new IllegalArgumentException(routeName + " does not exist.");
		}
		
		Route r = routes.get(routeName);
		
		if (index < 0 || index >= r.points.size()) {
			r.index = 0;
		}
		else {
			r.index = index;
		}
	}

}
