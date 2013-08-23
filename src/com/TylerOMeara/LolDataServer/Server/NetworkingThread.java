package com.TylerOMeara.LolDataServer.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

import com.TylerOMeara.LolDataServer.Server.API.GameService;
import com.TylerOMeara.LolDataServer.Server.API.LeaguesServiceProxy;
import com.TylerOMeara.LolDataServer.Server.API.PlayerStatsService;
import com.TylerOMeara.LolDataServer.Server.API.SummonerService;
import com.TylerOMeara.LolDataServer.Server.API.SummonerTeamService;

public class NetworkingThread extends Thread
{
	private static final HashMap<String, Integer> operationArgs = new HashMap<String, Integer>()
	{
		{
			put("getInGameProgressInfo", 1);
			put("getLeagueForPlayer", 2);
			put("getAllLeaguesForPlayer", 1);
			put("getRecentGames", 1);
			put("retrievePlayerStatsByAccountId", 1);
			put("getRankedStats", 3);
			put("getSummonerByName", 1);
			put("getSummonerNamesByIDs", 1);
			put("getAllPublicSummonerDataByAccount", 1);
			put("getAllSummonerDataByAccount", 1);
			put("getPlayerRankedTeams", 1);
		}
	};
	
	//Separate arg locations with &
	//... trailing a number means every arg following and including that specified
	private static final HashMap<String, String> operationNFEArgs = new HashMap<String, String>()
	{
		{
			put("getLeagueForPlayer", "0");
			put("getAllLeaguesForPlayer", "0");
			put("getRecentGames", "0");
			put("retrievePlayerStatsByAccountId", "0");
			put("getRecentGames", "0");
			put("getRankedStats", "0");
			put("getSummonerNamesByIDs", "0...");
			put("getAllPublicSummonerDataByAccount", "0");
			put("getAllSummonerDataByAccount", "0");
			put("getPlayerRankedTeams", "0");
		}
	};
	
	private Socket socket;
	
	public NetworkingThread(Socket socket)
	{
		this.socket = socket;
	}
	
	//TODO timeout connections
	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(socket.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			//Blocks until it receives the request from the client
			String line;
			while((line = br.readLine()) != null)
			{
				String response = handleRequest(line);
				pw.println(response);
				
				//TODO DEBUG CODE
				System.out.println(response);
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//TODO Find out why null getRankedStats returns "}"
	private String handleRequest(String line)
	{
		String[] components = line.split("~");
		if(components.length != 3)
		{
			return "Invalid request: Must contain the requested region, operation and arguments seperated by a ~. Please see documentation" +
					"for more details.";
		}
		String region = components[0];
		String operation = components[1];
		String[] arguments = components[2].split("&");
		//Case statements are in braces to localize variables
		String s;
		if(!(s = checkValidArguments(operation, arguments)).equals("VALID"))
		{
			return s;
		}
		switch(operation)
		{
			case "getInGameProgressInfo":
			{
				String summonerName = arguments[0];
				return GameService.getInGameProgressInfo(region, summonerName);
			}
			case "getLeagueForPlayer":
			{
				int summonerID = Integer.valueOf(arguments[0]);
				String queue = arguments[1];
				return LeaguesServiceProxy.getLeagueForPlayer(region, summonerID, queue);
			}
			case "getAllLeaguesForPlayer":
			{
				int summonerID = Integer.valueOf(arguments[0]);
				return LeaguesServiceProxy.getAllLeaguesForPlayer(region, summonerID);
			}
			case "getRecentGames":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return PlayerStatsService.getRecentGames(region, accountID);
			}
			case "retrievePlayerStatsByAccountId":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return PlayerStatsService.getPlayerStatsByAccountID(region, accountID);
			}
			case "getRankedStats":
			{
				int accountID = Integer.valueOf(arguments[0]);
				String gameMode = arguments[1]; //TODO enum gameModes
				String season = arguments[2]; //TODO enum Seasons
				return PlayerStatsService.getRankedStats(region, accountID, gameMode, season);
			}
			case "getSummonerByName":
			{
				String summonerName = arguments[0];
				return SummonerService.getSummonerByName(region, summonerName);
			}
			case "getSummonerNamesByIDs":
			{
				Object[] summonerIDs = new Object[arguments.length];
				for(int x = 0; x < arguments.length; x++)
				{
					summonerIDs[x] = Integer.valueOf(arguments[x]);
				}
				return SummonerService.getSummonersByIDs(region, summonerIDs);
			}
			case "getAllPublicSummonerDataByAccount":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return SummonerService.getAllPublicSummonerDataByAccount(region, accountID);
			}
			case "getAllSummonerDataByAccount":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return SummonerService.getAllSummonerDataByAccount(region, accountID);
			}
			case "getPlayerRankedTeams":
			{
				int summonerID = Integer.valueOf(arguments[0]);
				return SummonerTeamService.getPlayerRankedTeams(region, summonerID);
			}
			default:
			{
				return "The operation you requested could not be found: " + operation;
			}
		}
	}
	
	private String checkValidArguments(String operation, String[] arguments)
	{
		if(!operationArgs.containsKey(operation))
		{
			return "The operation you requested could not be found: " + operation;
		}
		//Checks number of arguments.
		if(operation.equals("getSummonerNamesByIDs") && arguments.length < operationArgs.get(operation))
		{
			return "Invalid number of arguments for the requested operation. " + operation + " requires at least "
					+ operationArgs.get(operation) + " arguments.";
		}
		if(arguments.length != operationArgs.get(operation))
		{
			return "Invalid number of arguments for the requested operation. " + operation + " requires "
					+ operationArgs.get(operation) + " arguments.";
		}
		if(!operationNFEArgs.containsKey(operation))
		{
			return "VALID";
		}
		String[] argsNums = operationNFEArgs.get(operation).split("&");
		for(String s : argsNums)
		{
			if(s.contains("..."))
			{
				for(int x = Integer.valueOf(s.substring(0,1)); x < arguments.length; x++)
				{
					if(!isInteger(arguments[x]))
					{
						return "Error: Expected argument " + x + " to be a number.";
					}
				}
			}
			if(!isInteger(arguments[Integer.valueOf(s)]))
			{
				return "Error: Expected argument " + s + " to be a number.";
			}
		}
		return "VALID";
	}
	
	public static boolean isInteger(String s)
	{
		try
		{
			Integer.valueOf(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}
}
