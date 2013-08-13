package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

public class API 
{
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
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
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
	
	public static String getSummonerByName(String region, String name)
	{
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke("summonerService", "getSummonerByName", new Object[] {name});
			TypedObject data = client.getResult(id).getTO("data").getTO("body");
			String json = "{\"Name\":\"" + data.getString("name"); //Adds name to JSON
			json += "\", \"InternalName\":\"" + data.getString("internalName") + "\","; //Adds internalName to JSON
			json += "\"DataVersion\":" + data.getInt("dataVersion") + ",";
			json += "\"AccountID\":" + data.getDouble("acctId") + ",";
			json += "\"ProfileIconID\":" + data.getInt("profileIconId") + ",";
			json += "\"RevisionDate\":\"" + data.getDate("revisionDate").toString() + "\",";
			json += "\"RevisionID\":" + data.getDouble("revisionId") + ",";
			json += "\"FutureData\":\"" + data.getString("futureData") + "\",";
			json += "\"SummonerID\":" + data.getDouble("SummonerID") + ",";
			json += "\"SummonerLevel\":" + data.getInt("summonerLevel") + "}";
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
	 * playerStatsService
	 * getAggregatedStats
	 * @return
	 */
	//TODO Check for null
	public static String getRankedStats(String region, int acctID, String gameMode, String season)
	{
		HashMap<Integer, HashMap<String, Double>> champions = new HashMap<Integer, HashMap<String, Double>>();
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke("playerStatsService", "getAggregatedStats", new Object[] {acctID,gameMode,season});
			Object[] array = (client.getResult(id).getTO("data").getTO("body").getArray("lifetimeStatistics"));
			for(Object x : array)
			{
				if(x instanceof TypedObject)
				{
					TypedObject y = (TypedObject) x;
					//Data for champion exists
					if(champions.containsKey(y.getInt("championId")))
					{
						champions.get(y.getInt("championId")).put(y.getString("statType"), y.getDouble("value"));
					}
					else //Data for champion doesn't exist
					{
						HashMap<String, Double> champion = new HashMap<String, Double>();
						champion.put(y.getString("statType"), y.getDouble("value"));
						champions.put(y.getInt("championId"), champion);
					}
				}
			}
			//Generates the JSON from collected data
			String json = "{";
			for(int x : champions.keySet())
			{
				json += ("\"" + x + "\"" + ":{");
				for(String s : champions.get(x).keySet())
				{
					json += ("\"" + s + "\":" + champions.get(x).get(s) + ",");
				}
				json = json.substring(0,json.length()-1);
				json += "},";
			}
			json = json.substring(0,json.length()-1);
			json += "}";
			return json;
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
