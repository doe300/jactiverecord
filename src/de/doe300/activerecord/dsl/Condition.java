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
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.record.ActiveRecord;
import javax.annotation.Syntax;
import javax.annotation.concurrent.Immutable;

/**
 *
 * @author doe300
 */
@Immutable
public interface Condition extends Predicate<ActiveRecord>
{
	/**
	 * @param driver the vendor-specific driver
	 * @param tableName the name to use to uniquely identify the table
	 * @return the SQL representation of this condition
	 */
	@Nonnull
	@Syntax(value = "SQL")
	public String toSQL(@Nonnull final JDBCDriver driver, @Nullable final String tableName);
	
	/**
	 * The wildcards (<code>?</code>) are replaced by the conditions {@link #getValues() values}.
	 * @return whether this conditions uses wildcards in its SQL statement
	 */
	public boolean hasWildcards();

	/**
	 * @return the values to match
	 */
	@Nullable
	public Object[] getValues();

	/**
	 * @param record
	 * @return whether the <code>record</code> matches this condition
	 */
	@Override
	public boolean test(final ActiveRecord record);

	/**
	 * NOTE: This method is not required to be supported
	 * @param map
	 * @return whether the column-map matches this condition
	 * @throws UnsupportedOperationException if the method is not supported
	 */
	public boolean test(@Nonnull final Map<String, Object> map);

	@Override
	@Nonnull
	public Condition negate();

	/**
	 * Two conditions are equal, if the represent the same SQL-clause
	 * @param condition
	 * @return whether the two conditions are equal
	 * @since 0.7
	 * @see #equals(java.lang.Object) 
	 */
	public default boolean equals( @Nonnull final Condition condition )
	{
		final JDBCDriver driver = new JDBCDriver();
		return toSQL( driver, null ).equals( condition.toSQL( driver, null));
	}
}
