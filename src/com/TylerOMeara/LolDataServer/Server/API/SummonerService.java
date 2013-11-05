package com.TylerOMeara.LolDataServer.Server.API;

public class SummonerService 
{
	public static String getSummonerByName(String region, String name, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getSummonerByName", async, new Object[] {name});
	}
	
	public static String getSummonersByIDs(String region, Object[] summonerIDs, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getSummonerNames", async, new Object[]{summonerIDs});
	}
	
	public static String getAllPublicSummonerDataByAccount(String region, int accountID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getAllPublicSummonerDataByAccount", async, new Object[]{accountID});
	}
	
	public static String getAllSummonerDataByAccount(String region, int accountID, boolean async)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getAllSummonerDataByAccount", async, new Object[]{accountID});
	}
}
