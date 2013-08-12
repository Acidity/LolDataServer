package com.TylerOMeara.LolDataServer.Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class NetworkingThread extends Thread
{
	private Socket socket;
	
	public NetworkingThread(Socket socket)
	{
		this.socket = socket;
	}
	
	public void run()
	{
		try
		{
			InputStreamReader input = new InputStreamReader(socket.getInputStream());
			OutputStreamWriter output = new OutputStreamWriter(socket.getOutputStream());
			BufferedReader iReader = new BufferedReader(input);
			BufferedWriter oWriter = new BufferedWriter(output);
			//Blocks until it receives the request from the client
			String line = iReader.readLine();
			//TODO: DEBUG CODE
			String response = API.getRankedStats("NA",44001109,"CLASSIC","CURRENT");
			//String response = API.manualRequest(line);
			//TODO handle API messages
			oWriter.write(response);
			//TODO DEBUG CODE
			System.out.println(response);
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
