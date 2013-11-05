package com.TylerOMeara.LolDataServer.Server.API;

public class LeaguesServiceProxy 
{
	public static String getLeagueForPlayer(String region, int summonerID, String queue, boolean async)
	{
		return BaseMethods.genericAPICall(region, "leaguesServiceProxy", "getLeagueForPlayer", async, new Object[] {summonerID, queue});
	}
	
	public static String getAllLeaguesForPlayer(String region, int summonerID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "leaguesServiceProxy", "getAllLeaguesForPlayer", async, new Object[]{summonerID});
	}
}
