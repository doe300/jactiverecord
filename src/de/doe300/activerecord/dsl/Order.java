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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import de.doe300.activerecord.record.ActiveRecord;
import javax.annotation.Syntax;

/**
 * An object representing the SQL ORDER BY-Clause
 * @author doe300
 */
@Immutable
public class Order implements Comparator<Map<String,Object>>
{
	@Nonnull
	private final String[] columns;
	@Nonnull
	private final OrderType types[];

	/**
	 * The order of the <code>columns</code> specifies the priority of the column in the ordering.
	 * If the <code>types</code>-array is smaller than the <code>columns</code>, the rest will be filled with {@link OrderType#ASCENDING}
	 * allowing for  only the <code>columns</code> to be specified.
	 * @param columns
	 * @param types the order-types, may be <code>null</code>
	 */
	public Order(@Nonnull final String[] columns, @Nullable final OrderType[] types)
	{
		this.columns = columns;
		this.types = Order.levelTypes( columns.length, types );
	}

	/**
	 * Order by a single column
	 * @param column
	 * @param type
	 */
	public Order(@Nonnull final String column, @Nonnull final OrderType type)
	{
		this.columns = new String[]{column};
		this.types = new OrderType[]{type};
	}

	/**
	 * @param sqlOrderBy
	 * @return a new order from the SQL ORDER BY-Statement
	 */
	@Nullable
	public static Order fromSQLString(@Nullable final String sqlOrderBy)
	{
		if(sqlOrderBy==null|| sqlOrderBy.isEmpty())
		{
			return null;
		}
		final String stmt = sqlOrderBy.contains( "ORDER BY") ? sqlOrderBy.substring( sqlOrderBy.indexOf( "ORDER BY")+"ORDER BY".length()) : sqlOrderBy;
		final String[] parts = stmt.trim().split( "\\,");
		final String[] columns = new String[parts.length];
		final OrderType[] types = new OrderType[parts.length];
		String[] tmp;
		for(int i=0;i<parts.length;i++)
		{
			tmp = parts[i].trim().split( "\\s+");
			if(tmp.length > 1)
			{
				columns[i] = tmp[0];
				types[i] = tmp[1].equalsIgnoreCase( "DESC") ? OrderType.DESCENDING : OrderType.ASCENDING;
			}
			else
			{
				columns[i] = tmp[0];
				types[i] = OrderType.ASCENDING;
			}
		}
		return new Order(columns, types );
	}

	@Nonnull
	private static OrderType[] levelTypes(final int num, @Nullable final OrderType[] types)
	{
		if(types!=null && num==types.length)
		{
			return types;
		}
		final OrderType[] newTypes = new OrderType[ num ];
		Arrays.fill( newTypes, OrderType.ASCENDING);
		if(types!=null)
		{
			System.arraycopy( types, 0, newTypes, 0, types.length);
		}
		return newTypes;
	}

	@Override
	public int compare( final Map<String, Object> o1, final Map<String, Object> o2 )
	{
		int index = 0;
		int compare = 0;
		Object val1;
		while(compare== 0 && index < columns.length)
		{
			val1 = o1.get( columns[index]);
			if(val1 instanceof Comparable)
			{
				compare = ((Comparable)val1).compareTo( o2.get( columns[index]));
				if(types[index] == OrderType.DESCENDING)
				{
					compare = -compare;
				}
			}
			index++;
		}
		return compare;
	}

	/**
	 * @return a SQL representation of this Order
	 */
	@Syntax(value = "SQL")
	public String toSQL()
	{
		final StringBuilder sb = new StringBuilder(100);
		for(int i=0;i<columns.length;i++)
		{
			sb.append( ", ").append( columns[i]).append( " ").append( types[i].toSQL());
		}
		//deletes first ', '
		sb.delete( 0, 2 );
		return sb.toString();
	}

	@Override
	public String toString()
	{
		return toSQL();
	}

	/**
	 * @return a Comparator to sort records
	 */
	@Nonnull
	public Comparator<ActiveRecord> toRecordComparator()
	{
		return (o1, o2) -> {
			final Map<String,Object> map1 = o1.getBase().getStore().getValues( o1.getBase(), o1.getPrimaryKey(), columns);
			final Map<String,Object> map2 = o2.getBase().getStore().getValues( o2.getBase(), o2.getPrimaryKey(), columns);
			return Order.this.compare( map1, map2 );
		};
	}

	/**
	 * The type of ordering
	 */
	public static enum OrderType
	{
		/**
		 * Order by value ascending
		 */
		ASCENDING {

			@Override
			public String toSQL()
			{
				return "ASC";
			}
		},
		/**
		 * Order by value descending
		 */
		DESCENDING {

			@Override
			public String toSQL()
			{
				return "DESC";
			}
		};

		@Syntax(value = "SQL")
		public abstract String toSQL();
	}
}
