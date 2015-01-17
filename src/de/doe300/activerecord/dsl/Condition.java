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
	/**
	 * The wildcards (<code>?</code>) are replaced by the conditions {@link #getValues() values}.
	 * @return whether this conditions uses wildcards in its SQL statement
	 */
	public boolean hasWildcards();
	
	/**
	 * @return the values to match
	 */
	public Object[] getValues();
	
	/**
	 * This method acts as a {@link Predicate Predicate&lt;ActiveRecord&gt;}
	 * @param record
	 * @return whether the <code>record</code> matches this condition
	 */
	public boolean test(ActiveRecord record);
}
