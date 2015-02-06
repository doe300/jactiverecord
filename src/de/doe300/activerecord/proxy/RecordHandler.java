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
package de.doe300.activerecord.proxy;

import de.doe300.activerecord.RecordBase;
import de.doe300.activerecord.logging.Logging;
import de.doe300.activerecord.proxy.handlers.ProxyHandler;
import de.doe300.activerecord.record.ActiveRecord;
import de.doe300.activerecord.record.TimestampedRecord;
import de.doe300.activerecord.record.attributes.AttributeGetter;
import de.doe300.activerecord.record.attributes.AttributeSetter;
import de.doe300.activerecord.record.attributes.Attributes;
import de.doe300.activerecord.store.RecordStore;
import de.doe300.activerecord.validation.ValidationFailed;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * The handler-class for proxy-based ActiveRecord. Mainly handles standard ActiveRecord-methods and delegates to any given {@link ProxyHandler}
 * @author doe300
 * @param <T>
 */
public final class RecordHandler<T extends ActiveRecord> implements InvocationHandler
{
	
	private final RecordStore store;
	private final RecordBase<T> base;
	private final ProxyHandler[] proxyHandlers;
	private final int primaryKey;
	
	private static final Method getPrimaryKey, getBase, hashCode, toString, equals, touch;
	private static final Constructor<MethodHandles.Lookup> constructor;
	static {
		try
		{
			getPrimaryKey = ActiveRecord.class.getMethod( "getPrimaryKey");
			getBase = ActiveRecord.class.getMethod( "getBase");
			hashCode = Object.class.getMethod( "hashCode");
			toString = Object.class.getMethod( "toString");
			equals = Object.class.getMethod( "equals", Object.class);
			touch = TimestampedRecord.class.getMethod( "touch");
		}
		catch(NoSuchMethodException e)
		{
			throw new NoSuchMethodError(e.getMessage());
		}
		
		try
		{
			constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
			if (!constructor.isAccessible())
			{
				constructor.setAccessible(true);
			}
		}
		catch ( NoSuchMethodException | SecurityException ex )
		{
			throw new NoSuchMethodError(ex.getMessage());
		}
		
	}

	/**
	 * For every ActiveRecord object, one Record-Handler is created
	 * @param primaryKey
	 * @param base
	 * @param handlers 
	 */
	public RecordHandler(int primaryKey, RecordBase<T> base, ProxyHandler... handlers)
	{
		this.primaryKey = primaryKey;
		this.base = base;
		this.store = base.getStore();
		this.proxyHandlers = handlers;
	}

	@Override
	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
	{
		ActiveRecord record;
		try{
			record=ActiveRecord.class.cast( proxy );
		}
		catch(ClassCastException e)
		{
			throw new IllegalArgumentException("RecordHandler can only be used with instances of ActiveRecord", e );
		}
		//0. handle ActiveRecord-Methods
		if(method.equals( getPrimaryKey))
		{
			return primaryKey;
		}
		if(method.equals( getBase))
		{
			return base;
		}
		if(method.equals( hashCode))
		{
			//TODO not a real hash
			return (base.getTableName().hashCode() << 7) + primaryKey;
		}
		if(method.equals( toString))
		{
			return "ActiveRecord{"+primaryKey+"@"+base.getRecordType().getCanonicalName()+"}";
		}
		if(method.equals( equals))
		{
			return args!=null && args.length==1 && args[0] instanceof ActiveRecord && RecordBase.equals(record, ( ActiveRecord ) args[0]);
		}
		if(method.equals( touch ))
		{
			store.touch( base, primaryKey );
		}
		//1. call method-handler
		//proxy-handlers are checked first to maximize extensibility
		if(proxyHandlers!=null)
		{
			for(ProxyHandler handler:proxyHandlers)
			{
				if(handler.handlesMethod( record, method, args ))
				{
					Logging.getLogger().debug( base.getRecordType().getSimpleName(), "Method "+method.getName()+" is handled by "+handler);
					return handler.invoke( record, this, method, args );
				}
			}
		}
		//2. delegate default methods back to Interface
		if (method.isDefault())
		{
			//handles default methods
			//see http://rmannibucau.wordpress.com/2014/03/27/java-8-default-interface-methods-and-jdk-dynamic-proxies/
			final Class<?> declaringClass = method.getDeclaringClass();
			return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
				.unreflectSpecial(method, declaringClass)
				.bindTo(proxy)
				.invokeWithArguments(args);
		}
		//3. check if attribute-accessor
		//3.1 check for AttributeAccessor-annotations
		if(method.isAnnotationPresent( AttributeGetter.class))
		{
			String column = method.getAnnotation( AttributeGetter.class).name();
			Method converterMethod = Attributes.getConverterMethod( method);
			if(converterMethod==null)
			{
				return store.getValue(base, primaryKey, column );
			}
			return converterMethod.invoke( proxy, store.getValue(base, primaryKey, column ) );
		}
		if(method.isAnnotationPresent( AttributeSetter.class))
		{
			String column = method.getAnnotation( AttributeSetter.class).name();
			Method converterMethod = Attributes.getConverterMethod( method);
			Method validatorMethod = Attributes.getValidatorMethod(method);
			if(args == null|| args.length==0)
			{
				Logging.getLogger().error( base.getRecordType().getSimpleName(), method.getName()+": Argument for setter can't be null");
				throw new IllegalArgumentException("Argument for setter can't be null");
			}
			if(validatorMethod!=null)
			{
				if(validatorMethod.invoke( proxy, args[0] ) == Boolean.FALSE)
				{
					throw new ValidationFailed(column, args[0]);
				}
			}
			if(converterMethod==null)
			{
				store.setValue(base, primaryKey, column, args[0]);
			}
			else
			{
				store.setValue(base, primaryKey, column, converterMethod.invoke( proxy, args[0] ));
			}
			//setters are void-methods
			return null;
		}
		//3.2 check for bean-style accessor
		String property = Attributes.getPropertyName( method );
		if(property!=null)
		{
			if(args!=null&&args.length==1&&Attributes.isSetter( method, args[0] == null ? null : args[0].getClass(), false))
			{
				store.setValue(base, primaryKey, property,args[0] );
				//setters are void-methods
				return null;
			}
			if(args==null||args.length==0&&Attributes.isGetter( method, false ))
			{
				return store.getValue(base, primaryKey, property );
			}
		}
		//method not handled
		Logging.getLogger().error( base.getRecordType().getSimpleName(), "Method '"+method.getName()+"' is not implemented for this record-type");
		throw new NoSuchMethodException("Method '"+method.getName()+"' is not implemented for this record-type");
	}

	/**
	 * @return the record-type
	 * @see RecordBase#getRecordType() 
	 */
	public Class<T> getRecordType()
	{
		return base.getRecordType();
	}
}
