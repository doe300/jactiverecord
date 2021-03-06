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
package de.doe300.activerecord.migration;

/**
 *
 * @author doe300
 */
public interface Migration
{
	/**
	 * Applies this migration
	 * 
	 * @return whether the migration was applied
	 * @throws Exception
	 */
	public boolean apply() throws Exception;

	/**
	 * Reverts the changes from this migration
	 * @return whether the migration was reverted
	 * @throws Exception
	 */
	public boolean revert() throws Exception;

	/**
	 * Update the data-structure or executes the <code>update</code>statement, depending on the type of migration
	 * @param dropColumns whether to drop columns, which are no longer present in the record-type
	 * @return whether the update was successful
	 * @throws Exception
	 */
	public boolean update(final boolean dropColumns) throws Exception;
}
