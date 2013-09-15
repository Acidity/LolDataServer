package com.TylerOMeara.LolDataServer.Server;

import java.io.IOException;

public class PvPNetClientInitializationThread extends Thread
{
	String x;
	public Integer tries = 1;
	public PvPNetClientInitializationThread(String x)
	{
		this.x = x;
	}
	
	public void run()
	{
		String[] xsplit = x.split("::");
		
		//Verifies that the argument has a username and password.
		if(xsplit.length < 3)
		{
			System.err.println("Error with argument " + x);
			Main.log.severe("Error with argument " + x);
			System.err.println("Ignoring that argument...");
			Main.log.severe("Ignoring that argument...");
			return;
		}
		
		while(!addClient(x))
		{
			try {
				Thread.sleep(5000 * tries);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			synchronized(tries)
			{
				if(tries < 30)
				{
					tries++;
				}
			}
		}
	}
	
	public boolean addClient(String x)
	{
		try {
			LoadBalancer.registerNewClient(x);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Had an error connecting to " + x.split("::")[0] + ": " + e.getMessage());
			System.err.println("Will continue to try to connect...");
			Main.log.warning("Had an error connecting to " + x.split("::")[0] + ": " + e.getMessage());
			Main.log.warning("Will continue to try to connect...");
			return false;
		}
		return true;
	}
}