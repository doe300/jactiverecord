/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 doe300
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
package de.doe300.activerecord.store;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nonnull;

/**
 * Record-store using a JDBC driver as underlying storage
 * 
 * @author doe300
 * @since 0.7
 */
public interface JDBCRecordStore extends RecordStore
{
	/**
	 * @return the underlying Connection to access the database
	 */
	@Nonnull
	public Connection getConnection();

	@Override
	@Nonnull
	public JDBCDriver getDriver();
	
	/**
	 * CAUTION: calling this will result in losing all the data in the table!
	 * @param tableName
	 * @return whether the table was dropped
	 * @throws SQLException 
	 * @since 0.8
	 */
	public boolean dropTable(@Nonnull final String tableName) throws SQLException;
}
