/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 doe300
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package de.doe300.activerecord.migration;

import java.sql.Connection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.logging.Logging;

/**
 * This migration is more general than the automatic migration.
 * Besides creating tables, the migration can be used for any other kind of generic SQL statement.
 * @author doe300
 */
public class ManualMigration implements Migration
{
	@Nonnull
	final Connection con;
	@Nonnull
	private final String command;
	@Nullable
	private final String revertedCommand, updateCommand;

	/**
	 *
	 * @param command the command for {@link #apply(java.sql.Connection) }
	 * @param updateCommand the command for {@link #update(java.sql.Connection) }
	 * @param revertCommand the command for {@link #revert(java.sql.Connection) }
	 * @param con the underlying connection
	 */
	public ManualMigration(@Nonnull final String command, @Nullable final String updateCommand,
		@Nullable final String revertCommand, @Nonnull final Connection con)
	{
		this.command = command;
		this.updateCommand = updateCommand;
		this.revertedCommand = revertCommand;
		this.con = con;
	}

	@Override
	public boolean apply() throws Exception
	{
		Logging.getLogger().info("ManualMigration", "Executing manual migration...");
		Logging.getLogger().info("ManualMigration", command);
		return con.createStatement().executeUpdate(command ) >= 0;
	}

	@Override
	public boolean revert() throws Exception
	{
		if(revertedCommand == null)
		{
			return false;
		}
		Logging.getLogger().info("ManualMigration", "Executing manual revert...");
		Logging.getLogger().info("ManualMigration", revertedCommand);
		return con.createStatement().executeUpdate(revertedCommand) >= 0;
	}

	@Override
	public boolean update(final boolean dropColumns) throws Exception
	{
		if(updateCommand == null)
		{
			return false;
		}
		Logging.getLogger().info("ManualMigration", "Executing manual update...");
		Logging.getLogger().info("ManualMigration", updateCommand);
		return con.createStatement().executeUpdate(updateCommand) >= 0;
	}
}
