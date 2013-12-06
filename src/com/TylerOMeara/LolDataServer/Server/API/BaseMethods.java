/*
 *  LolDataServer
 *  Copyright (C) 2013 Tyler O'Meara
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.TylerOMeara.LolDataServer.Server.API;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.TylerOMeara.LolDataServer.Server.LoadBalancer;
import com.TylerOMeara.LolDataServer.Server.Exceptions.NullClientForRegionException;
import com.gvaneyck.rtmp.Callback;
import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

/**
 * Base API methods. Intended only to decrease amount of code and to allow for adding new methods easier, none of these should be available
 * outside of the server.
 * @author Tyler O'Meara
 *
 */
public class BaseMethods 
{
	//Holds the results of async calls
	private static ConcurrentHashMap<Integer, String> asyncResults = new ConcurrentHashMap<Integer,String>();
	private static int ids = 0;
	
	@Deprecated
	public static String manualRequest(String line)
	{
		//Region_Service:operation-param1,param2,...
		String region = line.substring(0,line.indexOf("_"));
		String service = line.substring(line.indexOf("_")+1,line.indexOf(":"));
		String operation = line.substring(line.indexOf(":")+1,line.indexOf("-"));
		String temp = line.substring(line.indexOf("-")+1);
		String[] params = temp.split(",");
		Object[] obj;
		if(line.indexOf("-") != line.length()-1)
		{
			obj = new Object[params.length];
			for(int x = 0; x < params.length; x++)
			{
				obj[x] = params[x];
			}
		}
		else
		{
			obj = new Object[0];
		}
		LoLRTMPSClient client;
		try
		{
			client = LoadBalancer.returnClient(region);
		} 
		catch (NullClientForRegionException e1) 
		{
			return "Connection to " + e1.getRegion() + " failed. This may be because that region does not exist, or the administrator of this server " +
					" does not have it configured to that region, or because that region is currently offline.";
		}
		try 
		{
			int id = client.invoke(service, operation, obj);
			return String.valueOf(client.getResult(id));
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Makes a call to a service on Riot's servers and formats it to JSON.
	 * @param region Region corresponding to the server from which the data should be obtained
	 * @param service Service from which the data should be retrieved.
	 * @param operation Operation to be executed
	 * @param async true if the operation should be executed asynchronously, false otherwise
	 * @param args Holds the arguments to be sent to Riot's servers
	 * @return JSON string containing the data
	 */
	public static String genericAPICall(String region, String service, String operation, boolean async, Object[] args)
	{
		if(!async)
			return BaseMethods.genericSyncAPICall(region, service, operation, args);
		int id = BaseMethods.genericAsyncAPICall(region, service, operation, args);
		return BaseMethods.returnAsyncAPICallResult(id, true);
	}

	/**
	 * Makes a synchronous call to a service on Riot's servers and formats it to JSON.
	 * @param region Region corresponding to the server from which the data should be obtained
	 * @param service Service from which the data should be retrieved.
	 * @param operation Operation to be executed
	 * @param args Holds the arguments to be sent to Riot's servers
	 * @return JSON string containing the data
	 */
	public static String genericSyncAPICall(String region, String service, String operation, Object[] args)
	{
		//Retrieve a PvP.net client from the region.
		LoLRTMPSClient client;
		try
		{
			client = LoadBalancer.returnClient(region);
		} 
		catch (NullClientForRegionException e1) 
		{
			return "Connection to " + e1.getRegion() + " failed. This may be because that region does not exist, or the administrator of this server " +
					" does not have it configured to that region, or because that region is currently offline.";
		}
		//Retrieves the data and formats it into JSON
		try 
		{
			//JSON must start with a {
			String json = "{";
			
			//Retrieve the data from Riot
			int id = client.invoke(service, operation, args);
			TypedObject data = client.getResult(id);
			
			//Iterate through the data and add it to the JSON string
			for(String x : data.keySet())
			{
				json = addObject(json, data, x);
			}
			
			//Remove the last character from the string, would be an improper ,
			json = json.substring(0,json.length()-1);
			json += "}";
			return json;
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Add an async result String to the storage hashmap
	 * @param id ID to use as the key
	 * @param json String to use as the value
	 */
	private static void addResult(int id, String json)
	{
		asyncResults.put(id, json);
	}
	
	/**
	 * Returns a stored async result String
	 * @param id ID of the result
	 * @param remove true if you want to remove the result after retrieving it
	 * @return Stored result String
	 */
	public static String returnAsyncAPICallResult(int id, boolean remove)
	{
		if(remove)
			return asyncResults.remove(id);
		return asyncResults.get(id);
	}
	
	/**
	 * Makes an asynchronous call to a service on Riot's servers and formats it to JSON.
	 * @param region Region corresponding to the server from which the data should be obtained
	 * @param service Service from which the data should be retrieved.
	 * @param operation Operation to be executed
	 * @param args Holds the arguments to be sent to Riot's servers
	 * @return JSON string containing the data
	 */
	
	public static int genericAsyncAPICall(String region, String service, String operation, Object[] args)
	{
		//Retrieve client for the specified region
		LoLRTMPSClient client = null;
		//Set the ID to be assigned to the result value
		final int localID = ids++;
		try
		{
			client = LoadBalancer.returnClient(region);
		} 
		catch (NullClientForRegionException e1) 
		{
			addResult(localID, "Connection to " + e1.getRegion() + " failed. This may be because that region does not exist, or the administrator of this server " +
					" does not have it configured to that region, or because that region is currently offline.");
			return(localID);
		}
		//Retrieves the data from Riot's servers async then formats it into json during the callback and adds it to the async result hashmap
		try 
		{
			int id = client.invokeWithCallback(service, operation, args,
			new Callback()
			{
				public void callback(TypedObject result)
				{
					String json;
					json = "{";
					for(String x : result.keySet())
					{
						json = addObject(json, result, x);
					}
					json = json.substring(0,json.length()-1);
					json += "}";
					addResult(localID, json);
				}
			});
			client.join();
			//Wait for result to be obtained and added to hashmap
			while(returnAsyncAPICallResult(localID, false) == null)
			{
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return localID;
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Returns the output from Riot without modification
	 * @param region Region corresponding to the server from which the data should be obtained
	 * @param service Service from which the data should be retrieved.
	 * @param operation Operation to be executed
	 * @param args Holds the arguments to be sent to Riot's servers
	 * @return String containing the data
	 */
	public static String genericRawOutput(String region, String service, String operation, Object[] args)
	{
		LoLRTMPSClient client;
		try
		{
			client = LoadBalancer.returnClient(region);
		} 
		catch (NullClientForRegionException e1) 
		{
			return "Connection to " + e1.getRegion() + " failed. This may be because that region does not exist, or the administrator of this server " +
					" does not have it configured to that region, or because that region is currently offline.";
		}
		try 
		{
			int id = client.invoke(service, operation, args);
			return String.valueOf(client.getResult(id));
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static String addObject(String json, TypedObject data, String x)
	{
		json += "\"" + x + "\":";
		if(data.get(x) == null)
		{
			json += "\"null\","; 
		}
		if(data.get(x) instanceof Integer)
		{
			json += data.getInt(x) + ",";
			return json;
		}
		if(data.get(x) instanceof Double)
		{
			if(doubleIsInteger(data.getDouble(x)))
			{
				json += data.getInt(x) + ",";
			}
			else
			{
				json += data.getDouble(x) + ",";
			}
			return json;
		}
		if(data.get(x) instanceof Boolean)
		{
			json += data.getBool(x) + ",";
			return json;
		}
		if(data.get(x) instanceof String)
		{
			json += "\"" + data.getString(x) + "\",";
			return json;
		}
		if(data.get(x) instanceof Date)
		{
			json += "\"" + data.getDate(x) + "\",";
			return json;
		}
		if(data.get(x) instanceof Byte)
		{
			json += Integer.valueOf(String.valueOf(data.get(x))) + ",";
			return json;
		}
		//Need to check if it is an array manually because data.get(x) returns a TypedObject even when it is an Array
		if(!isArray(data, x) && data.get(x) instanceof TypedObject)
		{
			json += "{";
			for(String s : data.getTO(x).keySet())
			{
				json = addObject(json, data.getTO(x), s);
			}
			if(data.getTO(x).size() > 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "},";
			return json;
		}
		if(data.get(x) instanceof byte[])
		{
			byte[] array = (byte[]) data.get(x);
			json += "[";
			for(Object o : array)
			{
				json = addArrayObject(json, o);
			}
			if(array.length != 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "],";
			return json;
		}
		if(data.get(x) instanceof Object[] || isArray(data, x))
		{
			Object[] array = data.getArray(x);
			json += "[";
			for(Object o : array)
			{
				json = addArrayObject(json, o);
			}
			if(array.length != 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "],";
			return json;
		}
		
		return json;
	}
	
	private static String addArrayObject(String json, Object o)
	{
		if(o == null)
		{
			json += "\"null\","; 
		}
		if(o instanceof Integer)
		{
			json += o + ",";
			return json;
		}
		if(o instanceof Double)
		{
			if(doubleIsInteger((double)o))
			{
				json += (int)o + ",";
			}
			else
			{
				json += o + ",";
			}
			return json;
		}
		if(o instanceof Boolean)
		{
			json += o + ",";
			return json;
		}
		if(o instanceof String)
		{
			json += "\"" + o + "\",";
			return json;
		}
		if(o instanceof Date)
		{
			json += "\"" + o + "\",";
			return json;
		}
		if(o instanceof Byte)
		{
			json += Integer.valueOf(String.valueOf(o)) + ",";
			return json;
		}
		if(o instanceof TypedObject)
		{
			json += "{";
			for(String s : ((TypedObject)o).keySet())
			{
				json = addObject(json, ((TypedObject)o), s);
			}
			if(((TypedObject)o).size() > 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "},";
			return json;
		}
		if(o instanceof byte[])
		{
			byte[] array = (byte[]) o;
			json += "[";
			for(Object j : array)
			{
				json = addArrayObject(json, j);
			}
			if(array.length != 0)
			{
				json = json.substring(0,json.length()-1);
			}
			json += "],";
			return json;
		}
		if(o instanceof Object[])
		{
			Object[] array = (Object[])o;
			json += "[";
			for(Object j : array)
			{
				json = addArrayObject(json, j);
			}
			json += "],";
			return json;
		}
		
		return json;
	}
	
	private static boolean isArray(TypedObject data, String x)
	{
		try{
			if(data.getArray(x) == null)
			{
				return false;
			}
		}
		catch(Exception e)
		{
			return false;
		}
		return true;
	}
	
	private static boolean doubleIsInteger(double d)
	{
		if((int)d == d)
		{
			return true;
		}
		return false;
	}
}
