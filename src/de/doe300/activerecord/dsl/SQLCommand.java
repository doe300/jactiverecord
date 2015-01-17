package de.doe300.activerecord.dsl;

/**
 *
 * @author doe300
 */
public interface SQLCommand
{
	/**
	 * @return the sQL representation of this statement
	 */
	public String toSQL();
}
