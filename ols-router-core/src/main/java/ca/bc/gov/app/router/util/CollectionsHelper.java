/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.util;

import gnu.trove.map.TIntObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionsHelper {
	
	public static <K, V> void addToMapList(Map<K, List<V>> map, K key, V value, int startSize) {
		List<V> list = map.get(key);
		if(list == null) {
			list = new ArrayList<V>(startSize);
			map.put(key, list);
		}
		list.add(value);
	}

	public static <V> void addToTIntObjectMapList(TIntObjectMap<List<V>> map, int key, V value, int startSize) {
		List<V> list = map.get(key);
		if(list == null) {
			list = new ArrayList<V>(startSize);
			map.put(key, list);
		}
		list.add(value);
	}

}
