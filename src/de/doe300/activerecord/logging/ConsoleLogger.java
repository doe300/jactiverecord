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
package de.doe300.activerecord.logging;

/**
 * LoggerAdapter using {@link System.out} and {@link System.err} as output
 * @author doe300
 */
public class ConsoleLogger implements LoggerAdapter
{

	@Override
	public void info( String source, String message )
	{
		System.out.println( source+" [INFO] "+message);
	}

	@Override
	public void debug( String source, String message )
	{
		System.out.println( source+" [DEBUG] "+message);
	}

	@Override
	public void error( String source, String message )
	{
		System.err.println( source+" [ERROR] "+message);
	}

	@Override
	public void error( String source, Throwable exception )
	{
		System.out.println( source+" [ERROR] "+exception.getMessage());
		exception.printStackTrace( System.err);
	}

}
