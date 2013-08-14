package com.TylerOMeara.LolDataServer.Server.API;

public class SummonerTeamService 
{
	public static String getPlayerRankedTeams(String region, int summonerID)
	{
		return BaseMethods.genericAPICall(region, "summonerTeamService", "findPlayer", new Object[]{summonerID});
	}
}
