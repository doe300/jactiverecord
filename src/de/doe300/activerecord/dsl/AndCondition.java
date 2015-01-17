package de.doe300.activerecord.dsl;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Class for SQL cond1 AND cond2 (AND cond3 ...) conditions
 * @author doe300
 */
public class AndCondition implements Condition
{
	private final Condition[] conditions;

	/**
	 * Default constructor, concatenating all arguments with AND
	 * @param conditions 
	 */
	public AndCondition( Condition... conditions )
	{
		this.conditions = Objects.requireNonNull( conditions );
	}
	
	@Override
	public boolean hasWildcards()
	{
		for(Condition con : conditions)
		{
			if(con.hasWildcards())
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Object[] getValues()
	{
		List<Object> values = new ArrayList<>(conditions.length);
		for(Condition cond:conditions)
		{
			values.addAll( Arrays.asList( cond.getValues()));
		}
		return values.toArray();
	}

	@Override
	public boolean test( ActiveRecord record )
	{
		for(Condition cond:conditions)
		{
			if(!cond.test( record ))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean test( Map<String, Object> t )
	{
		for(Condition cond:conditions)
		{
			if(!cond.test( t ))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public String toSQL()
	{
		return "("+ Arrays.stream( conditions ).map( (Condition c) -> c.toSQL() ).collect( Collectors.joining( ") AND ("))+")";
	}

}
