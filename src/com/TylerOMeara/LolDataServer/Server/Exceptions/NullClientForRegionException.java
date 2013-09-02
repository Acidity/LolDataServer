package com.TylerOMeara.LolDataServer.Server.Exceptions;

public class NullClientForRegionException extends Exception
{
	private String region;
	/**
	 * 
	 */
	private static final long serialVersionUID = 3173952037837752529L;

	public NullClientForRegionException()
	{
		
	}
	
	public NullClientForRegionException(String message)
	{
		super(message);
	}
	
	public NullClientForRegionException(String message, String region)
	{
		super(message);
		this.region = region;
	}
	
	public String getRegion()
	{
		return region;
	}
}
