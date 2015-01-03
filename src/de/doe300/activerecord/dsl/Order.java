package de.doe300.activerecord.dsl;

import de.doe300.activerecord.record.ActiveRecord;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

/**
 * An object representing the SQL ORDER BY-Clause
 * @author doe300
 */
public class Order implements Comparator<Map<String,Object>>, SQLCommand
{
	private final String[] columns;
	private final OrderType types[];

	public Order( String[] columns, OrderType[] types )
	{
		this.columns = columns;
		this.types = levelTypes( columns.length, types );
	}

	public Order(String column, OrderType type)
	{
		this.columns = new String[]{column};
		this.types = new OrderType[]{type};
	}
	
	public static Order fromSQLString(String sqlOrderBy)
	{
		if(sqlOrderBy==null|| sqlOrderBy.isEmpty())
		{
			return null;
		}
		String stmt = sqlOrderBy.contains( "ORDER BY") ? sqlOrderBy.substring( sqlOrderBy.indexOf( "ORDER BY")+"ORDER BY".length()) : sqlOrderBy;
		String[] parts = stmt.trim().split( "\\,");
		String[] columns = new String[parts.length];
		OrderType[] types = new OrderType[parts.length];
		String[] tmp;
		for(int i=0;i<parts.length;i++)
		{
			tmp = parts[i].trim().split( "\\s+");
			if(tmp.length > 1)
			{
				columns[i] = tmp[0];
				types[i] = tmp[1].equalsIgnoreCase( "DESC") ? OrderType.DESCENDING : OrderType.ASCENDING;
			}
			else
			{
				columns[i] = tmp[0];
				types[i] = OrderType.ASCENDING;
			}
		}
		return new Order(columns, types );
	}
	
	private OrderType[] levelTypes(int num, OrderType[] types)
	{
		if(num==types.length)
		{
			return types;
		}
		OrderType[] newTypes = new OrderType[ num ];
		Arrays.fill( newTypes, OrderType.ASCENDING);
		System.arraycopy( types, 0, newTypes, 0, types.length);
		return newTypes;
	}
	
	@Override
	public int compare( Map<String, Object> o1, Map<String, Object> o2 )
	{
		int index = 0;
		int compare = 0;
		Object val1;
		while(compare== 0 && index < columns.length)
		{
			val1 = o1.get( columns[index]);
			if(val1 instanceof Comparable)
			{
				compare = ((Comparable)val1).compareTo( o2.get( columns[index]));
			}
			index++;
		}
		return compare;
	}

	@Override
	public String toSQL()
	{
		StringBuilder sb = new StringBuilder(100);
		for(int i=0;i<columns.length;i++)
		{
			sb.append( ", ").append( columns[i]).append( " ").append( types[i].toSQL());
		}
		//deletes first ', '
		sb.delete( 0, 2 );
		return sb.toString();
	}

	public Comparator<ActiveRecord> toRecordComparator()
	{
		return new Comparator<ActiveRecord>()
		{
			@Override
			public int compare( ActiveRecord o1, ActiveRecord o2 )
			{
				Map<String,Object> map1 = o1.getBase().getStore().getValues( o1.getBase(), o1.getPrimaryKey(), columns);
				Map<String,Object> map2 = o2.getBase().getStore().getValues( o2.getBase(), o2.getPrimaryKey(), columns);
				return Order.this.compare( map1, map2 );
			}
		};
	}

	public static enum OrderType implements SQLCommand
	{
		ASCENDING {

			@Override
			public String toSQL()
			{
				return "ASC";
			}
		},
		DESCENDING {

			@Override
			public String toSQL()
			{
				return "DESC";
			}
		};
		
		@Override
		public abstract String toSQL();
	}
}
