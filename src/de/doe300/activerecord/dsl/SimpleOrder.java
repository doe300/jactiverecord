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
import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Syntax;
import javax.annotation.concurrent.Immutable;

import de.doe300.activerecord.record.ActiveRecord;

/**
 * An object representing the SQL ORDER BY-Clause
 * @author doe300
 * @since 0.7
 */
@Immutable
class SimpleOrder implements Order
{
	@Nonnull
	protected final String[] columns;
	@Nonnull
	protected final OrderType types[];

	/**
	 * The order of the <code>columns</code> specifies the priority of the column in the ordering.
	 * If the <code>types</code>-array is smaller than the <code>columns</code>, the rest will be filled with {@link OrderType#ASCENDING}
	 * allowing for  only the <code>columns</code> to be specified.
	 * @param columns
	 * @param types the order-types, may be <code>null</code>
	 */
	SimpleOrder(@Nonnull final String[] columns, @Nullable final OrderType[] types)
	{
		this.columns = columns;
		this.types = SimpleOrder.levelTypes( columns.length, types );
	}

	/**
	 * Order by a single column
	 * @param column
	 * @param type
	 */
	SimpleOrder(@Nonnull final String column, @Nonnull final OrderType type)
	{
		this.columns = new String[]{column};
		this.types = new OrderType[]{type};
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
	public int compare( ActiveRecord o1, ActiveRecord o2 )
	{
		final Map<String,Object> map1 = o1.getBase().getStore().getValues( o1.getBase(), o1.getPrimaryKey(), columns);
		final Map<String,Object> map2 = o2.getBase().getStore().getValues( o2.getBase(), o2.getPrimaryKey(), columns);
		return SimpleOrder.this.compare( map1, map2 );
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
				compare = Comparable.class.cast(val1).compareTo(o2.get(columns[index]));
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
	@Override
	public String toSQL(@Nonnull final JDBCDriver driver)
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
		return toSQL(JDBCDriver.DEFAULT);
	}

	@Override
	public Order reversed()
	{
		OrderType[] reversedTypes = new OrderType[types.length];
		for(int i = 0; i < types.length; i++)
		{
			reversedTypes[i] = types[i] == OrderType.ASCENDING ? OrderType.DESCENDING : OrderType.ASCENDING;
		}
		return new SimpleOrder(columns, reversedTypes);
	}

	@Override
	public boolean equals( Object obj )
	{
		if(obj == null || !(obj instanceof Order))
		{
			return false;
		}
		return equals( (Order)obj);
	}
	
	@Override
	public int hashCode()
	{
		return toSQL( JDBCDriver.DEFAULT ).hashCode();
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

		/**
		 * @return the SQL-representation of this order
		 */
		@Syntax(value = "SQL")
		public abstract String toSQL();
	}
}
