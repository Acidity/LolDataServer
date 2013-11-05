package com.TylerOMeara.LolDataServer.Server.API;

public class GameService 
{
	public static String getInGameProgressInfo(String region, String name, boolean async)
	{
		return BaseMethods.genericAPICall(region, "gameService", "retrieveInProgressSpectatorGameInfo", async, new Object[] {name});
	}
}
