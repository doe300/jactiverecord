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

import de.doe300.activerecord.migration.AutomaticMigration;
import de.doe300.activerecord.store.RecordStore;

/**
 * Timestamped ActiveRecords automatically maintain {@link RecordStore#COLUMN_CREATED_AT created_at} and  {@link RecordStore#COLUMN_UPDATED_AT updated_at}
 * values (of type java.sql.Timestamp). The <code>updated_at</code> will be updated every time, a attribute of the record is changed.
 * {@link AutomaticMigration} will generate this two columns automatically.
 * @author doe300
 */
public interface TimestampedRecord extends ActiveRecord
{
	/**
	 * @return the creation date of this entry
	 */
	public long getCreatedAt();
	
	/**
	 * @return the timestamp of the last update
	 */
	public long getUpdatedAt();
	
	/**
	 * Sets the {@link #getUpdatedAt()} timestamp to this instant
	 */
	public void touch();
}
