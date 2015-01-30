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
package de.doe300.activerecord;

import de.doe300.activerecord.dsl.Comparison;
import de.doe300.activerecord.dsl.SimpleCondition;
import java.sql.SQLException;

/**
 * Just some code to test the feeling of the syntax
 * @author doe300
 */
public class TestActiveRecordSyntax
{
	public static void main(String[] args) throws SQLException, Exception
	{
		//0, create new core
		RecordCore core = RecordCore.fromDatabase( null, true );
		//1. create new recordbase
		RecordBase<TestInterface> base = core.getBase( TestInterface.class);
		//2. get record
		TestInterface el = base.getRecord(1);
		//3. change & store record
		el.setAge( 23);
		el.save();
		//4. destroy record
		el.destroy();

		//5. get all for attribute
		base.findFor( "age", 23).forEach( (TestInterface i)-> System.err.println( i.getName() ));
		
		//6. query
		base.where( new SimpleCondition("name", "Max", Comparison.IS));
	}
}
