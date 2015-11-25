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
package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * On {@link RecordHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]) }, ProxyHandlers are checked very early (after {@link ActiveRecord}-methods).
 * So the handlers can modify every data-access, which maximizes extensibility but bears the risk of ProxyHandlers overriding an essential method.
 * @author doe300
 */
public interface ProxyHandler
{
	
	/**
	 * @param record
	 * @param method
	 * @param args
	 * @return whether this handler handles the given method
	 * @throws IllegalArgumentException 
	 */
	public boolean handlesMethod(@Nonnull final ActiveRecord record, @Nonnull final Method method, Object[] args) throws IllegalArgumentException;
	
	/**
	 * @param <T>
	 * @param record
	 * @param handler
	 * @param method
	 * @param args
	 * @return the return value of the invocation
	 * @throws IllegalArgumentException 
	 */
	public <T extends ActiveRecord> Object invoke(@Nonnull final ActiveRecord record, @Nonnull final RecordHandler<T> handler, @Nonnull final Method method, Object[] args) throws IllegalArgumentException;
	
	/**
	 * A hook to manipulate/analyze the <code>value</code> for an attribute-getter
	 * @param record the record to get the attribute for
	 * @param attributeName the attribute-name
	 * @param value the attribute value
	 * @return the new attribute-value to return
	 * @since 0.7
	 */
	public default Object getAttributeHook(@Nonnull final ActiveRecord record, @Nonnull final String attributeName, @Nullable final Object value)
	{
		return value;
	}
	
	/**
	 * A hook to manipulate/analyze the <code>value</code> for an attribute-setter
	 * @param record the record to set the attribute for
	 * @param attributeName the attribute-name
	 * @param value the attribute value
	 * @return the new attribute-value to return
	 * @since 0.7
	 */
	public default Object setAttributeHook(@Nonnull final ActiveRecord record, @Nonnull final String attributeName, @Nullable final Object value)
	{
		return value;
	}
}
