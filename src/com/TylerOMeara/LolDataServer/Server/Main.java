package com.TylerOMeara.LolDataServer.Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.TylerOMeara.LolDataServer.Server.Enums.Regions;

//TODO Add ability to require authentication before returning data
//TODO Prevent retries on invalid username or password
//TODO Limit number of clients at once
//TODO RateLimit requests per client etc

public class Main 
{
	public final static String version = "1.0.0";
	
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
	
	private static ArrayList<String> users = new ArrayList<String>();
	
	/**
	 * Main server method.
	 * @param args region::username::password
	 */
	
	public static void main(String[] args)
	{
		System.out.println("Copyright 2013 Tyler O'Meara");
		log.fine("Copyright 2013 Tyler O'Meara");
		try 
		{
			loadConfigFile();
		} 
		catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		setUpLogger();
		System.out.println("Launching LoLDataServer v" + version);
		log.fine("Launching LoLDataServer v" + version);
		//Check that the program has args provided.
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
		{
			ArrayList<PvPNetClientInitializationThread> threads = new ArrayList<PvPNetClientInitializationThread>();
			for(String x : users)
			{
				PvPNetClientInitializationThread t = new PvPNetClientInitializationThread(x);
				t.start();
				threads.add(t);
			}
		/* for(Thread t: threads)
			{
				try {
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}*/
			for(PvPNetClientInitializationThread t : threads)
			{
				while(t.tries <= 1 && t.isAlive())
				{
					try {
						t.join(100);
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
	}
	
	public static void setUpLogger()
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
	
	public static void loadUsernameFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("Users.txt"));
		String s;
		while((s = br.readLine()) != null)
		{
			users.add(s);
		}
		br.close();
	}
	
	public static void loadConfigFile() throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("LDSconfig.txt"));
		String s;
		while((s = br.readLine()) != null)
		{
			String paramater = s.split(":")[0];
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
				}
			}
		}
		br.close();
	}
}
