package com.TylerOMeara.LolDataServer.Server.API;

public class SummonerTeamService 
{
	public static String getPlayerRankedTeams(String region, int summonerID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerTeamService", "findPlayer", async, new Object[]{summonerID});
	}
}
