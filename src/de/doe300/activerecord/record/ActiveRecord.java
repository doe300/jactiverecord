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
package de.doe300.activerecord.record;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.store.RecordStore;

/**
 *
 * @author doe300
 */
public interface ActiveRecord extends Comparable<ActiveRecord>
{
	/**
	 * Saves all cached values to the record-store
	 * @return whether this record was saved and data was changed
	 * @see #isSynchronized() 
	 */
	public default boolean save()
	{
		return getBase().save( this );
	}
	
	/**
	 * @return the unique primary key for this record
	 */
	public int getPrimaryKey();
	
	/**
	 * @return whether the attributes in this object are synchronized with the underlying {@link RecordStore}
	 */
	public default boolean isSynchronized()
	{
		return getBase().hasRecord( getPrimaryKey()) && getBase().isSynchronized( this );
	}

	@Override
	public default int compareTo( ActiveRecord o )
	{
		return Integer.compare( getPrimaryKey(), o.getPrimaryKey());
	}

	/**
	 * @return the base for this record
	 */
	public RecordBase<?> getBase();
	
	/**
	 * Destroys this record and its storage-entity
	 */
	public default void destroy()
	{
		getBase().destroy( getPrimaryKey() );
	}
	
	/**
	 * @return whether this record is backed by an entry in the {@link RecordStore}
	 */
	public default boolean inRecordStore()
	{
		return getBase().hasRecord( getPrimaryKey());
	}
	
	/**
	 * Discards all changes made to this record so.
	 * @see #save()
	 * @see #isSynchronized() 
	 */
	public default void reload()
	{
		getBase().reload( this );
	}
}
