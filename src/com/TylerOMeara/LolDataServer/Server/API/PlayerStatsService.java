package com.TylerOMeara.LolDataServer.Server.API;

import java.io.IOException;
import java.util.HashMap;

import com.TylerOMeara.LolDataServer.Server.LoadBalancer;
import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

public class PlayerStatsService 
{
	public static String getRecentMatchHistory(String region, int accountID)
	{
		return BaseMethods.genericAPICall(region, "playerStatsService", "getRecentGames", new Object[] {accountID});
	}
	
	public static String getPlayerStatsByAccount(String region, int accountID)
	{
		return BaseMethods.genericAPICall(region, "playerStatsService", "retrievePlayerStatsByAccountId", new Object[]{accountID});
	}
	
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