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
import de.doe300.activerecord.record.validation.ValidatedRecord;

/**
 * Callbacks are executed at before/after specific actions
 * @author doe300
 */
public interface RecordCallbacks extends ActiveRecord
{
	/**
	 * This method is called right after a new record was created.
	 * Use this callback to initialize default-values.
	 * @see RecordBase#createRecord()
	 * @see RecordBase#createRecord(java.util.Map)
	 */
	public default void afterCreate()
	{
		// do nothing
	}

	/**
	 * This callback is called after the first load of a record (form the underlying record-store)
	 * @see RecordBase#getRecord(int)
	 */
	public default void afterLoad()
	{
		// do nothing
	}

	/**
	 * This callback is executed before saving a record.
	 * This callback is called before {@link ValidatedRecord#validate()}, if the record-type is {@link RecordBase#isValidated() validated}
	 * @see #save()
	 * @see RecordBase#save(de.doe300.activerecord.record.ActiveRecord)
	 * @see RecordBase#saveAll()
	 */
	public default void beforeSave()
	{
		// do nothing
	}

	/**
	 * This callback is called before removing the record from the record-store.
	 * Purpose of this callback is i.e. to clear associated records or entries from association-tables
	 * @see #destroy()
	 * @see RecordBase#destroy(int)
	 */
	public default void onDestroy()
	{
		// do nothing
	}
}
