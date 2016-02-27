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
package de.doe300.activerecord.dsl.joins;

/**
 * The type of JOIN to perform
 * @since 0.7
 */
public enum JoinType
{
	/**
	 * A simple SQL (INNER) JOIN, returning all rows where the condition is met
	 */
	INNER_JOIN,
	/**
	 * A LEFT (OUTER) JOIN, returning all rows from the left (outer) table and matching rows from the right (inner)
	 * table, otherwise fills the right table's columns with NULLs
	 */
	LEFT_OUTER_JOIN,
	/**
	 * A RIGHT (OUTER) JOIN returns all rows from the right (outer) table and matching rows from the left table. Fills
	 * the left table's column with NULLs if there are not matching rows
	 */
	RIGHT_OUTER_JOIN,
	/**
	 * A FULL OUTER JOIN, returning all rows from the left and right table, combining LEFT JOIN and RIGHT JOIN
	 */
	FULL_OUTER_JOIN
}
