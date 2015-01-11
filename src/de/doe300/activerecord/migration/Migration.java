package de.doe300.activerecord.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author doe300
 */
public interface Migration
{
	/**
	 * Applies this migration to the given connection
	 * @param con
	 * @return whether the migration was applied
	 * @throws Exception 
	 */
	public boolean apply(Connection con) throws Exception;
	
	/**
	 * Reverts the changes from this migration
	 * @param con
	 * @return whether the migration was reverted
	 * @throws Exception 
	 */
	public boolean revert(Connection con) throws Exception;
	
	/**
	 * Update the data-structure or executes the <code>update</code>statement, depending on the type of migration
	 * @param con
	 * @return whether the update was successful
	 * @throws Exception 
	 */
	public boolean update(Connection con) throws Exception;
	
	/**
	 * @param con
	 * @param name
	 * @return whether the structure already exists
	 */
	public default boolean structureExists(Connection con, String name)
	{
		try
		{
			ResultSet set = con.getMetaData().getTables(con.getCatalog(), con.getSchema(), null, null );
			while(set.next())
			{
				if(set.getString( "TABLE_NAME").equalsIgnoreCase(name))
				{
					return true;
				}
			}
			return false;
		}
		catch ( SQLException ex )
		{
			return false;
		}
	}
}
