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
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Helper class to ease the creation of {@link Condition}
 * @author doe300
 * @since 0.8
 * @see Condition
 */
public final class Conditions
{
	/**
	 * @return a condition which is always true
	 */
	@Nonnull
	public static Condition isTrue()
	{
		return new SimpleCondition("", null, Comparison.TRUE);
	}

	/**
	 * @param attributeName
	 * @return a condition whether the attribute-value is null
	 */
	@Nonnull
	public static Condition isNull(@Nonnull final String attributeName)
	{
		return new SimpleCondition(attributeName, null, Comparison.IS_NULL);
	}

	/**
	 * @param attributeFunc
	 * @return a condition whether the result of the function is null
	 */
	@Nonnull
	public static Condition isNull(@Nonnull final SQLFunction<?, ?> attributeFunc)
	{
		return new SimpleCondition(attributeFunc, null, Comparison.IS_NULL);
	}

	/**
	 * @param attributeName
	 * @return a condition whether the attribute-value is not null
	 */
	@Nonnull
	public static Condition isNotNull(@Nonnull final String attributeName)
	{
		return new SimpleCondition(attributeName, null, Comparison.IS_NOT_NULL);
	}

	/**
	 * @param attributeFunc
	 * @return a condition whether the result of the function is not null
	 */
	@Nonnull
	public static Condition isNotNull(@Nonnull final SQLFunction<?, ?> attributeFunc)
	{
		return new SimpleCondition(attributeFunc, null, Comparison.IS_NOT_NULL);
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is the given vale
	 */
	@Nonnull
	public static Condition is(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.IS );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the result of the function is the given value
	 */
	@Nonnull
	public static Condition is(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.IS );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is not the given value
	 */
	@Nonnull
	public static Condition isNot(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.IS_NOT );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the result of the function is not the given
	 *         value
	 */
	@Nonnull
	public static Condition isNot(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.IS_NOT );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is 'LIKE' the given value
	 *         (according to SQL LIKE method)
	 */
	@Nonnull
	public static Condition isLike(@Nonnull final String attributeName, @Nullable final String value)
	{
		return new SimpleCondition(attributeName, value, Comparison.LIKE );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the result of the function is 'LIKE' the
	 *         given value
	 */
	@Nonnull
	public static Condition isLike(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final String value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.LIKE );
	}

	/**
	 * @param attributeName
	 * @param value
	 *            The container to check for contents
	 * @return a condition whether the attribute-value is 'IN' the value
	 */
	@Nonnull
	public static Condition isIn(@Nonnull final String attributeName, @Nullable final Object value)
	{
		return new SimpleCondition(attributeName, value, Comparison.IN );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 *            The container to check for contents
	 * @return a condition whether the function-value is 'IN' the value
	 */
	@Nonnull
	public static Condition isIn(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.IN );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is larger than the given
	 *         value
	 */
	@Nonnull
	public static Condition isLarger(@Nonnull final String attributeName, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeName, value, Comparison.LARGER );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the function return-value is larger than the
	 *         given value
	 */
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isLarger(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.LARGER );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is larger or equals the
	 *         given value
	 */
	@Nonnull
	public static Condition isLargerEquals(@Nonnull final String attributeName, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeName, value, Comparison.LARGER_EQUALS );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the function return-value is larger or equals
	 *         the given value
	 */
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isLargerEquals(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.LARGER_EQUALS );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is smaller than the given
	 *         value
	 */
	@Nonnull
	public static Condition isSmaller(@Nonnull final String attributeName, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeName, value, Comparison.SMALLER );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the function-value is smaller than the given
	 *         value
	 */
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isSmaller(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.SMALLER );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @return a condition whether the attribute-value is smaller or equals the
	 *         given value
	 */
	@Nonnull
	public static Condition isSmallerEquals(@Nonnull final String attributeName, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeName, value, Comparison.SMALLER_EQUALS );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @return a condition whether the function-value is smaller or equals the
	 *         given value
	 */
	@Nonnull
	public static <T extends Comparable<? super T>> Condition isSmallerEquals(@Nonnull final SQLFunction<?, T> attributeFunc, @Nullable final Comparable<?> value)
	{
		return new SimpleCondition(attributeFunc, value, Comparison.SMALLER_EQUALS );
	}

	/**
	 * @param attributeName
	 * @param value
	 * @param comparison
	 * @return a condition whether the given comparison is true for the
	 *         attribute-value and the given value
	 */
	@Nonnull
	public static Condition compare(@Nonnull final String attributeName, @Nullable final Object value, @Nonnull final Comparison comparison)
	{
		return new SimpleCondition(attributeName, value, comparison );
	}

	/**
	 * @param attributeFunc
	 * @param value
	 * @param comparison
	 * @return a condition whether the given comparison is true for the
	 *         function-value and the given value
	 */
	@Nonnull
	public static Condition compare(@Nonnull final SQLFunction<?, ?> attributeFunc, @Nullable final Object value, @Nonnull final Comparison comparison)
	{
		return new SimpleCondition(attributeFunc, value, comparison );
	}

	/**
	 * Wrapper for {@link #and(java.util.Collection) }
	 * @param conds
	 * @return the combined Condition
	 * @see #and(java.util.Collection)
	 */
	@Nonnull
	public static Condition and(@Nullable final Condition... conds )
	{
		return Conditions.and( Arrays.asList( conds));
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
	 * @return the combined Condition or <code>null</code> if all conditions are tautologies
	 * @since 0.8
	 */
	@Nullable
	public static Condition and(@Nullable final Collection<Condition> conds )
	{
		if ( conds == null || conds.isEmpty() )
		{
			throw new IllegalArgumentException();
		}
		final ArrayList<Condition> list = new ArrayList<>( conds.size() );
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
			//no condition
			return null;
		}
		if ( list.size() == 1 )
		{
			return list.get( 0 );
		}
		return new AndCondition( list.toArray( new Condition[ list.size() ] ) );
	}

	/**
	 * Wrapper for {@link #or(java.util.Collection) }
	 * @param conds
	 * @return the combined Condition
	 * @see #or(java.util.Collection)
	 */
	@Nonnull
	public static Condition or(@Nullable final Condition... conds )
	{
		return Conditions.or( Arrays.asList( conds));
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
	 * @return the combined Condition or <code>null</code> if all conditions are tautologies
	 * @since 0.8
	 */
	@Nullable
	public static Condition or(@Nullable final Collection<Condition> conds )
	{
		if ( conds == null || conds.isEmpty() )
		{
			throw new IllegalArgumentException();
		}
		final ArrayList<Condition> list = new ArrayList<>( conds.size() );
		for ( final Condition cond : conds )
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
			//no conditions
			return null;
		}
		if ( list.size() == 1 )
		{
			return list.get( 0 );
		}
		return new OrCondition( list.toArray( new Condition[ list.size() ] ) );
	}

	/**
	 * Wrapper for {@link #xor(java.util.Collection) }
	 * @param conditions
	 * @return the combined Condition
	 * @see #xor(java.util.Collection)
	 * @since 0.8
	 */
	@Nonnull
	public static Condition xor(@Nullable final Condition... conditions)
	{
		return Conditions.xor( Arrays.asList( conditions));
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
	public static Condition xor(@Nullable final Collection<Condition> conditions)
	{
		if ( conditions == null || conditions.isEmpty() )
		{
			throw new IllegalArgumentException();
		}
		final ArrayList<Condition> list = new ArrayList<>( conditions.size() );
		for ( final Condition cond : conditions )
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
