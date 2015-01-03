package de.doe300.activerecord.dsl;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author doe300
 */
public class OrCondition implements Condition
{
	private final Condition[] conditions;

	public OrCondition( Condition... conditions )
	{
		this.conditions = Objects.requireNonNull( conditions);
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
			if(cond.test( record ))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean test( Map<String, Object> t )
	{
		for(Condition cond:conditions)
		{
			if(cond.test( t ))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public String toSQL()
	{
		return "("+ Arrays.stream( conditions ).map( (Condition c) -> c.toSQL() ).collect( Collectors.joining( ") OR ("))+")";
	}

}
