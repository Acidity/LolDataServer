package com.TylerOMeara.LolDataServer.Server.API;

import java.io.IOException;
import java.util.Date;

import com.TylerOMeara.LolDataServer.Server.LoadBalancer;
import com.TylerOMeara.LolDataServer.Server.Exceptions.NullClientForRegionException;
import com.gvaneyck.rtmp.Callback;
import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

class BaseMethods 
{
	@Deprecated
	public static String manualRequest(String line)
	{
		//Region_Service:operation-param1,param2,...
		String region = line.substring(0,line.indexOf("_"));
		String service = line.substring(line.indexOf("_")+1,line.indexOf(":"));
		String operation = line.substring(line.indexOf(":")+1,line.indexOf("-"));
		String temp = line.substring(line.indexOf("-")+1);
		String[] params = temp.split(",");
		Object[] obj = new Object[params.length];
		for(int x = 0; x < params.length; x++)
		{
			obj[x] = params[x];
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

	public static String genericAPICall(String region, String service, String operation, Object[] args)
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
			String json = "{";
			int id = client.invoke(service, operation, args);
			TypedObject data = client.getResult(id);
			for(String x : data.keySet())
			{
				json = addObject(json, data, x);
			}
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
