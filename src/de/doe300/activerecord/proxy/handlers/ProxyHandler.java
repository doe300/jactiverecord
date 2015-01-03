package de.doe300.activerecord.proxy.handlers;

import de.doe300.activerecord.proxy.RecordHandler;
import de.doe300.activerecord.record.ActiveRecord;
import java.lang.reflect.Method;

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
	public boolean handlesMethod(ActiveRecord record, Method method, Object[] args) throws IllegalArgumentException;
	
	/**
	 * @param <T>
	 * @param record
	 * @param handler
	 * @param method
	 * @param args
	 * @return the return value of the invocation
	 * @throws IllegalArgumentException 
	 */
	public <T extends ActiveRecord> Object invoke(ActiveRecord record, RecordHandler<T> handler, Method method, Object[] args) throws IllegalArgumentException;
}
