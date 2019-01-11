/**
 * Copyright 2008-2019, Province of British Columbia
 *  All rights reserved.
 */
package ca.bc.gov.app.router.util;

import java.util.Arrays;

/** 
 * IntArrayMap is a Map from an int key to a generic object value.
 * It uses a dynamically resized array (like an ArrayList)
 * and works on the assumption that the int key values are already 
 * densely packed from 0 to the max-key value, and so no hashing or other
 * techniques are required or used. 
 * 
 * ArrayList would almost work as is, but ArrayList.set(list.size(), value) 
 * fails with an out-of-range exception whereas IntArrayMap.put(size(), value) 
 * dynamically resizes the internal array.
 *   
 * @author chodgson@refractions.net
 *
 * @param <T> The object class of values stored in the map
 */
public class IntObjectArrayMap<T> {
	private static final int DEFAULT_INITIAL_CAPACITY = 10;
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	private Object[] data; // of type T

	/**
	 * Create an IntObjectArrayMap with a default initial capacity.
	 */
	public IntObjectArrayMap() {
		this(DEFAULT_INITIAL_CAPACITY);
	}	

	/**
	 * Create an IntObjectArrayMap with the specified initial capacity.
	 * Larger initial capacities, closer to the expected required
	 * capacity, reduce array copy overhead caused by resizing.
	 * 
	 * @param initialCapacity the initial capacity of the backing array
	 */
	public IntObjectArrayMap(int initialCapacity) {
		data = new Object[initialCapacity];
	}
	
	/**
	 * Stores the value at the key in the map.
	 * 
	 * @param key The key by which the value can be later retrieved
	 * @param value the object to store in the map
	 */
	public void put(int key, T value) {
		ensureCapacity(key + 1);
		data[key] = value;
	}
	
	/**
	 * Retrieves the value at the given key in the map.
	 *
	 * @param key the key by which to retrieve the value
	 * @return the value object stored at the given key or null if nothing is stored there
	 */
	@SuppressWarnings("unchecked")
	public T get(int key) {
		if(key >= data.length) {
			return null;
		}
		return (T)(data[key]);
	}
	
	private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
		if (minCapacity - data.length > 0) {
            int oldCapacity = data.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                newCapacity = MAX_ARRAY_SIZE;
            }
            data = Arrays.copyOf(data, newCapacity);
		}
    }
}
