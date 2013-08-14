package com.TylerOMeara.LolDataServer.Server.API;

public class SummonerService 
{
	public static String getSummonerByName(String region, String name)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getSummonerByName", new Object[] {name});
	}
	
	public static String getSummonersByIDs(String region, Object[] summonerIDs)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getSummonerNames", new Object[]{summonerIDs});
	}
	
	public static String getAllPublicSummonerDataByAccount(String region, int accountID)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getAllPublicSummonerDataByAccount", new Object[]{accountID});
	}
	
	public static String getAllSummonerDataByAccount(String region, int accountID)
	{
		return BaseMethods.genericAPICall(region, "summonerService", "getAllSummonerDataByAccount", new Object[]{accountID});
	}
}
