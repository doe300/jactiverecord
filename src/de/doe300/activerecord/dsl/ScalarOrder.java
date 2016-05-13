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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An extended version of {@link SimpleOrder}, supporting scalar functions
 * 
 * @author doe300
 * @since 0.7
 */
class ScalarOrder extends SimpleOrder
{
	@Nonnull
	private final ScalarFunction<?, ?, ?>[] mappings;
	
	/**
	 * The order of the <code>columns</code> specifies the priority of the column in the ordering.
	 * If the <code>types</code>-array is smaller than the <code>columns</code>, the rest will be filled with {@link SimpleOrder.OrderType#ASCENDING}
	 * allowing for  only the <code>columns</code> to be specified.
	 * @param columns
	 * @param mappings the scalar functions to apply to the columns for value-mapping
	 * @param types the order-types, may be <code>null</code>
	 */
	ScalarOrder(@Nonnull final String[] columns, @Nullable final ScalarFunction<?, ?, ?>[] mappings, @Nullable final OrderType[] types)
	{
		super( columns, types );
		if(mappings == null)
		{
			this.mappings = new ScalarFunction[columns.length];
		}
		else
		{
			this.mappings = mappings;
			if(mappings.length != columns.length)
			{
				throw new IllegalArgumentException("Invalid number of scalar-functions specified");
			}
		}
	}

	/**
	 * Order by a single column
	 * @param column
	 * @param mapping
	 * @param type
	 */
	ScalarOrder(@Nonnull final String column, @Nullable final ScalarFunction<?, ?, ?> mapping, @Nonnull final OrderType type)
	{
		super( column, type );
		this.mappings = new ScalarFunction[]{mapping};
	}

	@Override
	public int compare( Map<String, Object> o1, Map<String, Object> o2 )
	{
		int index = 0;
		int compare = 0;
		Object val1, val2;
		ScalarFunction<?, ?,?> mapping;
		while(compare== 0 && index < columns.length)
		{
			mapping = mappings[index];
			if(mapping != null)
			{
				val1 = mapping.apply( o1);
				val2 = mapping.apply( o2);
			}
			else
			{
				val1 = o1.get( columns[index]);
				val2 = o2.get(columns[index]);
			}
			if(val1 instanceof Comparable)
			{
				compare = Comparable.class.cast(val1).compareTo(val2);
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
	public String toSQL(@Nonnull final JDBCDriver driver)
	{
		final StringBuilder sb = new StringBuilder(100);
		for(int i=0;i<columns.length;i++)
		{
			sb.append( ", ").append( mappings[i].toSQL( driver, null )).append( " ").append( types[i].toSQL());
		}
		//deletes first ', '
		sb.delete( 0, 2 );
		return sb.toString();
	}
	
	@Override
	public Order reversed()
	{
		OrderType[] reversedTypes = new OrderType[types.length];
		for(int i = 0; i < types.length; i++)
		{
			reversedTypes[i] = types[i] == OrderType.ASCENDING ? OrderType.DESCENDING : OrderType.ASCENDING;
		}
		return new ScalarOrder(columns, mappings, reversedTypes);
	}
}
