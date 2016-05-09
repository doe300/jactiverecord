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

import javax.annotation.Nullable;

import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * A scope is a set of predefined conditions to narrow the results
 * @author doe300
 */
@Immutable
public final class Scope
{
	/**
	 * No specific limit was set
	 */
	public static final int NO_LIMIT = -1;
	
	/**
	 * Scope which does not limit, filter or order the results
	 * @since 0.5
	 */
	public static final Scope DEFAULT = new Scope(null, null, NO_LIMIT);

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

	/**
	 * Two scopes are considered equal, if the represent the same {@link Condition}, {@link Order} and limit
	 * @param obj
	 * @return whether the two scopes are equal
	 * @see Object#equals(java.lang.Object) 
	 * @since 0.7
	 */
	@Override
	public boolean equals( Object obj )
	{
		if(obj == null || !(obj instanceof Scope))
		{
			return false;
		}
		return Objects.equals( ((Scope)obj).getCondition(), getCondition()) &&
				Objects.equals( ((Scope)obj).getOrder(), getOrder()) && ((Scope)obj).getLimit() == limit;
	}	

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 11 * hash + Objects.hashCode( this.condition );
		hash = 11 * hash + Objects.hashCode( this.order );
		hash = 11 * hash + this.limit;
		return hash;
	}
}