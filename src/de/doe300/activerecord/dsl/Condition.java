package de.doe300.activerecord.dsl;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.Map;
import java.util.function.Predicate;

/**
 *
 * @author doe300
 */
public interface Condition extends Predicate<Map<String, Object>>, SQLCommand
{
	public boolean hasWildcards();
	
	public Object[] getValues();
	
	public boolean test(ActiveRecord record);
}
