/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.io.IOException;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.util.Utils;

/**
 * Hash key that identifies a {@link KnowledgeItem} based on a subset of its attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CompositeKey extends TLID {

	private static final int[] PRIMES = {
		40277, 40283, 40289, 40343, 40351, 40357, 40361, 40387, 40423, 40427, 40429, 40433, 40459, 40471,
		40483, 40487, 40493, 40499, 40507, 40519, 40529, 40531, 40543, 40559, 40577, 40583, 40591, 40597,
		40609, 40627, 40637, 40639, 40693, 40697, 40699, 40709, 40739, 40751, 40759, 40763, 40771, 40787,
		40801, 40813, 40819, 40823, 40829, 40841, 40847, 40849, 40853, 40867, 40879, 40883, 40897, 40903,
		40927, 40933, 40939, 40949, 40961, 40973, 40993, 41011, 41017, 41023, 41039, 41047, 41051, 41057,
		41077, 41081, 41113, 41117, 41131, 41141, 41143, 41149, 41161, 41177, 41179, 41183, 41189, 41201,
		41203, 41213, 41221, 41227, 41231, 41233, 41243, 41257, 41263, 41269, 41281, 41299, 41333, 41341,
		41351, 41357, 41381, 41387, 41389, 41399, 41411, 41413, 41443, 41453, 41467, 41479, 41491, 41507,
		41513, 41519, 41521, 41539, 41543, 41549, 41579, 41593, 41597, 41603, 41609, 41611, 41617, 41621
	};

	/**
	 * The values identifying the indexed item.
	 */
	private final Object[] _storage;

	/**
	 * Constructs a {@link CompositeKey} that identifies the knowledge item by the given storage.
	 * 
	 * @param storage
	 *        The storage that defines the identity.
	 */
	public CompositeKey(Object[] storage) {
		this._storage = storage;
	}
	
	@Override
	public int hashCode() {
		int cnt = getSize();
		int result = cnt;
		for (int n = 0; n < cnt; n++) {
			Object value = getValue(n);
			if (value != null) {
				result += PRIMES[n] * value.hashCode();
			}
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
        if (obj instanceof CompositeKey) {
        	return equalsCompositeKey((CompositeKey) obj);
        } else {
        	return false;
        }
	}

	private boolean equalsCompositeKey(CompositeKey other) {
		int size = getSize();
		
		if (size != other.getSize()) {
			return false;
		}
		
        for (int n = 0; n < size; n++) {
            Object value1 = this.getValue(n);
            Object value2 = other.getValue(n);
            
            if (! Utils.equals(value1, value2)) {
            	return false;
            }
        }
        
		return true;
	}

	/**
	 * The number of entries in this key.
	 */
	private int getSize() {
		return _storage.length;
	}

	/**
	 * Retrieve the value of the entry with the given index from the base object.
	 */
	private Object getValue(int attributeIndex) {
		return _storage[attributeIndex];
	}

	@Override
	public int compareTo(TLID obj) {
		CompositeKey other = (CompositeKey) obj;
		for (int n = 0, cnt = getSize(); n < cnt; n++) {
			int result = ((Comparable) getValue(n)).compareTo(other.getValue(n));
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

	@Override
	public Object toStorageValue() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toExternalForm() {
		StringBuilder buffer = new StringBuilder();
		try {
			appendExternalForm(buffer);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		return buffer.toString();
	}

	@Override
	public void appendExternalForm(Appendable buffer) throws IOException {
		buffer.append('(');
		for (int n = 0, cnt = getSize(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(',');
			}
			buffer.append(String.valueOf(getValue(n)));
		}
		buffer.append(')');
	}

}
