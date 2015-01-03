package de.doe300.activerecord.migration;

import de.doe300.activerecord.record.ActiveRecord;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 *
 * @author doe300
 */
public interface Migration
{
	/**
	 * Applies this migration to the given connection
	 * @param <T>
	 * @param con
	 * @return whether the migration was applied
	 * @throws Exception 
	 */
	public <T extends ActiveRecord> boolean apply(Connection con) throws Exception;
	
	/**
	 * Reverts the changes from this migration
	 * @param <T>
	 * @param con
	 * @return whether the migration was reverted
	 * @throws Exception 
	 */
	public <T extends ActiveRecord> boolean revert(Connection con) throws Exception;
	
	/**
	 * @param con
	 * @param name
	 * @return whether the structure already exists
	 */
	public default boolean structureExists(Connection con, String name)
	{
		try
		{
			ResultSet set = con.getMetaData().getTables(null, null, null, null );
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
	
	/**
	 * Returns the mapped sql-type
	 * @param type
	 * @return the mapped sql type
	 * @see Types
	 */
	public default String getSQLType(Class<?> type)
	{
		//TODO how to map??
		//copy JDBC-defaults
		return null;
	}
}
