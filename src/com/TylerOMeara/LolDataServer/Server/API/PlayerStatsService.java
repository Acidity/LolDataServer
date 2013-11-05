package com.TylerOMeara.LolDataServer.Server.API;

import java.io.IOException;
import java.util.HashMap;

import com.TylerOMeara.LolDataServer.Server.LoadBalancer;
import com.TylerOMeara.LolDataServer.Server.Exceptions.NullClientForRegionException;
import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

public class PlayerStatsService 
{
	public static String getRecentGames(String region, int accountID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "playerStatsService", "getRecentGames", async, new Object[] {accountID});
	}
	
	public static String getPlayerStatsByAccountID(String region, int accountID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "playerStatsService", "retrievePlayerStatsByAccountId", async, new Object[]{accountID});
	}
	
	//TODO GET RANKED STATS WITH CHAMP NAMES INSTEAD OF NUMS
	//TODO Support async calls
	public static String getRankedStats(String region, int accountID, String gameMode, String season, boolean async)
	{
		HashMap<Integer, HashMap<String, Double>> champions = new HashMap<Integer, HashMap<String, Double>>();
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
			int id = client.invoke("playerStatsService", "getAggregatedStats", new Object[] {accountID,gameMode,season});
			Object[] array = (client.getResult(id).getTO("data").getTO("body").getArray("lifetimeStatistics"));
			if(array.length == 0)
			{
				return "This player has no ranked stats for the specified game mode and season";
			}
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
			
			//Handles the case where the player does not ahve ranked data for that gamemode and season
			if(json.equals("}"))
			{
				json = null;
			}
			
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
