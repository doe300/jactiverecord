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
package de.doe300.activerecord.dsl;

import de.doe300.activerecord.jdbc.driver.JDBCDriver;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Comparator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Syntax;
import javax.annotation.concurrent.Immutable;

/**
 * An object representing the SQL ORDER BY-Clause
 * @author doe300
 */
@Immutable
public interface Order extends Comparator<ActiveRecord>
{
	/**
	 * @param driver the driver for the underlying RDBMS
	 * @return a SQL representation of this Order
	 */
	@Syntax(value = "SQL")
	public String toSQL(@Nonnull final JDBCDriver driver);
	
	@Override
	public int compare(ActiveRecord o1, ActiveRecord o2);
	
	/**
	 * @param o1 the attribute-value map for the first record
	 * @param o2 the attribute-value map for the second record
	 * @return the comparison-value
	 * @see #compare(de.doe300.activerecord.record.ActiveRecord, de.doe300.activerecord.record.ActiveRecord) 
	 */
	public int compare(Map<String, Object> o1, Map<String, Object> o2);
	
	/**
	 * @return an Order with exact the inverse ordering
	 * @since 0.7
	 */
	@Override
	public Order reversed();

	/**
	 * Orders are equal, if their SQL-representation is the same
	 * @param order
	 * @return whether the two orders are equal
	 * @see #equals(java.lang.Object) 
	 * @since 0.7
	 */
	public default boolean equals( @Nonnull final Order order )
	{
		return toSQL( JDBCDriver.DEFAULT ).equals( order.toSQL( JDBCDriver.DEFAULT));
	}
}
