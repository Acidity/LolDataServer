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
