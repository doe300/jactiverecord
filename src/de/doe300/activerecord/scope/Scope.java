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


package de.doe300.activerecord.scope;

import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import javax.annotation.Nullable;

/**
 * A scope is a set of predefined conditions to narrow the results
 * @author doe300
 */
public final class Scope
{
	/**
	 * No specific limit was set
	 */
	public static final int NO_LIMIT = -1;

	@Nullable
	private final Condition condition;
	@Nullable
	private final Order order;
	private final int limit;

	/**
	 *
	 * @param condition the condition to narrow the results, may be <code>null</code>
	 * @param order a order to apply to the results, may be <code>null</code>
	 * @param limit a maximum number of results to retrieve, <code>NO_LIMIT</code> to disable
	 */
	public Scope(@Nullable final Condition condition, @Nullable final Order order, final int limit )
	{
		this.condition = condition;
		this.order = order;
		this.limit = limit;
	}

	/**
	 * @return the condition, may be <code>null</code>
	 */
	@Nullable
	public Condition getCondition()
	{
		return condition;
	}

	/**
	 * @return the order, may be <code>null</code>
	 */
	@Nullable
	public Order getOrder()
	{
		return order;
	}

	/**
	 * @return the limit
	 */
	public int getLimit()
	{
		return limit;
	}
}
