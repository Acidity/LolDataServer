package com.TylerOMeara.LolDataServer.Server;

public class PvPNetClientInitializationThread extends Thread
{
	String x;
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
			System.out.println("Error with argument " + x);
			System.out.println("Ignoring that argument...");
			return;
		}
		
		LoadBalancer.registerNewClient(x);
	}
}
