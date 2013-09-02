package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//TODO Add ability to require authentication before returning data
//TODO Prevent retries on invalid username or password

public class Main 
{
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
	public static String PvPNetVersion = "3.10.13_08_21_11_50";
	
	public static LoadBalancer loadBalancer = new LoadBalancer();
	
	/**
	 * Main server method.
	 * @param args region::username::password
	 */
	
	//TODO Handle server's going down
	
	public static void main(String[] args)
	{
		//TODO: Pull Version number from args
		//Check that the program has args provided.
		if(args.length == 0)
		{
			System.out.println("You must provide at least 1 username and password.");
			//TODO: DEBUG CODE
			args = new String[10];

			//return;
		}
		//Loops through the arguments and creates a new client for each username password pair.
		{
			ArrayList<PvPNetClientInitializationThread> threads = new ArrayList<PvPNetClientInitializationThread>();
			for(String x : args)
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
		//TODO Debug Code
		System.out.println("All clients initiated, awaiting requests. Clients that couldn't connect will continue to " +
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
}
