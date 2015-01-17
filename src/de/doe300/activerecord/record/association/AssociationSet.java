package de.doe300.activerecord.record.association;

import de.doe300.activerecord.FinderMethods;
import de.doe300.activerecord.record.ActiveRecord;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base interface for association-based sets
 * @author doe300
 * @param <T>
 */
public interface AssociationSet<T extends ActiveRecord> extends Set<T>, FinderMethods<T>
{
	@Override
	public default Stream<T> findAll()
	{
		return stream();
	}
}
