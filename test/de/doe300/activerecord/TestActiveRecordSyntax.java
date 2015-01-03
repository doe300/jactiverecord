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
