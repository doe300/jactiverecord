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
package de.doe300.activerecord;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.scope.Scope;
import javax.annotation.Nonnegative;

/**
 * A collection of finder-methods for {@link ActiveRecord records}
 * @author doe300
 * @param <T>
 */
public interface FinderMethods<T extends ActiveRecord>
{
	/**
	 * Finds all records matching the given condition
	 * @param condition
	 * @return all records matching the given condition or an empty Stream
	 */
	@Nonnull
	public default Stream<T> find(@Nullable final Condition condition)
	{
		return findWithScope( new Scope(condition, null, Scope.NO_LIMIT));
	}

	/**
	 * Finds the first record for the given condition
	 * @param condition
	 * @return the first record matching the condition or <code>null</code>
	 */
	@Nullable
	public default T findFirst(@Nullable final Condition condition)
	{
		return findFirstWithScope( new Scope(condition, null, Scope.NO_LIMIT));
	}

	/**
	 * @param column
	 * @param value
	 * @return all records for the given value or an empty Stream
	 */
	@Nonnull
	public default Stream<T> findFor(@Nonnull final String column, @Nullable final Object value)
	{
		return find( Conditions.is( column, value));
	}

	/**
	 * @param column
	 * @param value
	 * @return the first record for the given value or <code>null</code>
	 */
	@Nullable
	public default T findFirstFor(@Nonnull final String column, @Nullable final Object value)
	{
		return findFirst( Conditions.is( column, value));
	}

	/**
	 * @param data
	 * @return all records matching all the given values or an empty Stream
	 */
	@Nonnull
	public default Stream<T> findFor(@Nonnull final Map<String,Object> data)
	{
		final ArrayList<Condition> conds = new ArrayList<>(data.size());
		for(final Map.Entry<String,Object> e :data.entrySet())
		{
			conds.add( Conditions.is( e.getKey(), e.getValue()));
		}
		if(conds.size()==1)
		{
			return find( conds.get( 0));
		}
		return find(Conditions.and( conds.toArray( new Condition[conds.size()])) );
	}

	/**
	 * @param data
	 * @return the first record matching all given values or <code>null</code>
	 */
	@Nullable
	public default T findFirstFor(@Nonnull final Map<String,Object> data)
	{
		final ArrayList<Condition> conds = new ArrayList<>(data.size());
		for(final Map.Entry<String,Object> e :data.entrySet())
		{
			conds.add( Conditions.is( e.getKey(), e.getValue()));
		}
		if(conds.size()==1)
		{
			return findFirst( conds.get( 0));
		}
		return findFirst(Conditions.and(conds.toArray( new Condition[conds.size()]) ));
	}

	/**
	 * @return all records
	 */
	@Nonnull
	public default Stream<T> findAll()
	{
		return find( null);
	}

	/**
	 * The most flexible finder-method
	 * @param scope
	 * @return the stream of results
	 */
	@Nonnull
	public Stream<T> findWithScope(@Nonnull final Scope scope);

	/**
	 * @param scope
	 * @return the first record according to the scope or <code>null</code>
	 */
	@Nullable
	public T findFirstWithScope(@Nonnull final Scope scope);
	
	/**
	 * @param condition
	 * @return the number of records matching these conditions
	 */
	@Nonnegative
	public default int count(@Nullable final Condition condition)
	{
		return ( int ) find( condition ).count();
	}
}
