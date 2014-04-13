package com.exercise.cc;

/**
 * Interface for handling asynchronous db update & currency conversion results 
 * @author Matti
 *
 */
public interface IUpdateResultHandler {
	
	/**
	 * Handler method for handling asynchronous currency databas update
	 * @param result String representing update result
	 */
	void handleUpdateResult(String result);
	
	/**
	 * Handler method for handling asynchronous currency conversion result 
	 * @param result Float describing result of conversion
	 */
	void handleConversionResult(float result);
}
