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

package de.doe300.activerecord.dsl;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper-class to create {@link Order orders} in various ways
 * @author doe300
 * @since 0.8
 */
public final class Orders
{

	/**
	 * Combines and optimized the orders by removing all duplicates and returning the single order, if only one is passed
	 *
	 * @param orders
	 * @return the combined order
	 */
	@Nonnull
	public static Order combine(@Nonnull final Order... orders )
	{
		if ( orders.length == 1 )
		{
			return orders[0];
		}
		final List<Order> newOrders = new ArrayList<>( orders.length );
		for ( Order order : orders )
		{
			if ( order != null && !newOrders.contains( order ) )
			{
				newOrders.add( order );
			}
		}
		if ( newOrders.size() == 1 )
		{
			return newOrders.get( 0 );
		}
		return new CombinedOrder( newOrders.toArray( new Order[ newOrders.size() ] ) );
	}

	/**
	 *
	 *
	 * @param sqlOrderBy
	 * @return a new order from the SQL ORDER BY-Statement
	 */
	@Nullable
	public static Order fromSQLString(@Nullable final String sqlOrderBy )
	{
		if ( sqlOrderBy == null || sqlOrderBy.isEmpty() )
		{
			return null;
		}
		final String stmt = sqlOrderBy.contains( "ORDER BY" ) ? sqlOrderBy.substring( sqlOrderBy.indexOf( "ORDER BY" ) +
				"ORDER BY".length() ) : sqlOrderBy;
		final String[] parts = stmt.trim().split( "\\," );
		final String[] columns = new String[ parts.length ];
		final SimpleOrder.OrderType[] types = new SimpleOrder.OrderType[ parts.length ];
		String[] tmp;
		for ( int i = 0; i < parts.length;
				i++ )
		{
			tmp = parts[i].trim().split( "\\s+" );
			if ( tmp.length > 1 )
			{
				columns[i] = tmp[0];
				types[i] = tmp[1].equalsIgnoreCase( "DESC" ) ? SimpleOrder.OrderType.DESCENDING : SimpleOrder.OrderType.ASCENDING;
			}
			else
			{
				columns[i] = tmp[0];
				types[i] = SimpleOrder.OrderType.ASCENDING;
			}
		}
		return new SimpleOrder( columns, types );
	}
	
	/**
	 * Creates an order for the given attribute in ascending order
	 * @param attributeName
	 * @return the new order
	 * @since 0.8
	 */
	@Nonnull
	public static Order sortAscending(@Nonnull final String attributeName)
	{
		return new SimpleOrder(attributeName, SimpleOrder.OrderType.ASCENDING );
	}
	
	/**
	 * Creates an order for the given attribute in ascending order
	 * @param scalarFunction
	 * @return the new order
	 * @since 0.8
	 */
	@Nonnull
	public static Order sortAscending(@Nonnull final ScalarFunction<?, ?, ?> scalarFunction)
	{
		return new ScalarOrder(scalarFunction.getAttributeName(), scalarFunction, SimpleOrder.OrderType.ASCENDING );
	}
	
	/**
	 * Creates an order for the given attribute in descending order
	 * @param attributeName
	 * @return the new order
	 * @since 0.8
	 */
	@Nonnull
	public static Order sortDescending(@Nonnull final String attributeName)
	{
		return new SimpleOrder(attributeName, SimpleOrder.OrderType.DESCENDING );
	}
	
	/**
	 * Creates an order for the given attribute in descending order
	 * @param scalarFunction
	 * @return the new order
	 * @since 0.8
	 */
	@Nonnull
	public static Order sortDescending(@Nonnull final ScalarFunction<?, ?, ?> scalarFunction)
	{
		return new ScalarOrder(scalarFunction.getAttributeName(), scalarFunction, SimpleOrder.OrderType.DESCENDING );
	}
	
	private Orders()
	{
		
	}
}
