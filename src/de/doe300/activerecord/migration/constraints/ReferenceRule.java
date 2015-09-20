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
package de.doe300.activerecord.migration.constraints;

import de.doe300.activerecord.migration.Attribute;
import de.doe300.activerecord.migration.AutomaticMigration;
import javax.annotation.Syntax;

/**
 * Rule for FOREIGN KEYS for ON DELETE or ON UPDATE clauses
 * @author doe300
 *
 * @see Attribute
 * @see AutomaticMigration
 */
public enum ReferenceRule
{
	/**
	 * Do nothing, default
	 */
	NONE
	{
		@Override
		public String toSQL( final String onAction )
		{
			return "";
		}
	},
	/**
	 * CASCADE.
	 * Associated columns will be updated (ON UPDATE) or deleted (ON DELETE)
	 */
	CASCADE
	{
		@Override
		public String toSQL( final String onAction )
		{
			return " ON "+onAction+" CASCADE";
		}
	},
	/**
	 * SET DEFAULT.
	 * Associated cells are set to the DEFAULT-value
	 */
	SET_DEFAULT
	{
		@Override
		public String toSQL( final String onAction )
		{
			return " ON "+onAction+" SET DEFAULT";
		}
	},
	/**
	 * SET NULL.
	 * Associated cells will be set to NULL
	 */
	SET_NULL
	{
		@Override
		public String toSQL( final String onAction )
		{
			return " ON "+onAction+" SET NULL";
		}
	};

	/**
	 * The SQL UPDATE action
	 */
	public static final String ACTION_UPDATE = "UPDATE";

	/**
	 * The SQL DELETE action
	 */
	public static final String ACTION_DELETE = "DELETE";

	/**
	 * @param onAction
	 * @return the SQL clause
	 */
	@Syntax(value = "SQL")
	public abstract String toSQL(String onAction);
}
