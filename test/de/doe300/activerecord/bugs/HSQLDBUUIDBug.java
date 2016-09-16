/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.bugs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 *
 * @author doe300
 */
public class HSQLDBUUIDBug
{
	//Occurs with hsqldb 2.3.4
	//Reported as https://sourceforge.net/p/hsqldb/bugs/1446/
	//fixed and will be released in version 2.3.5
	
	public static void main(String[] args) throws SQLException
	{
		final Connection con = DriverManager.getConnection( "jdbc:hsqldb:mem:test", "sa", "");
		con.createStatement().execute( "CREATE TABLE testUUID (testColumn UUID NULL)" );
		final PreparedStatement insert = con.prepareStatement( "INSERT INTO testUUID (testColumn) VALUES (?)" );
		final UUID uuid1 = UUID.randomUUID();
		insert.setObject( 1, uuid1);
		insert.execute();
		final ResultSet res = con.createStatement().executeQuery( "SELECT * FROM testUUID");
		if(res.next())
		{
			System.out.println( res.getObject( "testColumn").getClass() );
			//-> This should be "class java.util.UUID"
		}
	}
}
