package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.util.HashMap;

import com.gvaneyck.rtmp.LoLRTMPSClient;
import com.gvaneyck.rtmp.TypedObject;

public class API 
{
	//TODO HANDLE ERRORS!
	public static String manualRequest(String line)
	{
		//Region_Service:operation-param1,param2,...
		String region = line.substring(0,line.indexOf("_"));
		String service = line.substring(line.indexOf("_")+1,line.indexOf(":"));
		String operation = line.substring(line.indexOf(":")+1,line.indexOf("-"));
		String temp = line.substring(line.indexOf("-")+1);
		String[] params = temp.split(",");
		Object[] obj = new Object[params.length];
		for(int x = 0; x < params.length; x++)
		{
			obj[x] = params[x];
		}
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke(service, operation, obj);
			return String.valueOf(client.getResult(id));
		} 
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public static String getSummonerByName(String region, String name)
	{
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke("summonerService", "getSummonerByName", new Object[] {name});
			TypedObject data = client.getResult(id).getTO("data").getTO("body");
			String json = "{\"Name\":\"" + data.getString("name"); //Adds name to JSON
			json += "\", \"InternalName\":\"" + data.getString("internalName") + "\","; //Adds internalName to JSON
			json += "\"DataVersion\":" + data.getInt("dataVersion") + ",";
			json += "\"AccountID\":" + data.getDouble("acctId") + ",";
			json += "\"ProfileIconID\":" + data.getInt("profileIconId") + ",";
			json += "\"RevisionDate\":\"" + data.getDate("revisionDate").toString() + "\",";
			json += "\"RevisionID\":" + data.getDouble("revisionId") + ",";
			json += "\"FutureData\":\"" + data.getString("futureData") + "\",";
			json += "\"SummonerID\":" + data.getDouble("SummonerID") + ",";
			json += "\"SummonerLevel\":" + data.getInt("summonerLevel") + "}";
			return json;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getInGameProgressInfo(String region, String name)
	{
		LoLRTMPSClient client = LoadBalancer.returnClient(region);
		try 
		{
			int id = client.invoke("gameService", "retrieveInProgressSpectatorGameInfo", new Object[] {name});
			TypedObject data = client.getResult(id).getTO("data").getTO("body");
			String json = "{";
			json += "\"GameSpecificLoyaltyRewards\":\"" + data.get("gameSpecificLoyaltyRewards") + "\",";
			json += "\"RecconectDelay\":" + data.getDouble("reconnectDelay") + ",";
			json += "\"DataVersion\":" + data.getInt("dataVersion") + ",";
			json += "\"LastModifiedDate\":\"" + data.getString("lastModifiedDate") + "\",";
			json += "\"Game\":{";
			TypedObject gameData = data.getTO("game");
			json += "\"PasswordSet\":" + gameData.getBool("passwordSet") + ",";
			json += "\"SpectatorsAllowed\":\"" + gameData.getString("spectatorsAllowed") + "\",";
			json += "\"GameType\":\"" + gameData.getString("gameType") + "\",";
			json += "\"GameTypeConfigID\":" + gameData.getInt("gameTypeConfigId") + ",";
			json += "\"GameState\":\"" + gameData.getString("gameState") + "\",";
			json += "\"GlmGameID\":\"" + gameData.getString("glmGameId") + "\",";
			json += "\"GlmHost\":\"" + gameData.getString("glmHost") + "\",";
			Object[] observers = gameData.getArray("observers");
			//TODO: HANDLE OBSERVERS
			json += "\"StatusOfParticipants\":\"" + gameData.getString("statusOfParticipants") + "\",";
			json += "\"GlmSecurePort\":" + gameData.getInt("glmSecurePort") + ",";
			json += "\"OwnerSummary\":\"" + gameData.getString("ownerSummary") + "\",";
			json += "\"ID\":" + gameData.getInt("id") + ",";
			Object[] teamOne = gameData.getArray("teamOne");
			json += "\"TeamOne\":[";
			for(Object x : teamOne)
			{
				if(x instanceof TypedObject)
				{
					json += "{";
					TypedObject y = (TypedObject)x;
					json += "\"QueueRating\":" + y.getInt("queueRating") + ",";
					json += "\"AccountID\":" + y.getInt("accountId") + ",";
					json += "\"BotDifficulty\":\"" + y.getString("botDifficulty") + "\",";
					json += "\"Minor\":" + y.getBool("minor") + ",";
					json += "\"Locale\":\"" + y.getString("locale") + "\",";
					json += "\"LastSelectedSkinIndex\":" + y.getInt("lastSelectedSkinIndex") + ",";
					json += "\"PartnerID\":\"" + y.getString("partnerId") + "\",";
					json += "\"ProfileIconID\":" + y.getInt("profileIconId") + ",";
					json += "\"SummonerID\":" + y.getInt("summonerId") + ",";
					json += "\"DataVersion\":" + y.getInt("dataVersion") + ",";
					json += "\"SelectedRole\":\"" + y.getString("selectedRole") + "\",";
					json += "\"PickMode\":" + y.getInt("pickMode") + ",";
					json += "\"TeamParticipantID\":" + y.getInt("teamParticipantId") + ",";
					json += "\"Index\":" + y.getInt("index") + ",";
					json += "\"TimeAddedToQueue\":" + y.getDouble("timeAddedToQueue") + ",";
					json += "\"OriginalAccountNumber\":" + y.getInt("originalAccountNumber") + ",";
					json += "\"SummonerInternalName\":\"" + y.getString("summonerInternalName") +"\",";
					json += "\"TeamOwner\":" + y.getBool("teamOwner") + ",";
					json += "\"FutureData\":\"" + y.getString("futureData") + "\",";
					json += "\"Badges\":" + y.getInt("badges") + ",";
					json += "\"PickTurn\":" + y.getInt("pickTurn") + ",";
					json += "\"ClientInSynch\":" + y.getBool("clientInSynch") + ",";
					json += "\"SummonerName\":\"" + y.getString("summonerName") + "\",";
					json += "\"OriginalPlatformID\":\"" + y.getString("originalPlatformId") + "\",";
					json += "\"SelectedPosition\":\"" + y.getString("selectedPosition") + "\"},";
				}
			}
			json = json.substring(0, json.length()-1);
			json += "],";
			Object[] teamTwo = gameData.getArray("teamTwo");
			json += "\"TeamTwo\":[";
			for(Object x : teamTwo)
			{
				if(x instanceof TypedObject)
				{
					json += "{";
					TypedObject y = (TypedObject)x;
					json += "\"QueueRating\":" + y.getInt("queueRating") + ",";
					json += "\"AccountID\":" + y.getInt("accountId") + ",";
					json += "\"BotDifficulty\":\"" + y.getString("botDifficulty") + "\",";
					json += "\"Minor\":" + y.getBool("minor") + ",";
					json += "\"Locale\":\"" + y.getString("locale") + "\",";
					json += "\"LastSelectedSkinIndex\":" + y.getInt("lastSelectedSkinIndex") + ",";
					json += "\"PartnerID\":\"" + y.getString("partnerId") + "\",";
					json += "\"ProfileIconID\":" + y.getInt("profileIconId") + ",";
					json += "\"SummonerID\":" + y.getInt("summonerId") + ",";
					json += "\"DataVersion\":" + y.getInt("dataVersion") + ",";
					json += "\"SelectedRole\":\"" + y.getString("selectedRole") + "\",";
					json += "\"PickMode\":" + y.getInt("pickMode") + ",";
					json += "\"TeamParticipantID\":" + y.getInt("teamParticipantId") + ",";
					json += "\"Index\":" + y.getInt("index") + ",";
					json += "\"TimeAddedToQueue\":" + y.getDouble("timeAddedToQueue") + ",";
					json += "\"OriginalAccountNumber\":" + y.getInt("originalAccountNumber") + ",";
					json += "\"SummonerInternalName\":\"" + y.getString("summonerInternalName") +"\",";
					json += "\"TeamOwner\":" + y.getBool("teamOwner") + ",";
					json += "\"FutureData\":\"" + y.getString("futureData") + "\",";
					json += "\"Badges\":" + y.getInt("badges") + ",";
					json += "\"PickTurn\":" + y.getInt("pickTurn") + ",";
					json += "\"ClientInSynch\":" + y.getBool("clientInSynch") + ",";
					json += "\"SummonerName\":\"" + y.getString("summonerName") + "\",";
					json += "\"OriginalPlatformID\":\"" + y.getString("originalPlatformId") + "\",";
					json += "\"SelectedPosition\":\"" + y.getString("selectedPosition") + "\"},";
				}
			}
			json = json.substring(0, json.length()-1);
			json += "],";
			
			Object[] bannedChamps = gameData.getArray("bannedChampions");
			//TODO: HANDLE BANNED CHAMPIONS
			
			json += "\"DataVersion\":" + gameData.getInt("dataVersion") + ",";
			json += "\"RoomName\":\"" + gameData.getString("roomName") + "\",";
			json += "\"Name\":\"" + gameData.getString("name") + "\",";
			json += "\"SpectatorDelay\":" + gameData.getInt("spectatorDelay") + ",";
			json += "\"TerminatedCondition\":\"" + gameData.getString("terminatedCondition") + "\",";
			json += "\"QueueTypeName\":\"" + gameData.getString("queueTypeName") + "\",";
			json += "\"GlmPort\":" + gameData.getInt("glmPort") + ",";
			json += "\"PassbackURL\":\"" + gameData.getString("passbackUrl") + "\",";
			json += "\"RoomPassword\":\"" + gameData.getString("roomPassword") + "\",";
			json += "\"OptimisticLock\":" + gameData.getDouble("optimisiticLock") + ",";
			json += "\"MaxNumPlayers\":" + gameData.getInt("maxNumPlayers") + ",";
			json += "\"QueuePosition\":" + gameData.getInt("queuePosition") + ",";
			json += "\"FutureData\":\"" + gameData.getString("futureData") + "\",";
			json += "\"ExpiryTime\":" + gameData.getDouble("expiryTime") + ",";
			json += "\"GameMode\":\"" + gameData.getString("gameMode") + "\",";
			json += "\"MapID\":" + gameData.getInt("mapId") + ",";
			json += "\"BanOrder\":\"" + gameData.getString("banOrder") + "\",";
			json += "\"GameStateString\":\"" + gameData.getString("gameStateString") + "\",";
			json += "\"PickTurn\":" + gameData.getInt("pickTurn") + ",";
			
			Object[] playerPicks = gameData.getArray("playerChampionSelections");
			
			json += "\"PlayerChampionSelections\":[";
			for(Object x : playerPicks)
			{
				if(x instanceof TypedObject)
				{
					json += "{";
					TypedObject y = (TypedObject)x;
					json += "\"DataVersion\":" + y.getInt("dataVersion") + ",";
					json += "\"SummonerInternalName\":\"" + y.getString("summonerInternalName") + "\",";
					json += "\"Spell1ID\":" + y.getInt("spell1ID") + ",";
					json += "\"Spell2ID\":" + y.getInt("spell2ID") + ",";
					json += "\"SelectedSkinIndex\":" + y.getInt("selectedSkinIndex") + ",";
					json += "\"ChampionID\":" + y.getInt("championId") + ",";
					json += "\"FutureData\":\"" + y.getString("futureData") + "\"},";
				}
			}
			json = json.substring(0, json.length()-1);
			json += "],";
			
			json += "\"PassbackDataPacket\":\"" + gameData.getString("passbackDataPacket") + "\",";
			json += "\"JoinTimerDuration\":" + gameData.getInt("joinTimerDuration") + "},";
			
			
			json += "\"ConnectivityStateEnum\":\"" + data.getString("connectivityStateEnum") + "\",";
			json += "\"GameName\":\"" + data.getString("\"gameName") + "\",";
			json += "\"PlayerCredentials\":{";
			TypedObject credData = data.getTO("playerCredentials");
			json += "\"EncryptionKey\":\"" + credData.getString("encryptionKey") + "\",";
			json += "\"GameID\":" + credData.getInt("gameId") + ",";
			json += "\"LastSelectedSkinIndex\":" + credData.getInt("lastSelectedSkinIndex") + ",";
			json += "\"ServerIP\":\"" + credData.getString("serverIp") + "\",";
			json += "\"Observer\":" + credData.getBool("observer") + ",";
			json += "\"SummonerID\":" + credData.getInt("summonerId") + ",";
			json += "\"FutureData\":\"" + credData.getString("futureData") + "\",";
			json += "\"ObserverServerIP\":\"" + credData.getString("observerServerIp") + "\",";
			json += "\"DataVersion\":" + credData.getInt("dataVersion") + ",";
			json += "\"HandshakeToken\":\"" + credData.getString("handshakeToken") + "\",";
			json += "\"PlayerID\":" + credData.getInt("playerId") + ",";
			json += "\"ServerPort\":" + credData.getInt("serverPort") + ",";
			json += "\"ObserverServerPort\":" + credData.getInt("observerServerPort") + ",";
			json += "\"SummonerName\":\"" + credData.getString("summonerName") + "\",";
			json += "\"ObserverEncryptionKey\":\"" + credData.getString("observerEncryptionKey") + "\",";
			json += "\"ChampionID\":" + credData.getInt("championId") + "},";
			
			json += "\"FutureData\":\"" + data.getString("futureData") + "\"}";
			
			return json;
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * playerStatsService
	 * getAggregatedStats
	 * @return
	 */
	//TODO Check for null
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
