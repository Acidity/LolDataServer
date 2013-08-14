package com.TylerOMeara.LolDataServer.Server.API;

public class GameService 
{
	public static String getInGameProgressInfo(String region, String name)
	{
		return BaseMethods.genericAPICall(region, "gameService", "retrieveInProgressSpectatorGameInfo", new Object[] {name});
	}
}
