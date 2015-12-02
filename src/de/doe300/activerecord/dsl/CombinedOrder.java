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
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * An {@link Order} consisting of a set of orders
 * @author doe300
 * @since 0.7
 */
public class CombinedOrder implements Order
{
	private final Order[] orders;


	/**
	 * @param orders the list of orders
	 */
	public CombinedOrder( @Nonnull final Order... orders )
	{
		this.orders = orders;
	}

	@Override
	public String toSQL( JDBCDriver driver )
	{
		return Arrays.stream( orders ).map( (Order o) -> o.toSQL( driver)).collect( Collectors.joining(", "));
	}

	@Override
	public Order reversed()
	{
		final Order[] reversedOrders = new Order[orders.length];
		for(int i = 0; i < reversedOrders.length; i++)
		{
			reversedOrders[i] = orders[i].reversed();
		}
		return new CombinedOrder(reversedOrders);
	}

	@Override
	public int compare( Map<String, Object> o1, Map<String, Object> o2 )
	{
		int index = 0;
		int compare = 0;
		while(compare== 0 && index < orders.length)
		{
			compare = orders[index].compare( o1, o2 );
			index++;
		}
		return compare;
	}

	@Override
	public int compare( ActiveRecord o1, ActiveRecord o2 )
	{
		int index = 0;
		int compare = 0;
		while(compare== 0 && index < orders.length)
		{
			compare = orders[index].compare( o1, o2 );
			index++;
		}
		return compare;
	}
	
}
