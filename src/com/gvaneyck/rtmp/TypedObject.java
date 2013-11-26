/*
  Copyright (C) 2012-2012 Gabriel Van Eyck

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

  Gabriel Van Eyck vaneyckster@gmail.com
 */

package com.gvaneyck.rtmp;
import java.util.Date;
import java.util.HashMap;

/**
 * A map that has a type, used to represent an object
 * 
 * @author Gabriel Van Eyck
 */
public class TypedObject extends HashMap<String, Object>
{
	private static final long serialVersionUID = 1244827787088018807L;

	public String type;
	
	/**
	 * Creates a typed object that is simply a map (null type)
	 */
	public TypedObject()
	{
		this.type = null;
	}

	/**
	 * Initializes the type of the object, null type implies a dynamic class
	 * (used for headers and some other things)
	 * 
	 * @param type The type of the object
	 */
	public TypedObject(String type)
	{
		this.type = type;
	}

	/**
	 * Creates a flex.messaging.io.ArrayCollection in the structure that the
	 * encoder expects
	 * 
	 * @param data The data for the ArrayCollection
	 * @return
	 */
	public static TypedObject makeArrayCollection(Object[] data)
	{
		TypedObject ret = new TypedObject("flex.messaging.io.ArrayCollection");
		ret.put("array", data);
		return ret;
	}

	/**
	 * Convenience for going through object hierarchy
	 * 
	 * @param key The key of the TypedObject
	 * @return The TypedObject
	 */
	public TypedObject getTO(String key)
	{
		return (TypedObject)get(key);
	}
	
	/**
	 * Convenience for retrieving Strings
	 * 
	 * @param key The key of the String
	 * @return The String
	 */
	public String getString(String key)
	{
		return (String)get(key);
	}

	/**
	 * Convenience for retrieving integers
	 * 
	 * @param key The key of the integer
	 * @return The integer
	 */
	public Integer getInt(String key)
	{
		Object val = get(key);
		if (val == null)
			return null;
		else if (val instanceof Integer)
			return (Integer)val;
		else 
			return ((Double)val).intValue();
	}

	/**
	 * Convenience for retrieving doubles
	 * 
	 * @param key The key of the double
	 * @return The double
	 */
	public Double getDouble(String key)
	{
		Object val = get(key);
		if (val == null)
			return null;
		else if (val instanceof Double)
			return (Double)val;
		else 
			return ((Integer)val).doubleValue();
	}
	
	/**
	 * Convenience for retrieving booleans
	 * 
	 * @param key The key of the boolean
	 * @return The boolean
	 */
	public Boolean getBool(String key)
	{
		return (Boolean)get(key);
	}

	/**
	 * Convenience for retrieving object arrays
	 * Also handles flex.messaging.io.ArrayCollection
	 * 
	 * @param key The key of the object array
	 * @return The object array
	 */
	public Object[] getArray(String key)
	{
		if (get(key) instanceof TypedObject && getTO(key).type.equals("flex.messaging.io.ArrayCollection"))
			return (Object[])getTO(key).get("array");
		else
			return (Object[])get(key);
	}

	/**
	 * Convenience for retrieving Date objects
	 * 
	 * @param key The key of the Date object
	 * @return The Date object
	 */
	public Date getDate(String key)
	{
		return (Date)get(key);
	}

	public String toString()
	{
		if (type == null)
			return super.toString();
		else if (type.equals("flex.messaging.io.ArrayCollection"))
		{
			StringBuilder sb = new StringBuilder();
			Object[] data = (Object[])get("array");
			sb.append("ArrayCollection:[");
			for (int i = 0; i < data.length; i++)
			{
				sb.append(data[i]);
				if (i < data.length - 1)
					sb.append(", ");
			}
			sb.append(']');
			return sb.toString();
		}
		else
			return type + ":" + super.toString();
	}
}
