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
package de.doe300.activerecord.examples.versioning;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.RecordCore;
import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.record.ActiveRecord;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Maintaining dummy-updates to provide data for the versioning-example
 * @author doe300
 */
public class Updates
{
	//The log of releases, in this case a simple array of versions, their changelog and an array of modified record-types
	private static final Object[][] update_log = {
		{"0.1", "Initial release", new String[]{"com.example.TestRecord"}},
		{"0.1.1", "Bug fixes", new String[]{"com.example.TestRecord", "com.example.Test2"}},
		{"0.2", "New features ...", new String[]{"com.example.NewFeature"}}
	};
	
	
	public static void main(String[] args) throws SQLException
	{
		//Here initialize DB-connection
		//...
		Connection con = null;
		RecordCore core = RecordCore.fromDatabase( con, true );
		
		
		//This code executed at startup of the program will ensure the database to be up to date
		RecordBase<Version> versions = core.buildBase( Version.class);
		for(int i = 0; i < update_log.length;i++)
		{
			//we assume, no entries are ever deleted from the 
			//so the ID of the record is the index in the array
			if(versions.hasRecord( i ))
			{
				continue;
			}
			Map<String,Object> data = new HashMap<>(2);
			data.put( "version", update_log[i][0]);
			data.put( "changelog", update_log[i][1]);
			//Save new version into database
			versions.createRecord( data );
			
			//Update modified record-types
			for(String className : (String[])update_log[i][2])
			{
				try
				{
					Class<? extends ActiveRecord> recordType = ( Class<? extends ActiveRecord> ) Class.forName( className);
					AutomaticMigration mig = new AutomaticMigration(recordType, true );
					//Tries to create the table
					if(!mig.apply( con ))
					{
						//Updates it instead, if it exists
						mig.update( con );
					}
				}
				catch ( ClassNotFoundException ex )
				{
					//either part of the library was not found or the record-type was removed
				}
			}
		}
		
		//Now the updates-table has all changelogs up to the version of the program
		//and all changes to the record-types are reflected in their corresponding tables
	}
}
