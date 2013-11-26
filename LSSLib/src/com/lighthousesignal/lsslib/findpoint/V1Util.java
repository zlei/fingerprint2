package com.lighthousesignal.lsslib.findpoint;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.entity.InputStreamEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.lighthousesignal.lsslib.ApiUtil;

import com.lighthousesignal.lsslib.Fence;
import com.lighthousesignal.lsslib.Floor;
import com.lighthousesignal.lsslib.NetworkListener;
import com.lighthousesignal.lsslib.NetworkTask;
import com.lighthousesignal.lsslib.Offer;
import com.lighthousesignal.lsslib.WifiScanResult;
import com.lighthousesignal.lsslib.XMLMaker;
import com.lighthousesignal.lsslib.pathfinding.MapNode;
import com.lighthousesignal.lsslib.pathfinding.Path;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;

public class V1Util implements ApiUtil{
	
	private int queryId = 0;

	public NetworkTask findPointTask(NetworkListener listener, String token, Vector<WifiScanResult> res, int count,	int time, int floorId, Context context) {

		XMLMaker logWritter = new XMLMaker();
		logWritter.addScanParams(time, count);
		logWritter.addScanStatistics(res, count);
		
		long unixTime = System.currentTimeMillis();
		logWritter.addCaptureTime(unixTime);
		
		if(LSSService.ADD_DEVICE_INFO)logWritter.addDeviceInfo(context);
		if(LSSService.ADD_USER_INFO){
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String customer_id = prefs.getString("customer_id", "");
			String developer_id = prefs.getString("developer_id", "");
			logWritter.addUserInfo(context, customer_id, developer_id);
		}
		if(LSSService.USE_GPS_DATA){
			logWritter.addLocation(LSSService.getLastGpsLocation());
		}
		logWritter.endLog();
		
		Hashtable<String, String> paramHash = new Hashtable<String,String>(4);
		paramHash.put("logdata", logWritter.getString());
		paramHash.put("scanname", "logfile");
		paramHash.put("token", token);
		
		System.out.println("Log text was: \n" + logWritter.getString());
		
		if(LSSService.selected_floor != -1 ){
			System.out.println("putting floor id " + LSSService.selected_floor);
			paramHash.put("floorId", "" + LSSService.selected_floor);
		}else if(LSSService.currentFloorId > 0) {
			System.out.println("putting floor id " + LSSService.currentFloorId);
			paramHash.put("floorId", "" + LSSService.currentFloorId);
		}
		
		String post = makePost(paramHash,false, false);
		
		NetworkTask task = new NetworkTask(listener, NetworkListener.FIND_POINT, "/logs/pars/findpoint/", false, false, post, ++queryId);  
		
		return task;
	}
	
	public void processFindPointResponse(LSSService service, byte[] response, int qid){
		Log.d("LSS V1Util", "Processing find point response");
		try {
			SparseArray<Offer> offers = new SparseArray<Offer>();
			ArrayList<Fence> fences = new ArrayList<Fence>();
			
			//System.out.println("qid: " + qid + " queryId: " + queryId);
			System.out.println("Find point response: \n\n" + new String(response));
			
			//Make sure that this is the latest request
			if(qid == queryId) {
			
		        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		        factory.setNamespaceAware(true);
		        XmlPullParser xpp = factory.newPullParser();
		        xpp.setInput(new ByteArrayInputStream(response), "UTF-8");
		        
		        int eventType = xpp.getEventType();
		        
		        while(eventType != XmlPullParser.END_DOCUMENT){
		        	if(eventType == XmlPullParser.START_DOCUMENT){
		        	}else if(eventType == XmlPullParser.END_DOCUMENT){
		        	}else if(eventType == XmlPullParser.START_TAG){
		        		
		        		/*
		        		 * Parse any error tag
		        		 */
		        		if(xpp.getName().equalsIgnoreCase("error")){
		        			xpp.next();
		        			//System.out.println("Error Returned: " + xpp.getText());
		        			service.getListener().onStatusChanged(LSSListener.MSG_NO_FIX_AVAIL);
		        			eventType = xpp.next();
		        			continue;
		        		}
		        		
		        		/*
		        		 * Parse a location point
		        		 */
		        		if(xpp.getName().equalsIgnoreCase("point")){
		        			float x = Float.parseFloat(xpp.getAttributeValue(null, "x"));
		        			float y = Float.parseFloat(xpp.getAttributeValue(null, "y"));
		        			float e = Float.parseFloat(xpp.getAttributeValue(null, "error"));
		        			String imgPath = "/plans/" + xpp.getAttributeValue(null, "img");
		        			String queryId = xpp.getAttributeValue(null, "queryLog");
		        			int floorId = Integer.parseInt(xpp.getAttributeValue(null, "floorId"));
	
		        			/*
		        			 * Create a new location and update the listener
		        			 */
		        			Location loc = new Location("Lighthouse");
							loc.setLatitude(x);
							loc.setLongitude(y);
							loc.setAccuracy(e);
							if(service.getListener() != null){
								((LSSListener)service.getListener()).onLocationChanged(loc);
								service.getListener().onStatusChanged(LSSListener.MSG_NEW_POSITION);
							}
		        			
							/*
							 * Add the new floor and download the image if applicable
							 */
							Floor f = new Floor(floorId);
							f.setImagePath(imgPath);
							f.setName("Default Floor");
							
							LSSService.currentFloorId = floorId;
							
		        			if(service.getFloors().size() == 0) {
								SparseArray<Floor> newfloors = new SparseArray<Floor>();
								newfloors.put(floorId, f);
								service.setFloors(newfloors);
								service.currentFloorId = floorId;
								service.downloadMapImage();
							}else{
								if(service.getFloors().get(LSSService.currentFloorId).getImagePath() == null){
									service.getFloors().get(LSSService.currentFloorId).setImagePath(imgPath);
								}else if(service.getFloors().get(LSSService.currentFloorId).getMapImage() == null){
									service.downloadMapImage();
								}else{
									//System.out.println("No images need to be downloaded.");
								}
							}
		        			
		        			eventType = xpp.next();
		        			continue;
		        		}
		        		
		        		/*
		        		 * Parse infoData
		        		 */
		        		if(xpp.getName().equalsIgnoreCase("infoData")) {
		        			while(true) {
		        				int type = xpp.next();
		        				if(type == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("infoData"))break;
		        				if(type == XmlPullParser.START_TAG){
		        					if(xpp.getName().equalsIgnoreCase("floor")){
		        						xpp.next();
		        						service.getFloors().get(service.currentFloorId).setName(xpp.getText());
		        					}else if(xpp.getName().equalsIgnoreCase("building")){
		        						xpp.next();
		        						service.getFloors().get(service.currentFloorId).setBuildingName(xpp.getText());
		        					}
		        				}
		        			}
		        			eventType = xpp.next();
		        			continue;
		        		}
		        		
		        		/*
		        		 * Parse offers
		        		 */
		        		if(xpp.getName().equalsIgnoreCase("offers")){
		        			xpp.next();
		       
		        			//until </offers>
		        			while(!xpp.getName().equalsIgnoreCase("offers")){
	
			        			if(xpp.getName().equalsIgnoreCase("offer")){
			        						        				
			        				int off_id = Integer.parseInt(xpp.getAttributeValue(null, "offer_id"));
			        				String ret_id = xpp.getAttributeValue(null, "retailer_id");
			        				String title = xpp.getAttributeValue(null, "title");
			        				xpp.next();
			        				String description = xpp.getText();
	
			        				Offer o = new Offer();
			        				o.setOfferId(off_id);
			        				o.setRetailerId(ret_id);
			        				o.setTitle(title);
			        				o.setDescription(description);
		        					offers.put(off_id, o);
			        				
			        				xpp.next();//</offer>
			        				xpp.next();//either <offer> or something else
			        			}
		        			}
		        			
		        			if(offers.size() > 0){
		        				service.getListener().onStatusChanged(LSSListener.MSG_OFFERS_AVAIL);
		        			}
		        			
		        			service.setOffers(offers);
		        			
		        			eventType = xpp.next();
		        			continue;
		        		}
		        		
		        		/*
		        		 * Parse fences
		        		 */
		        		if(xpp.getName().equalsIgnoreCase("fences")){
		        			xpp.next();
		        			
		        			fences = new ArrayList<Fence>();
		        			
		        			//until </fences>
		        			while(!xpp.getName().equalsIgnoreCase("fences")){
		        				
		        				if(xpp.getName().equalsIgnoreCase("fence")){
		        					
		        					String geo_id = xpp.getAttributeValue(null, "geofence_id");
		        					String prop_id = xpp.getAttributeValue(null, "property_id");
		        					String retail_id = xpp.getAttributeValue(null, "retailer_id");
		        					
		        					Fence fence = new Fence();
		        					fence.setGeofenceId(geo_id);
		        					fence.setPropertyId(prop_id);
		        					fence.setRetailerId(retail_id);
		        					fences.add(fence);
		        					
		        					xpp.next();//</fence>
		        					xpp.next();//<fence> or other
		        				}
		        			}
		        			
		        			if(fences.size() > 0){
		        				service.getListener().onStatusChanged(LSSListener.MSG_FENCES_AVAIL);
		        			}
		        			
		        			eventType = xpp.next();
		        			continue;
		        		}
		        		
		        		/*
		        		 * Parse floors (for pre root)
		        		 */
		        		if(xpp.getName().equalsIgnoreCase("floors")){
		        			xpp.next();
		        			
		        			SparseArray<Floor> floors;
		        			if(service.getFloors().size() == 0) {
		        				floors = new SparseArray<Floor>();
		        			}else{
		        				floors = service.getFloors();
		        			}
		        				
		        			//Until </floors>
		        			while(!xpp.getName().equalsIgnoreCase("floors")){
		        				if(xpp.getEventType() == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("floor")){
		        					int f_id = Integer.parseInt(xpp.getAttributeValue(null, "floor_id"));
		        					String name = xpp.getAttributeValue(null, "name");
		        					Floor f = new Floor(f_id, name);
		        					floors.put(f_id, f);
		        				}
		        				
		        				//TODO check
		        				xpp.next();
		        				xpp.next();
		        			}
		        			
		        			service.setFloors(floors);
		        			
		        			//If the floor is currently 0, set to the first floor returned until the third party changes floors.
							if(service.currentFloorId <= 0 && floors.size() > 0){
								service.currentFloorId = floors.valueAt(0).getId();
							}
							
							service.downloadMapImage();
							
							//Now will provide floor id, run again to obtain root response
							service.findPoint();
		        			
		        			eventType = xpp.next();
		        			continue;
		        		}
		        		
		        	}else if(eventType == XmlPullParser.END_TAG){
		        	}else if(eventType == XmlPullParser.TEXT){
		        	}
		        	
		        	eventType = xpp.next();
		        }
		        
		        service.setOffers(offers);
		        service.setFences(fences);
			}
		        
	    } catch (XmlPullParserException e) {
		} catch (IOException e1) {
		} catch (Exception e){
		}
	}
	
	public NetworkTask loginTask(NetworkListener listener, Hashtable<String,String> credentials){
		String post = makePost(credentials,false, true);
		return new NetworkTask(listener, NetworkListener.LOGIN, "/user/index/login/", false, true, post, 0);
	}
	
	public NetworkTask logoutTask(NetworkListener listener, Hashtable<String,String> tokenHash){
		String post = makePost(tokenHash, false, true);
		return new NetworkTask(listener, NetworkListener.LOGOUT, "/user/index/logout/", false, true, post, 0);
	}
	
	public String makePost(Hashtable<String, String> params, boolean isGet, boolean isPostUrlEncoded){
		//ifposturlencode
		String result = "";
		if(isGet){
			StringBuffer postBuffer = new StringBuffer(params.size() * 20);
			boolean isFirst = true;
			for (Enumeration<String> it = params.keys(); it.hasMoreElements();) {
				String key = it.nextElement();
				String value = URLEncoder.encode(params.get(key));
				postBuffer.append(isFirst? '?' : '&').append(key).append('=').append(value);
				isFirst = false;
			}
			result = postBuffer.toString();
		}else if (isPostUrlEncoded){
			boolean isFirst = true;
			StringBuffer postBuffer = new StringBuffer(params.size() * 20);
			for (Enumeration<String> it = params.keys(); it.hasMoreElements();) {
				String key = it.nextElement();
				String value = URLEncoder.encode(params.get(key));
				if (!isFirst)
					postBuffer.append('&');
				postBuffer.append(key).append('=').append(value);
				isFirst = false;
			}
			result = postBuffer.toString();
		}else{
			StringBuffer postBuffer = new StringBuffer(params.size() * 40);
			for (Enumeration<String> it = params.keys(); it.hasMoreElements();) {
				String key = it.nextElement();
				String value = params.get(key);
				postBuffer.append("\r\n--" + NetworkTask.BOUNDARY + "\r\n");
				postBuffer.append("Content-Disposition: form-data; name=\""+ key +"\"\r\n\r\n");
				postBuffer.append(value);
			}
			result = postBuffer.toString();
		}
		//endif
		
		return result;
	}

	public NetworkTask downloadMapTask(NetworkListener listener, String token, String path, int floorId) {
		NetworkTask task = new NetworkTask(listener, NetworkListener.GET_IMAGE, path, true, false, "", floorId);
		return task;
	}

	
	public NetworkTask sendFeedbackTask(NetworkListener listener,
			Hashtable<String, String> feedback) {
		String post = makePost(feedback, false, false);
		NetworkTask task = new NetworkTask(listener, NetworkListener.SEND_FEEDBACK, "/logs/pars/moderate/", false, false, post, 0);
		
		return task;
	}

	public NetworkTask findPathTask(NetworkListener listener, int startNodeId, int endNodeId) {
		String path = "/paths/findpath";
		String params = "?start_map_node_id=" + startNodeId + "&end_map_node_id=" + endNodeId;
		NetworkTask task = new NetworkTask(listener, NetworkListener.FIND_PATH, path, true, false, params, 0);
		return task;
	}

	public Path processFindPathResponse(byte[] data) {
		Log.d("LSS V1Util", "Processing find path response");
		Path p = new Path();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Document doc = dBuilder.parse(bais);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("node");

			for (int i = 0; i < nList.getLength(); ++i) {
				Node n = nList.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					Element e = (Element) n;
					double px = Double.parseDouble(e.getAttribute("x"));
					double py = Double.parseDouble(e.getAttribute("y"));
					int id = Integer.parseInt(e.getAttribute("id"));
					p.addNode(id, px, py);
				}
			}
		} catch (Exception e) {
			Log.d("V1Util", "Error parsing find path response");
			e.printStackTrace();
		}

		return p;
	}
	
	public NetworkTask findNearestNodeTask(NetworkListener listener, int floorId, double pointX, double pointY, int queryId) {
		String path = "/paths/nearest_node";
		String params = "?floor_id=" + floorId + "&point_x=" + pointX + "&point_y=" + pointY;
		NetworkTask task = new NetworkTask(listener, NetworkListener.NEAREST_NODE, path, true, false, params, queryId);
		return task;	
	}

	public MapNode processFindNearestNodeResponse(byte[] data) {
		Log.d("LSS V1Util", "Processing find nearest node response");
		MapNode node = new MapNode();

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			Document doc = dBuilder.parse(bais);
			doc.getDocumentElement().normalize();
			Node n = doc.getElementsByTagName("map-node").item(0);

			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				node.id = Integer.parseInt(e.getElementsByTagName("map-node-id").item(0).getTextContent());
				node.px = Double.parseDouble(e.getElementsByTagName("point-x").item(0).getTextContent());
				node.py = Double.parseDouble(e.getElementsByTagName("point-y").item(0).getTextContent());
			}
		} catch (Exception e) {
			Log.d("V1Util", "Error parsing nearest node response");
			e.printStackTrace();
		}
		
		return node;
	}

}
