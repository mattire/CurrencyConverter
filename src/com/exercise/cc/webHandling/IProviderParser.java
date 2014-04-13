package com.exercise.cc.webHandling;

import java.util.HashMap;

/**
 * Interface for getting currency data from web,
 * This level of abstraction is implemented so that
 * the could be more than one web data provider
 * @author Matti
 *
 */
public interface IProviderParser {
	float fetchRate(String currency1, String currency2);
	HashMap<String, Float> fetchRates();
	float parseRate(String data, String currency1, String currency2);
	String getErrors();
}
