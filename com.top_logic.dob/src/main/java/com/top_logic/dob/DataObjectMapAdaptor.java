/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.simple.ExampleDataObject;
import com.top_logic.dob.util.MetaObjectUtils;

/**
 * Adaptor of the {@link DataObject} interface to the {@link Map} interface. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DataObjectMapAdaptor implements Map {

    /** Map a DataObject to a DataObjectMapAdaptor */
	public static final Mapping MAPPING = new Mapping() {
		@Override
		public Object map(Object input) {
			return new DataObjectMapAdaptor((DataObject) input);
		}
	};
	
	/** DataObjectMapAdaptor is based on this Object */ 
	private final DataObject object;

	public DataObjectMapAdaptor(DataObject object) {
		this.object = object;
		if (object instanceof ExampleDataObject) {
		    Logger.warn("Please use ExampleDataObject.getMap()", this);
		}
	}
	
	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsKey(Object key) {
		if (key instanceof String) {
			String attribute = (String) key;
			return MetaObjectUtils.hasAttribute(object.tTable(), attribute);
		}
		
		return false;
	}

	@Override
	public Object get(Object key) {
		if (key instanceof String) {
			String attribute = (String) key;
			if (MetaObjectUtils.hasAttribute(object.tTable(), attribute)) {
				try {
					return object.getAttributeValue(attribute);
				} catch (NoSuchAttributeException ex) {
					throw new UnreachableAssertion(ex);
				}
			}
		} 
		
		return null;
	}

	/** Simplified access for helper methods below */
    public Object get(String key) {
        if (MetaObjectUtils.hasAttribute(object.tTable(), key)) {
            try {
                return object.getAttributeValue(key);
            } catch (NoSuchAttributeException ex) {
                throw new UnreachableAssertion(ex);
            }
        }
        return null;
    }

    @Override
	public boolean isEmpty() {
		return object.getAttributeNames().length == 0;
	}

	@Override
	public Object put(Object key, Object value) {
		if (key instanceof String) {
			String attribute = (String) key;
			if (MetaObjectUtils.hasAttribute(object.tTable(), attribute)) {
				{
					Object before = object.getAttributeValue(attribute);
					object.setAttributeValue(attribute, value);
					return before;
				}
			} else {
				throw new IllegalArgumentException("Value for key '" + attribute + "' cannot be set.");
			}
		} 
		
		throw new IllegalArgumentException("Key must be of type String.");
	}

	@Override
	public void putAll(Map t) {
		for (Iterator it = t.entrySet().iterator(); it.hasNext(); ) {
			Entry entry = (Entry) it.next();
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Object remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set keySet() {
		return new LinkedHashSet(Arrays.asList(object.getAttributeNames()));
	}

	@Override
	public int size() {
		return object.getAttributeNames().length;
	}

	@Override
	public Collection values() {
	    String[] keys = object.getAttributeNames();
		ArrayList result = new ArrayList(keys.length);
        for (int i = 0; i < keys.length; i++) {
			result.add(get(keys[i]));
		}
		return result;
	}

	@Override
	public Set entrySet() {
		String[] keys = object.getAttributeNames();
		HashSet result = new LinkedHashSet(keys.length);
		for (int i = 0; i < keys.length; i++) {
			final String key = keys[i];
			result.add(new Entry() {
				@Override
				public Object getKey() {
					return key;
				}

				@Override
				public Object getValue() {
					return get(key);
				}

				@Override
				public Object setValue(Object value) {
					return put(key, value);
				}
			});
		}
		return result;
	}

	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}

}
