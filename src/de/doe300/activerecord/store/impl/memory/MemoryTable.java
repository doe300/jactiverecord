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
package de.doe300.activerecord.store.impl.memory;

import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Order;
import de.doe300.activerecord.scope.Scope;
import de.doe300.activerecord.store.NoSuchAttributeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author doe300
 * @since 0.3
 */
class MemoryTable
{
	private final String primaryColumn;
	private final SortedMap<String, MemoryColumn> columns;
	private final SortedMap<Integer, MemoryRow> data;
	
	//cache values
	private Map<String, Class<?>> columnTypes;
	private int nextRowIndex = 0;

	MemoryTable(@Nonnull final String primaryColumn, @Nonnull final MemoryColumn[] columns)
	{
		this.primaryColumn = primaryColumn;
		this.columns = new TreeMap<>();
		Arrays.stream( columns).forEach( (MemoryColumn c) -> this.columns.put( c.getName(), c));
		this.data = new TreeMap<>();
	}
	
	@Nonnull
	public String getPrimaryColumn()
	{
		return primaryColumn;
	}
	
	@Nonnull
	public Set<String> getColumnNames()
	{
		if(columnTypes == null)
		{
			getColumnTypes();
		}
		return columnTypes.keySet();
	}
	
	@Nonnull
	public Map<String, Class<?>> getColumnTypes()
	{
		if(columnTypes == null)
		{
			columnTypes = new HashMap<>(columns.size());
			for(Map.Entry<String, MemoryColumn> column : columns.entrySet())
			{
				columnTypes.put( column.getKey(), column.getValue().getType());
			}
		}
		return columnTypes;
	}
	
	private Object checkColumn(@Nonnull final String columnName, @Nullable final Object value)
	{
		if(!columns.containsKey( columnName))
		{
			throw new NoSuchAttributeException("MemoryTable", columnName);
		}
		return columns.get( columnName).checkValue( value );
	}
	
	public boolean putValue(@Nonnegative int primaryKey, @Nonnull final String columnName, @Nullable final Object value)
	{
		if(!data.containsKey( primaryKey))
		{
			return false;
		}
		if(primaryColumn.equals( columnName))
		{
			return false;
		}
		data.get(primaryKey ).putRowValue( columnName, checkColumn( columnName, value ) );
		return true;
	}
	
	public boolean putValues(@Nonnegative int primaryKey, @Nonnull final Map<String, Object> values)
	{
		MemoryRow row = data.get(primaryKey );
		if(row == null)
		{
			return false;
		}
		boolean rowUpdated = false;
		for(Map.Entry<String, Object> e : values.entrySet())
		{
			if(primaryColumn.equals( e.getKey()))
			{
				continue;
			}
			row.putRowValue( e.getKey(), checkColumn( e.getKey(), e.getValue()));
			rowUpdated = true;
		}
		return rowUpdated;
	}
	
	public boolean containsValue(@Nonnegative int primaryKey, @Nonnull final String columnName)
	{
		return data.containsKey( primaryKey) && data.get( primaryKey).getRowMap().containsKey( columnName);
	}
	
	@Nullable
	public Object getValue(@Nonnegative int primaryKey, @Nonnull final String columnName)
	{
		if(!columns.containsKey( columnName))
		{
			throw new NoSuchAttributeException("MemoryTable", columnName);
		}
		if(!data.containsKey( primaryKey))
		{
			return null;
		}
		return data.get(primaryKey ).getRowValue( columnName );
	}
	
	@Nonnull
	public Map<String, Object> getValues(@Nonnegative int primaryKey, @Nonnull final String[] columnNames)
	{
		if(!data.containsKey( primaryKey))
		{
			return Collections.emptyMap();
		}
		Map<String, Object> values = new HashMap<>(columnNames.length);
		for(String columnName : columnNames)
		{
			values.put( columnName, getValue( primaryKey, columnName));
		}
		return values;
	}
	
	@Nonnegative
	public int insertRow()
	{
		int rowIndex = nextRowIndex;
		nextRowIndex++;
		data.put( rowIndex, new MemoryRow(columns.keySet(), primaryColumn, rowIndex));
		return rowIndex;
	}
	
	public void removeRow(@Nonnegative int primaryKey)
	{
		data.remove( primaryKey);
	}
	
	@Nonnull
	public Stream<Object> getValues(@Nonnull final String column, @Nonnull final String condColumn, @Nullable final Object condValue)
	{
		return StreamSupport.stream( new Spliterator<Object>()
		{
			//i.e. for HasManyThroughAssociationSet#removeAll(), elements are removed while map is traversed
			//so we need to copy the indices and traverse the copy
			private final Iterator<Integer> rowKeys = new TreeSet<Integer>(data.keySet()).iterator();
			@Override
			public boolean tryAdvance(final Consumer<? super Object> action )
			{
				int rowIndex;
				while(rowKeys.hasNext())
				{
					rowIndex = rowKeys.next();
					if(Objects.equals( data.get( rowIndex).getRowValue( condColumn), condValue))
					{
						action.accept( data.get( rowIndex).getRowValue( column) );
						return true;
					}
				}
				return false;
			}

			@Override
			public Spliterator<Object> trySplit()
			{
				return null;
			}

			@Override
			public long estimateSize()
			{
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics()
			{
				return Spliterator.IMMUTABLE|Spliterator.ORDERED;
			}
		},false);
	}
	
	@Nullable
	public Map.Entry<Integer, MemoryRow> findFirstRow(@Nonnull final Scope scope)
	{
		Stream<Map.Entry<Integer, MemoryRow>> result = findAllRows( scope.getCondition());
		if(scope.getOrder() != null)
		{
			result = sortResult( scope.getOrder(), result);
		}
		Optional<Map.Entry<Integer, MemoryRow>> first = result.findFirst();
		return first.isPresent() ? first.get() : null;
	}
	
	@Nonnull
	public Stream<Map.Entry<Integer, MemoryRow>> findAllRows(@Nonnull final Scope scope)
	{
		Stream<Map.Entry<Integer, MemoryRow>> result = findAllRows( scope.getCondition());
		if(scope.getOrder() != null)
		{
			result = sortResult( scope.getOrder(), result);
		}
		if(scope.getLimit() != Scope.NO_LIMIT)
		{
			return result.limit( scope.getLimit());
		}
		return result;
	}
	
	@Nonnull
	private static Stream<Map.Entry<Integer, MemoryRow>> sortResult(@Nonnull final Order order, @Nonnull final Stream<Map.Entry<Integer, MemoryRow>> result)
	{
		return result.sorted( (Map.Entry<Integer, MemoryRow> e1, Map.Entry<Integer, MemoryRow> e2) -> 
		{
			return order.compare( e1.getValue().getRowMap(), e2.getValue().getRowMap());
		});
	}
	
	@Nonnull
	private Stream<Map.Entry<Integer, MemoryRow>> findAllRows(@Nullable final Condition cond)
	{
		return StreamSupport.stream( new Spliterator<Map.Entry<Integer, MemoryRow>>()
		{
			private final Iterator<Map.Entry<Integer, MemoryRow>> rowKeys;
			{
				//to prevent ConcurrentModificationException while copying the entries
				synchronized(data)
				{
					rowKeys = new HashSet<>(data.entrySet()).iterator();
				}
			}
			@Override
			public boolean tryAdvance(final Consumer<? super Map.Entry<Integer, MemoryRow>> action )
			{
				Map.Entry<Integer, MemoryRow> currentRow;
				while(rowKeys.hasNext())
				{
					currentRow = rowKeys.next();
					if(cond == null || cond.test( currentRow.getValue().getRowMap()))
					{
						action.accept( currentRow );
						return true;
					}
				}
				return false;
			}

			@Override
			public Spliterator<Map.Entry<Integer, MemoryRow>> trySplit()
			{
				return null;
			}

			@Override
			public long estimateSize()
			{
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics()
			{
				return Spliterator.IMMUTABLE|Spliterator.ORDERED|Spliterator.DISTINCT|Spliterator.NONNULL;
			}
		},false);
	}
}
