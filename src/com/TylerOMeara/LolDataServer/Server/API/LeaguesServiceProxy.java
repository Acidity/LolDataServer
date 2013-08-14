package com.TylerOMeara.LolDataServer.Server.API;

public class LeaguesServiceProxy 
{
	public static String getLeagueData(String region, int summonerID, String queue)
	{
		return BaseMethods.genericAPICall(region, "leaguesServiceProxy", "getLeagueForPlayer", new Object[] {summonerID, queue});
	}
	
	public static String getAllLeaguesForPlayer(String region, int summonerID)
	{
		return BaseMethods.genericAPICall(region, "leaguesServiceProxy", "getAllLeaguesForPlayer", new Object[]{summonerID});
	}
}
