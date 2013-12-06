/*
 *  LolDataServer
 *  Copyright (C) 2013 Tyler O'Meara
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.TylerOMeara.LolDataServer.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import com.TylerOMeara.LolDataServer.Server.API.GameService;
import com.TylerOMeara.LolDataServer.Server.API.LeaguesServiceProxy;
import com.TylerOMeara.LolDataServer.Server.API.PlayerStatsService;
import com.TylerOMeara.LolDataServer.Server.API.SummonerService;
import com.TylerOMeara.LolDataServer.Server.API.SummonerTeamService;
import com.TylerOMeara.LolDataServer.Server.API.BaseMethods;
import com.TylerOMeara.LolDataServer.Server.Enums.ArgumentTypes;
import com.TylerOMeara.LolDataServer.Server.Enums.GameMode;
import com.TylerOMeara.LolDataServer.Server.Enums.Queue;
import com.TylerOMeara.LolDataServer.Server.Enums.Season;

public class NetworkingThread extends Thread
{	
	//Do not include the async argument
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
	
	//Separate arg locations with &
	//... trailing a number means every arg following and including that specified
	private static final HashMap<String, String> operationGameModeArgs = new HashMap<String, String>()
	{
		{
			put("getRankedStats", "1");
		}
	};
	
	//Separate arg locations with &
	//... trailing a number means every arg following and including that specified
	private static final HashMap<String, String> operationQueueArgs = new HashMap<String, String>()
	{
		{
			put("getLeagueForPlayer", "1");
		}
	};
	
	//Separate arg locations with &
	//... trailing a number means every arg following and including that specified
	private static final HashMap<String, String> operationSeasonArgs = new HashMap<String, String>()
	{
		{
			put("getRankedStats", "2");
		}
	};
	
	private Socket socket;
	private User user;
	
	/**
	 * Creates a new NetworkingThread to handle the given socket
	 * @param socket
	 */
	
	public NetworkingThread(Socket socket)
	{
		LolDataServer.log.fine("Created NetworkingThread for IP Address: " + socket.getInetAddress());
		this.socket = socket;
	}
	
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
				//TODO: Documentation - keys can't contain ':'
				//Handles authentication statements from the API Client.
				if(line.startsWith("AUTHENTICATE:"))
				{
					if(user != null)
					{
						//MUST authenticate on first line sent
						socket.close();
						return;
					}
					user = LolDataServer.clients.get(line.split(":")[1]);
					if(user == null)
					{
						//TODO: Inform client of invalid key
						LolDataServer.log.fine("Invalid Authenticate message from " + socket.getInetAddress());
						socket.close();
						return;
					}
					if(user.getIpAddress() != null && !user.getIpAddress().equals(socket.getInetAddress().toString()))
					{
						//TODO: Inform client of invalid IP Address
						LolDataServer.log.fine(user.getName() + " attempted to log in from an invalid IP Address: " + socket.getInetAddress());
						socket.close();
						return;
					}
					continue;
				}
				//Checks if the server requires authentication, and if it does, closes the connection of
				//unauthenticated users.
				if(LolDataServer.requireUsers && user == null)
				{
					//TODO: Inform client of need to authenticate
					socket.close();
					return;
				}
				//Retrieves the user data for an unauthenticated user if they've already connected.
				//Required to ensure that closing the connection and then restarting it won't reset the rate limiting.
				if(user == null && LolDataServer.clients.get(socket.getInetAddress().toString()) != null)
				{
					user = LolDataServer.clients.get(socket.getInetAddress().toString());
				}
				//If none of the prior conditions were met, then create a new AnonymousUser to handle this API Client
				if(user == null)
				{
					user = new AnonymousUser();
					user.setIpAddress(socket.getInetAddress().toString());
					LolDataServer.clients.put(user.getIpAddress(), user);
				}
				
				//Logging stuff is fun!
				LolDataServer.log.fine("Received request from " + socket.getInetAddress() + " : line");
				String response = handleRequest(line);
				LolDataServer.log.fine("Returned to " + socket.getInetAddress() + " : " + response);
				pw.println(response);
			}
			//Cleaning up readers and writers.
			pw.close();
			br.close();
			isr.close();
		}
		catch(SocketTimeoutException e)
		{
			try {
				socket.close();
			} catch (IOException ie) {
				// TODO Auto-generated catch block
				ie.printStackTrace();
			}
		}
		catch(IOException e)
		{
			//e.printStackTrace();
		}
		finally
		{
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LolDataServer.log.fine("Destroyed NetworkingThread for IP Address: " + socket.getInetAddress());
	}
	
	/**
	 * Methods that handles all normal API requests.
	 * @param line Request as received by the server.
	 * @return Returns the requested data, or an error 
	 */
	//TODO Add new commands for PvP.net version and League version
	//TODO Allow for disabling of request validity
	private String handleRequest(String line)
	{
		//Handles when the last check time for limits was more than the limit period ago
		if(System.currentTimeMillis() - user.getLimitCheckTime() > user.getLimitTime()*1000)
		{
			user.setLimitCheckTime(System.currentTimeMillis());
			user.setCurrentRequests(1);
		}
		//If it's been less than the limit period since the last check but the user is still under their max requests just add one to their requests.
		else if(user.getCurrentRequests() < user.getRequestLimit())
		{
			user.iterateCurrentRequests();
		}
		//User has exceeded their maximum limit of requests
		else
		{
			return "You have exceeded your maximum limit of requests of " + user.getRequestLimit() + " requests per " + user.getLimitTime() + " seconds.";
		}
		
		//Splits the request into three parts based on ~
		String[] components = line.split("~");
		if(components.length != 3)
		{
			return "Invalid request: Must contain the requested region, operation and arguments seperated by a ~. Please see documentation " +
					"for more details.";
		}
		//By definition, the region is the first part, the operation is the second, and the arguments are the third
		String region = components[0];
		String operation = components[1];
		String[] arguments = components[2].split("&");
		
		//Checks that the string sent by the client is valid
		//Temporary variable necessary because the method returns an error statement if it is invalid.
		boolean async = false;
		if(!LolDataServer.enableManualRequests || !operation.equals("Manual"))
		{
			String temp;
			if(!(temp = checkValidArguments(operation, arguments)).equals("VALID"))
			{
				return temp;
			}
		
		
			//Handles async argument, it's optional, may be overriden by server, and is always last
			if(LolDataServer.overrideAsync || arguments.length == operationArgs.get(operation))
			{
				async = LolDataServer.async;
			}
			else
			{
				//Takes anything other than true to be false
				async = Boolean.valueOf(arguments[arguments.length-1]);
			}
		}
		
		//Go through and assign the arguments to variables for readability reasons, and then call the necessary function to get the data.
		//The data returned directly from the methods.
		//Case statements are in braces to localize variables
		switch(operation)
		{
			//NOT a normal API call. Exists only for debugging.
			case "Manual":
			{
				return BaseMethods.manualRequest(arguments[0]);
			}
			case "getInGameProgressInfo":
			{
				String summonerName = arguments[0];
				return GameService.getInGameProgressInfo(region, summonerName, async);
			}
			case "getLeagueForPlayer":
			{
				int summonerID = Integer.valueOf(arguments[0]);
				String queue = arguments[1];
				return LeaguesServiceProxy.getLeagueForPlayer(region, summonerID, queue, async);
			}
			case "getAllLeaguesForPlayer":
			{
				int summonerID = Integer.valueOf(arguments[0]);
				return LeaguesServiceProxy.getAllLeaguesForPlayer(region, summonerID, async);
			}
			case "getRecentGames":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return PlayerStatsService.getRecentGames(region, accountID, async);
			}
			case "retrievePlayerStatsByAccountId":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return PlayerStatsService.getPlayerStatsByAccountID(region, accountID, async);
			}
			case "getRankedStats":
			{
				int accountID = Integer.valueOf(arguments[0]);
				String gameMode = arguments[1];
				String season = Season.convertToInt(arguments[2]);
				return PlayerStatsService.getRankedStats(region, accountID, gameMode, season, async);
			}
			case "getSummonerByName":
			{
				String summonerName = arguments[0];
				return SummonerService.getSummonerByName(region, summonerName, async);
			}
			case "getSummonerNamesByIDs":
			{
				Object[] summonerIDs = new Object[arguments.length];
				for(int x = 0; x < arguments.length; x++)
				{
					summonerIDs[x] = Integer.valueOf(arguments[x]);
				}
				return SummonerService.getSummonersByIDs(region, summonerIDs, async);
			}
			case "getAllPublicSummonerDataByAccount":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return SummonerService.getAllPublicSummonerDataByAccount(region, accountID, async);
			}
			case "getAllSummonerDataByAccount":
			{
				int accountID = Integer.valueOf(arguments[0]);
				return SummonerService.getAllSummonerDataByAccount(region, accountID, async);
			}
			case "getPlayerRankedTeams":
			{
				int summonerID = Integer.valueOf(arguments[0]);
				return SummonerTeamService.getPlayerRankedTeams(region, summonerID, async);
			}
			default:
			{
				return "The operation you requested could not be found: " + operation;
			}
		}
	}
	
	/**
	 * Checks if the arguments supplied for the operation are of the valid type and quantity.
	 * @param operation The argument supplied by the client in the second position, the argument that specifies the operation
	 * @param arguments The argument supplied by the client in the third position, the argument that specifies the argument
	 * @return "VALID" if the data provided is valid, an error code otherwise.
	 */
	
	private String checkValidArguments(String operation, String[] arguments)
	{
		//Checks if actual operation
		if(!operationArgs.containsKey(operation))
		{
			return "The operation you requested could not be found: " + operation;
		}
		//TODO Consolidate this and make it more efficient
		//Checks number of arguments.
		if(operation.equals("getSummonerNamesByIDs") && arguments.length < operationArgs.get(operation))
		{
			return "Invalid number of arguments for the requested operation. " + operation + " requires at least "
					+ operationArgs.get(operation) + " arguments.";
		}
		//Checks number of arguments when async is provided.
		if(arguments.length != operationArgs.get(operation) && (arguments.length != (operationArgs.get(operation)+1) && !(arguments[arguments.length-1].equals("true") || arguments[arguments.length-1].equals("false"))))
		{
			return "Invalid number of arguments for the requested operation. " + operation + " requires "
					+ operationArgs.get(operation) + " arguments.";
		}
		
		//Loops through all argument types and checks if all arguments for that operation that should be of one
		//of those types is valid, otherwise it returns an error.
		String argType;
		for(ArgumentTypes type : ArgumentTypes.values())
		{
			if(!(argType = checkArgumentType(operation, arguments, type)).equals("VALID"))
			{
				return argType;
			}
		}
		//Everything is correct
		return "VALID";
	}
	
	/**
	 * Checks to see if the arguments for the specified ArgumentType are in the right position.
	 * @param operation Operation being called.
	 * @param arguments Arguments as provided by the client, in the array in *specified order*
	 * @param arg ArgumentType that the method should be checking
	 * @return "VALID" if the arguments of the specified type are in correct position, error message otherwise.
	 */
	private static String checkArgumentType(String operation, String[] arguments, ArgumentTypes arg)
	{
		//Specifies which argument hashmap to use.s
		HashMap<String,String> hashMap = null;
		switch(arg)
		{
			case NUMBER:
			{
				hashMap = operationNFEArgs;
				break;
			}
			case QUEUE:
			{
				hashMap = operationQueueArgs;
				break;
			}
			case SEASON:
			{
				hashMap = operationSeasonArgs;
				break;
			}
			case GAMEMODE:
			{
				hashMap = operationGameModeArgs;
				break;
			}
			default:
			{
				hashMap = null;
				break;
			}
		}
		
		//If the operation isn't in the hashmap for that argument type, that means it isn't required.
		if(!hashMap.containsKey(operation))
		{
			return "VALID";
		}
		String[] argsNums = hashMap.get(operation).split("&");
		
		//Checks all of the args to see if they're proper
		for(String s : argsNums)
		{
			if(s.contains("..."))
			{
				//TODO: Allow args to be individual and ...
				for(int x = Integer.valueOf(s.substring(0,1)); x < arguments.length; x++)
				{
					if(!isType(arguments[x], arg))
					{
						return "Error: Expected argument " + x + " to be a " + arg.toString() + ".";
					}
				}
			}
			if(!isType(arguments[Integer.valueOf(s)], arg))
			{
				return "Error: Expected argument " + s + " to be a " + arg.toString() + ".";
			}
		}
		return "VALID";
	}
	
	/**
	 * Checks if the string supplied is valid for the ArgumentType supplied
	 * @param argument String to be checked.
	 * @param arg ArgumentType being checked against.
	 * @return true if the string is one of the ArgumentType, false otherwise.
	 */
	
	private static boolean isType(String argument, ArgumentTypes arg)
	{
		switch(arg)
		{
			case NUMBER:
			{
				return isInteger(argument);
			}
			case QUEUE:
			{
				try
				{
					Queue.valueOf(argument);
					return true;
				}
				catch(IllegalArgumentException e)
				{
					return false;
				}
			}
			case SEASON:
			{
				try
				{
					Season.valueOf(argument);
					return true;
				}
				catch(IllegalArgumentException e)
				{
					//Both numbers and written out versions of Season numbers are valid
					if(isInteger(argument) && Integer.valueOf(argument) <= Season.CURRENT.getSeasonInt())
					{
						return true;
					}
					return false;
				}
			}
			case GAMEMODE:
			{
				try
				{
					GameMode.valueOf(argument);
					return true;
				}
				catch(IllegalArgumentException e)
				{
					return false;
				}
			}
			default:
			{
				return false;
			}
		}
	}
	
	@Deprecated
	private static String checkNFE(String operation, String[] arguments)
	{
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
	
	private static boolean isInteger(String s)
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
