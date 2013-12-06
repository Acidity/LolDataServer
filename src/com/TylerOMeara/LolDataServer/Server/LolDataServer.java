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
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.TylerOMeara.LolDataServer.Server.Enums.Regions;

//TODO Detect when a PvP.net client lost connection and remove it from the loadbalancer.
//TODO Prevent retries on invalid username or password
//TODO Limit number of clients at once

/**
 * Main class for the program, contains the main method as well as program wide variables.
 * @author Tyler O'Meara
 *
 */

public class LolDataServer 
{
	public final static String version = "0.5.0";
	
	/**
	 * Port on which the server should listen for connections.
	 */
	public static int serverPort = 22222;
	
	/**
	 * Stores the PvP.Net version that the clients should use when connecting to League's servers.
	 */
	public static HashMap<String,String> PvPNetVersion = new HashMap<String, String>();
	
	public static LoadBalancer loadBalancer = new LoadBalancer();
	
	public static Logger log = Logger.getLogger("LoLDataServer");
	
	//Max amount of time to wait for PvPNet clients to connect before accepting clients
	private static long maxWaitTime = 10000;
	
	/**
	 * Array to hold users from the text file before they're initialized
	 */
	private static ArrayList<String> users = new ArrayList<String>();
	
	/**
	 * Holds the API Clients that have been connected since server start.
	 */
	//TODO: Remove entries that are no longer needed (not connected and past rate limiting time)
	public static ConcurrentHashMap<String,User> clients = new ConcurrentHashMap<String,User>();
	
	//Config booleans
	//TODO Move config stuff to separate file.
	public static boolean isUserAccessEnabled;
	public static boolean isRateLimitingEnabled;
	public static int defaultLimitTime;
	public static int defaultLimitAmount;
	public static boolean requireUsers;
	public static boolean overrideAsync;
	public static boolean async;
	public static boolean enableManualRequests = false;
	public static boolean debugMode = false;
	
	/**
	 * Main server method.
	 * @param args region::username::password - DEPRECATED, NO LONGER USED
	 */
	
	public static void main(String[] args)
	{
		System.out.println("Copyright 2013 Tyler O'Meara");
		log.fine("Copyright 2013 Tyler O'Meara");
		
		//Loads the configuration file
		try 
		{
			loadConfigFile();
		} 
		catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		//Sets up the logger for the server
		setUpLogger();
		
		System.out.println("Launching LoLDataServer v" + version);
		log.fine("Launching LoLDataServer v" + version);
		//Retrieves all entries from Users.txt and checks to make sure it isn't empty, if it is
		try 
		{
			loadUsernameFile();
			if(users.size() == 0)
			{
				System.err.println("No usernames were detected. Server will be unable to retrieve any data.");
				log.warning("No usernames were detected. Server will be unable to retrieve any data.");
			}
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//Loops through the arguments and creates a new client for each username password pair.
		//These braces are to leave the arraylist outside of the scope of the main method so that it doesn't waste memory during normal
		//server operation.
		{
			ArrayList<PvPNetClientInitializationThread> threads = new ArrayList<PvPNetClientInitializationThread>();
			for(String x : users)
			{
				PvPNetClientInitializationThread t = new PvPNetClientInitializationThread(x);
				t.start();
				threads.add(t);
			}
			//Iterates through the threads and waits maxWaitTime milliseconds for it to connect successfully.
			//After that time it acts as if it had connected successfully and proceeds to the next thread. After looping through
			//all the threads the server then proceeds to start accepting connections.
			for(PvPNetClientInitializationThread t : threads)
			{
				int iterations = 0;
				while(t.tries <= 1 && t.isAlive())
				{
					//Breaks loop for long login queues/slow connections
					if(maxWaitTime/100 < iterations)
					{
						break;
					}
					try {
						t.join(100);
						iterations++;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("All clients initiated, awaiting requests. Clients that couldn't connect will continue to " +
				"attempt to reconnect.");
		log.info("All clients initiated, awaiting requests. Clients that couldn't connect will continue to " +
				"attempt to reconnect.");
		System.out.println("Server initialized on port " + serverPort);
		log.info("Server initialized on port " + serverPort);
		
		//Creates server socket and server loop
		try 
		{
			ServerSocket ssocket = new ServerSocket(serverPort);
			Socket listen;
			//Main Server Loop, should never exit except upon program destruction
			while((listen = ssocket.accept()) != null)
			{
				listen.setSoTimeout(600000);
				NetworkingThread thread = new NetworkingThread(listen);
				thread.setName("NetworkingThread: " + listen.getInetAddress());
				thread.start();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Server exited.");
		log.info("Server exited.");
	}
	
	/**
	 * Set's up the logger for the server
	 */
	private static void setUpLogger()
	{
		try {
			//TODO Fix logging format
			FileHandler fh = new FileHandler("log.txt");
			SimpleFormatter sf = new SimpleFormatter();
			fh.setFormatter(sf);
			log.addHandler(fh);
			log.setUseParentHandlers(false);
		} catch (SecurityException e1) {
			System.err.println("Error creating log due to SecurityException");
		} catch (IOException e1) {
			System.err.println("Error creating log due to IOException");
		}
		//TODO: Allow to be set in config
		log.setLevel(Level.ALL);
	}
	
	/**
	 * Loads Users.txt and adds them to users
	 * @throws IOException
	 */
	private static void loadUsernameFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("Users.txt"));
		String s;
		while((s = br.readLine()) != null)
		{
			users.add(s);
		}
		br.close();
	}
	
	/**
	 * Goes through the config and changes the proper variables based on the settings.
	 * @throws IOException
	 */
	private static void loadConfigFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("LDSconfig.txt"));
		String s;
		while((s = br.readLine()) != null)
		{
			String paramater = s.split(":")[0];
			//Ignore comments in the config file
			if(paramater.startsWith("//"))
			{
				continue;
			}
			switch(paramater)
			{
				case "Port":
					serverPort = Integer.valueOf(s.split(":")[1]);
					break;
				case "PvPNetVersion":
				{
					String region = (s.split(":")[1]).split("-")[0];
					PvPNetVersion.put(region, (s.split(":")[1]).split("-")[1]);
					//NOTE: All ONLY FILLS THOSE REGIONS NOT SPECIFIED.
					if(region.equals("All"))
					{
						for(Regions r : Regions.values())
						{
							if(!PvPNetVersion.containsKey(r))
							{
								PvPNetVersion.put(r.toString(), (s.split(":")[1]).split("-")[1]);
							}
						}
					}
					break;
				}
				case "AuthenticateUsers":
				{
					boolean value = Boolean.valueOf((s.split(":")[1])); //TODO: Check that it's a boolean
					isUserAccessEnabled = value;
					//TODO: Read in users file in separate thread
					if(value)
						loadLDSUsernameFile();
					break;
				}
				case "RequireUsers":
				{
					requireUsers = Boolean.valueOf((s.split(":")[1]));//TODO: Check boolean
					break;
				}
				case "DefaultLimit":
				{
					defaultLimitAmount = Integer.valueOf(s.split(":")[1]);//TODO Check integer
					break;
				}
				case "LimitTime":
				{
					defaultLimitTime = Integer.valueOf(s.split(":")[1]);//TODO: Check Integer
					break;
				}
				case "DefaultAsync":
				{
					async = Boolean.valueOf(s.split(":")[1]);
					break;
				}
				case "EnforceAsyncSetting":
				{
					overrideAsync = Boolean.valueOf(s.split(":")[1]);
					break;
				}
				case "EnableManualRequests":
				{
					enableManualRequests = Boolean.valueOf(s.split(":")[1]);
					break;
				}
				case "Debug":
				{
					debugMode = Boolean.valueOf(s.split(":")[1]);
					break;
				}
				case "SSL":
				{
					//TODO
					break;
				}
				case "RequireSSL":
				{
					//TODO
					break;
				}
				default:
					break;
			}
		}
		br.close();
	}
	
	/**
	 * Loads LDSUsers.txt
	 * @throws IOException
	 */
	//TODO: Documentation - Users must have a key
	private static void loadLDSUsernameFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("LDSUsers.txt"));
		String s;
		boolean nested = false;
		AuthenticatedUser u = null;
		while((s = br.readLine()) != null)
		{
			if(s.startsWith("//"))
				continue;
			if(!nested && !s.equals("{"))
			{
				u = new AuthenticatedUser(s);
				continue;
			}
			if(s.equals("{"))
			{
				nested = true;
				continue;
			}
			if(s.equals("}"))
			{
				nested = false;
				clients.put(u.getKey(),u);
				continue;
			}
			String paramater = s.split(":")[0].trim();
			switch(paramater)
			{
				case "IPAddress":
					u.setIpAddress(s.split(":")[1]);
					break;
				case "Group":
					u.setGroup(s.split(":")[1]);
					break;
				case "Key":
					u.setKey(s.split(":")[1]);
					break;
				case "RequestLimit":
					u.setRequestLimit(Integer.valueOf(s.split(":")[1]));//TODO: Make sure this is an integer
					break;
				case "LimitTime":
					u.setLimitTime(Long.valueOf(s.split(":")[1]));//TODO: Make sure this is a long
					break;
				default:
					break;
			}
		}
		br.close();
	}
}
