package de.doe300.activerecord.migration;

import java.sql.Connection;

/**
 * This migration is more general than the automatic migration.
 * Besides creating tables, the migration can be used for any other kind of generic SQL statement.
 * @author doe300
 */
public class ManualMigration implements Migration
{
	private final String command, revertedCommand, updateCommand;

	public ManualMigration(String command, String updateCommand, String revertCommand)
	{
		this.command = command;
		this.updateCommand = updateCommand;
		this.revertedCommand = revertCommand;
	}

	@Override
	public boolean apply( Connection con ) throws Exception
	{
		return con.createStatement().executeUpdate(command ) >= 0;
	}

	@Override
	public boolean revert( Connection con ) throws Exception
	{
		if(revertedCommand == null)
		{
			return false;
		}
		return con.createStatement().executeUpdate(revertedCommand) >= 0;
	}

	@Override
	public boolean update( Connection con ) throws Exception
	{
		if(updateCommand == null)
		{
			return false;
		}
		return con.createStatement().executeUpdate(updateCommand) >= 0;
	}
}
