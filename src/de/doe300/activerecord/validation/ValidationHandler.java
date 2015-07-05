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
package de.doe300.activerecord.validation;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This handler dynamically creates the record's validation-method from its {@link Validate} annotations.
 * @author doe300
 * @see Validate
 * @see ValidatedRecord
 */
public class ValidationHandler implements ProxyHandler
{
	private static final Map<Class<? extends ActiveRecord>, Predicate<ActiveRecord>> validationChecks = new HashMap<>(10);
	private static final Map<Class<? extends ActiveRecord>, Consumer<ActiveRecord>> validationEnforcements = new HashMap<>(10);

	@Override
	public boolean handlesMethod( ActiveRecord record, Method method, Object[] args ) throws IllegalArgumentException
	{
		return record instanceof ValidatedRecord && method.getDeclaringClass() == ValidatedRecord.class;
	}

	@Override
	public <T extends ActiveRecord> Object invoke( ActiveRecord record, RecordHandler<T> handler,
			Method method, Object[] args ) throws IllegalArgumentException
	{
		try
		{
			if(method.equals( ValidatedRecord.class.getMethod( "isValid")))			
			{
				return getOrCreateValidationCheck( handler.getRecordType() ).test( record );
			}
			if(method.equals( ValidatedRecord.class.getMethod( "validate")))
			{
				getOrCreateValidationEnforcer( handler.getRecordType()).accept( record );
			}
			throw new IllegalArgumentException("Method is not handled by this handler");
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			throw new IllegalArgumentException(ex);
		}
	}
	
	private Predicate<ActiveRecord> getOrCreateValidationCheck(Class<? extends ActiveRecord> recordType)
	{
		if(!validationChecks.containsKey( recordType))
		{
			Validate[] validations = recordType.getAnnotationsByType( Validate.class);
			if(validations.length==0)
			{
				validationChecks.put( recordType, (record)-> true);
			}
			else
			{
				Predicate<ActiveRecord> pred = (record) -> true;
				for(Validate valid:validations)
				{
					pred = pred.and( (ActiveRecord record) -> {
						Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), valid.attribute());
						return Validations.getValidationMethod( valid ).test( record, value);
					});
				}
				validationChecks.put( recordType, pred );
			}
		}
		return validationChecks.get( recordType);
	}
	
	private Consumer<ActiveRecord> getOrCreateValidationEnforcer(Class<? extends ActiveRecord> recordType)
	{
		if(!validationEnforcements.containsKey( recordType))
		{
			Validate[] validations = recordType.getAnnotationsByType( Validate.class);
			if(validations.length==0)
			{
				validationEnforcements.put( recordType, (record)->{});
			}
			else
			{
				Consumer<ActiveRecord> con = (record)->{};
				for(Validate valid:validations)
				{
					con = con.andThen( (ActiveRecord record) -> {
						Object value = record.getBase().getStore().getValue( record.getBase(), record.getPrimaryKey(), valid.attribute());
						if(!Validations.getValidationMethod( valid ).test( record, value))
						{
							throw new ValidationFailed(valid.attribute(), value);
						}
					});
				}
				validationEnforcements.put( recordType, con );
			}
		}
		return validationEnforcements.get( recordType);
	}
}
