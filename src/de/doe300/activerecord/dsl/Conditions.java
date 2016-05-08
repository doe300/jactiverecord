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
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper class to ease the creation of {@link Condition}
 * @author doe300
 * @since 0.8
 * @see Condition
 */
public class Conditions
{
	@Nonnull
	public static Condition isTrue(@Nonnull final String attributeName)
	{
		return new SimpleCondition(attributeName, null, Comparison.TRUE);
	}
	
	@Nonnull
	public static Condition isTrue(@Nonnull final SQLFunction<?, ?> attributeFunc)
	{
		return new SimpleCondition(attributeFunc, null, Comparison.TRUE);
	}
	
	@Nonnull
	public static Condition isNull(@Nonnull final String attributeName)
	{
		return new SimpleCondition(attributeName, null, Comparison.IS_NULL);
	}
	
	@Nonnull
	public static Condition isNull(@Nonnull final SQLFunction<?, ?> attributeFunc)
	{
		return new SimpleCondition(attributeFunc, null, Comparison.IS_NULL);
	}
	
	@Nonnull
	public static Condition isNotNull(@Nonnull final String attributeName)
	{
		return new SimpleCondition(attributeName, null, Comparison.IS_NOT_NULL);
	}
	
	@Nonnull
	public static Condition isNotNull(@Nonnull final SQLFunction<?, ?> attributeFunc)
	{
		return new SimpleCondition(attributeFunc, null, Comparison.IS_NOT_NULL);
	}
	
	@Nonnull
	public static Condition is(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.IS );
	}
	
	@Nonnull
	public static Condition is(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.IS );
	}
	
	@Nonnull
	public static Condition isNot(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.IS_NOT );
	}
	
	@Nonnull
	public static Condition isNot(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.IS_NOT );
	}
	
	@Nonnull
	public static Condition isLike(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.LIKE );
	}
	
	@Nonnull
	public static Condition isLike(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.LIKE );
	}
	
	@Nonnull
	public static Condition isIn(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.IN );
	}
	
	@Nonnull
	public static Condition isIn(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.IN );
	}
	
	@Nonnull
	public static Condition isLarger(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.LARGER );
	}
	
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isLarger(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.LARGER );
	}
	
	@Nonnull
	public static Condition isLargerEquals(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.LARGER_EQUALS );
	}
	
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isLargerEquals(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.LARGER_EQUALS );
	}
	
	@Nonnull
	public static Condition isSmaller(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.SMALLER );
	}
	
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isSmaller(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.SMALLER );
	}
	
	@Nonnull
	public static Condition isSmallerEquals(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.SMALLER_EQUALS );
	}
	
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isSmallerEquals(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.SMALLER_EQUALS );
	}

	/**
	 * Combines the <code>conds</code> and optimizes according to the following rules:
	 * <ul>
	 * <li>Removes all <code>null</code>-conditions</li>
	 * <li>Unrolls all children AND-conditions, because <code>a AND (b AND c)</code> is the same as <code>a AND b AND c</code></li>
	 * <li>If any condition results in the SQL-symbol <code>TRUE</code>, the condition does not contribute to the result and can be removed</li>
	 * <li>Skips conditions which are already in the list</li>
	 * <li>Returns the single condition, if only one passes all other tests</li>
	 * </ul>
	 *
	 * @param conds
	 * @return the combined Condition
	 */
	@Nonnull
	public static Condition and(@Nullable final Condition... conds )
	{
		if ( conds == null || conds.length == 0 )
		{
			throw new IllegalArgumentException();
		}
		final ArrayList<Condition> list = new ArrayList<>( conds.length );
		for ( final Condition cond : conds )
		{
			//remove nulls
			if ( cond == null )
			{
				continue;
			}
			//unroll ANDs
			if ( cond instanceof AndCondition )
			{
				list.addAll( Arrays.asList( (( AndCondition ) cond).getConditions() ) );
				continue;
			}
			//remove non-false rules
			if ( cond instanceof SimpleCondition && (( SimpleCondition ) cond).getComparison() == Comparison.TRUE )
			{
				continue;
			}
			//if condition is already in list, skip
			if ( list.contains( cond ) )
			{
				continue;
			}
			list.add( cond );
		}
		if ( list.isEmpty() )
		{
			throw new IllegalArgumentException( "Cant AND null conditions" );
		}
		if ( list.size() == 1 )
		{
			return list.get( 0 );
		}
		return new AndCondition( list.toArray( new Condition[ list.size() ] ) );
	}

	/**
	 * Combines the <code>conds</code> and optimizes according to the following rules:
	 * <ul>
	 * <li>Removes all <code>null</code>-conditions</li>
	 * <li>Unrolls all children OR-conditions, because <code>a OR (b OR c)</code> is the same as <code>a OR b OR c</code></li>
	 * <li>If any condition produces the SQL-symbol <code>TRUE</code> this condition will always be <code>true</code> and therefore any other condition can be discarded</li>
	 * <li>Skips conditions which are already in the list</li>
	 * <li>Returns the single condition, if only one passes all other tests</li>
	 * </ul>
	 *
	 * @param conds
	 * @return the combined Condition
	 */
	@Nonnull
	public static Condition or(@Nullable Condition... conds )
	{
		if ( conds == null || conds.length == 0 )
		{
			throw new IllegalArgumentException();
		}
		ArrayList<Condition> list = new ArrayList<>( conds.length );
		for ( Condition cond : conds )
		{
			//clears nulls
			if ( cond == null )
			{
				continue;
			}
			//unrolls or-conditions
			if ( cond instanceof OrCondition )
			{
				list.addAll( Arrays.asList( (( OrCondition ) cond).getConditions() ) );
				continue;
			}
			//check for non-false condition
			if ( cond instanceof SimpleCondition && (( SimpleCondition ) cond).getComparison() == Comparison.TRUE )
			{
				//this condition is non-false, no other conditions matter
				return cond;
			}
			//if condition is already in list, skip
			if ( list.contains( cond ) )
			{
				continue;
			}
			list.add( cond );
		}
		if ( list.isEmpty() )
		{
			throw new IllegalArgumentException( "Can't OR null conditions" );
		}
		if ( list.size() == 1 )
		{
			return list.get( 0 );
		}
		return new OrCondition( list.toArray( new Condition[ list.size() ] ) );
	}
	
	/**
	 * Combines the <code>conditions</code> and optimizes according to the following rules:
	 * <ul>
	 * <li>Removes all <code>null</code>-conditions</li>
	 * <li>Unrolls all children XOR-conditions, because <code>a XOR (b XOR c)</code> is the same as <code>a XOR b XOR c</code></li>
	 * <li>Skips conditions which are already in the list</li>
	 * <li>Returns the single condition, if only one passes all other tests</li>
	 * </ul>
	 * 
	 * @param conditions
	 * @return the XOR'ed conditions
	 * @since 0.8
	 */
	@Nonnull
	public static Condition xor(@Nullable final Condition... conditions)
	{
		if ( conditions == null || conditions.length == 0 )
		{
			throw new IllegalArgumentException();
		}
		ArrayList<Condition> list = new ArrayList<>( conditions.length );
		for ( Condition cond : conditions )
		{
			//clears nulls
			if ( cond == null )
			{
				continue;
			}
			//unrolls or-conditions
			if ( cond instanceof XorCondition )
			{
				list.addAll( Arrays.asList( (( XorCondition ) cond).getConditions() ) );
				continue;
			}
			//if condition is already in list, skip
			if ( list.contains( cond ) )
			{
				continue;
			}
			list.add( cond );
		}
		if ( list.isEmpty() )
		{
			throw new IllegalArgumentException( "Can't XOR null conditions" );
		}
		if ( list.size() == 1 )
		{
			return list.get( 0 );
		}
		return new XorCondition( list.toArray( new Condition[ list.size() ] ) );
	}

	/**
	 * Inverts the <code>cond</code>.
	 *
	 * This method optimizes by unwrapping a twice inverted condition, because <code>NOT(NOT(a))</code> is the same as <code>a</code>
	 *
	 * @param cond
	 * @return the inverted Condition
	 */
	@Nonnull
	public static Condition invert(@Nullable final Condition cond )
	{
		if ( cond == null )
		{
			throw new IllegalArgumentException();
		}
		if ( cond instanceof InvertedCondition )
		{
			return cond.negate();
		}
		return new InvertedCondition( cond );
	}
	
	private Conditions()
	{
		
	}
}
