/*
 * The MIT License
 *
 * Copyright 2016 doe300.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.dsl.Condition;
import de.doe300.activerecord.dsl.Conditions;
import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.association.AssociationHelper;
import de.doe300.activerecord.record.association.HasManyAssociationSet;
import de.doe300.activerecord.record.association.generation.BelongsTo;
import de.doe300.activerecord.record.association.generation.Has;
import de.doe300.activerecord.record.association.generation.HasManyThrough;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Proxy-handler for automatically accessing associations for interface-methods
 * @author doe300
 * @since 0.9
 */
public class AssociationHandler implements ProxyHandler
{
	public AssociationHandler()
	{
	}

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return method.isAnnotationPresent( BelongsTo.class) || method.isAnnotationPresent( HasManyThrough.class) || 
				method.isAnnotationPresent( Has.class);
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler, Method method,
			Object[] args ) throws IllegalArgumentException
	{
		final RecordBase<?> base = record.getBase();
		final int primaryKey = record.getPrimaryKey();
		if(method.isAnnotationPresent( Has.class))
		{
			final Has has = method.getAnnotation( Has.class);
			if(has.isHasOne())
			{
				if(has.associationForeignKey().isEmpty())
				{
					return AssociationHelper.getHasOne( record, has.associatedType(), has.associationKey());
				}
				else
				{
					final Object key = base.getStore().getValue( base, primaryKey, has.associationForeignKey());
					return key == null ? null : AssociationHelper.getHasOne( key, base.getCore().getBase( has.associatedType()), has.associationKey());
				}
			}
			else
			{
				if(has.associationForeignKey().isEmpty())
				{
					return AssociationHelper.getHasManySet( record, has.associatedType(), has.associationKey());
				}
				else
				{
					final RecordBase<?> otherBase = base.getCore().getBase( has.associatedType() );
					final Object key = base.getStore().getValue( base, primaryKey, has.associationForeignKey());
					final Condition cond = Conditions.is( has.associationKey(), key);
					final Consumer<? extends ActiveRecord> setAsoc = (r) -> otherBase.getStore().setValue( otherBase, r.getPrimaryKey(), has.associationKey(), key);
					final Consumer<? extends ActiveRecord> unsetAsoc = (r) -> otherBase.getStore().setValue( otherBase, r.getPrimaryKey(), has.associationKey(), null);
					return new HasManyAssociationSet(otherBase, cond, null, setAsoc, unsetAsoc);
				}
			}
		}
		if(method.isAnnotationPresent( BelongsTo.class))
		{
			final BelongsTo belongsTo = method.getAnnotation( BelongsTo.class);
			final String columnName = !belongsTo.associationKey().isEmpty() ? belongsTo.associationKey() : belongsTo.name();
			if(belongsTo.associationForeignKey().isEmpty())
			{
				return AssociationHelper.getBelongsTo( record, belongsTo.associatedType(), columnName);
			}
			else
			{
				final RecordBase<?> otherBase = base.getCore().getBase( belongsTo.associatedType() );
				final Object key = base.getStore().getValue( base, primaryKey, columnName );
				return key == null ? null : otherBase.findFirstFor( belongsTo.associationForeignKey(), key);
			}
				
		}
		if(method.isAnnotationPresent( HasManyThrough.class))
		{
			final HasManyThrough hasMany = method.getAnnotation( HasManyThrough.class);
			return AssociationHelper.getHasManyThroughSet( record, hasMany.associatedType(), hasMany.mappingTable(), hasMany.mappingTableThisKey(),
					hasMany.mappingTableAssociatedKey() );
		}
		throw new IllegalArgumentException("Method is not handled by this handler");
	}

}
