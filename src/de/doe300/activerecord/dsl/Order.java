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

import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * An object representing the SQL ORDER BY-Clause
 * @author doe300
 */
public class Order implements Comparator<Map<String,Object>>, SQLCommand
{
	private final String[] columns;
	private final OrderType types[];

	/**
	 * The order of the <code>columns</code> specifies the priority of the column in the ordering.
	 * If the <code>types</code>-array is smaller than the <code>columns</code>, the rest will be filled with {@link OrderType#ASCENDING}
	 * allowing for  only the <code>columns</code> to be specified.
	 * @param columns
	 * @param types the order-types, may be <code>null</code> 
	 */
	public Order( String[] columns, OrderType[] types )
	{
		this.columns = columns;
		this.types = levelTypes( columns.length, types );
	}

	/**
	 * Order by a single column
	 * @param column
	 * @param type 
	 */
	public Order(String column, OrderType type)
	{
		this.columns = new String[]{column};
		this.types = new OrderType[]{type};
	}
	
	/**
	 * @param sqlOrderBy
	 * @return a new order from the SQL ORDER BY-Statement
	 */
	public static Order fromSQLString(String sqlOrderBy)
	{
		if(sqlOrderBy==null|| sqlOrderBy.isEmpty())
		{
			return null;
		}
		String stmt = sqlOrderBy.contains( "ORDER BY") ? sqlOrderBy.substring( sqlOrderBy.indexOf( "ORDER BY")+"ORDER BY".length()) : sqlOrderBy;
		String[] parts = stmt.trim().split( "\\,");
		String[] columns = new String[parts.length];
		OrderType[] types = new OrderType[parts.length];
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
	
	private OrderType[] levelTypes(int num, OrderType[] types)
	{
		if(types!=null && num==types.length)
		{
			return types;
		}
		OrderType[] newTypes = new OrderType[ num ];
		Arrays.fill( newTypes, OrderType.ASCENDING);
		if(types!=null)
		{
			System.arraycopy( types, 0, newTypes, 0, types.length);
		}
		return newTypes;
	}
	
	@Override
	public int compare( Map<String, Object> o1, Map<String, Object> o2 )
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

	@Override
	public String toSQL()
	{
		StringBuilder sb = new StringBuilder(100);
		for(int i=0;i<columns.length;i++)
		{
			sb.append( ", ").append( columns[i]).append( " ").append( types[i].toSQL());
		}
		//deletes first ', '
		sb.delete( 0, 2 );
		return sb.toString();
	}

	/**
	 * @return a Comparator to sort records
	 */
	public Comparator<ActiveRecord> toRecordComparator()
	{
		return new Comparator<ActiveRecord>()
		{
			@Override
			public int compare( ActiveRecord o1, ActiveRecord o2 )
			{
				Map<String,Object> map1 = o1.getBase().getStore().getValues( o1.getBase(), o1.getPrimaryKey(), columns);
				Map<String,Object> map2 = o2.getBase().getStore().getValues( o2.getBase(), o2.getPrimaryKey(), columns);
				return Order.this.compare( map1, map2 );
			}
		};
	}

	/**
	 * The type of ordering
	 */
	public static enum OrderType implements SQLCommand
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
		
		@Override
		public abstract String toSQL();
	}
}
