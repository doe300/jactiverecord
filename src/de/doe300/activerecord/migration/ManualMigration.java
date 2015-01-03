package de.doe300.activerecord.migration;

import de.doe300.activerecord.record.ActiveRecord;
import java.sql.Connection;

/**
 *
 * @author doe300
 */
public class ManualMigration implements Migration
{
	private final String command, revertedCommand;

	public ManualMigration(String command, String revertCommand)
	{
		this.command = command;
		this.revertedCommand = revertCommand;
	}

	@Override
	public <T extends ActiveRecord> boolean apply( Connection con ) throws Exception
	{
		con.createStatement().execute( command );
		return true;
	}

	@Override
	public <T extends ActiveRecord> boolean revert( Connection con ) throws Exception
	{
		if(revertedCommand == null)
		{
			return false;
		}
		con.createStatement().execute( revertedCommand);
		return true;
	}

}
