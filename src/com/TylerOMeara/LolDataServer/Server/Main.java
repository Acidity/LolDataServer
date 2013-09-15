package com.TylerOMeara.LolDataServer.Server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//TODO Add ability to require authentication before returning data
//TODO Prevent retries on invalid username or password

public class Main 
{
	public final static String version = "1.0.0";
	
	/**
	 * Port on which the server should listen for connections.
	 */
	//TODO:Allow admins to change
	public static int serverPort = 22222;
	
	/**
	 * Stores the PvP.Net version that the clients should use when connecting to League's servers.
	 */
	//TODO:Make Variable based on region
	//TODO Allow admins to specify
	public static String PvPNetVersion = "3.11.13_09_11_18_32";
	
	public static LoadBalancer loadBalancer = new LoadBalancer();
	
	public static Logger log = Logger.getLogger("LoLDataServer");
	
	private static ArrayList<String> users = new ArrayList<String>();
	
	/**
	 * Main server method.
	 * @param args region::username::password
	 */
	
	public static void main(String[] args)
	{
		//TODO: Add copyright notice etc.
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
}
