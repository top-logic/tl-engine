/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.importer.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.importer.base.StructuredDataImportPerformer.GenericDataObjectWithChildren;
import com.top_logic.util.error.TopLogicException;

/**
 * If there is no one-to-one mapping of XML-Attributes to attributes of the Knowledge-Model the import handlers have to
 * perform respective transformations of input data to obtain the correct attribute values of the Knowledge-Model. This
 * interface provides the transformed data to be assigned to attributes of the Knowledge-Model.
 * 
 * @author <a href="mailto:tgi@top-logic.com">tgi</a>
 */
public interface ImportValueProvider {

	public static ValueHolder NO_VALUE = new ValueHolder() {

		@Override
		public boolean hasValue() {
			return false;
		}

		@Override
		public Object getValue() {
			throw new IllegalStateException("no value available");
		}
	};

	/**
	 * interface to mark the difference between <code>null</code> as a "normal" value or as not having a value.
	 */
	public static interface ValueHolder {
		Object getValue();

		boolean hasValue();
	}
    
	/** 
	 * (unique) lookup key to find already existing objects. Must never be <code>null</code>.
	 * Uniquness can not be guaranteed by implementations. It must be assured by import data
	 * 
	 */
	String getLookupValue();
    
	/**
	 * @param attribute
	 *        name of the attribute within the Knowledge-Model.
	 * @return true if the attribute is not due to the import.
	 */
	boolean hasAttribute(String attribute);

	/**
	 * @param attribute
	 *        of the attribute within the Knowledge-Model.
	 * @return always a {@link ValueHolder} and never <code>null</code>
	 */
	ValueHolder getValue(String attribute);

	GenericDataObjectWithChildren getDO();
	
	Map<String, Object> getRawValues();

	public static class DefaultImportValueProvider implements ImportValueProvider {

	    private final GenericDataObjectWithChildren _dob;

		private Map<String, String> _dobAttMap = null;

		public DefaultImportValueProvider(GenericDataObjectWithChildren dob) {
			_dob = dob;
			_dobAttMap = this.createAttributeMapping(_dob);
		}

		@Override
		public boolean hasAttribute(String attribute) {
			return _dobAttMap.containsKey(attribute);
		}

		@Override
		public ValueHolder getValue(String attribute) {
			final String doAttrib = _dobAttMap.get(attribute);

			try {
				final Object value = _dob.getAttributeValue(doAttrib);
				if (value != null) {
					return new ValueHolder() {

						@Override
						public boolean hasValue() {
							return true;
						}

						@Override
						public Object getValue() {
							return value;
						}
					};
				}
			}
			catch (NoSuchAttributeException e) {
				return NO_VALUE;
			}
			return NO_VALUE;

		}

		@Override
		public GenericDataObjectWithChildren getDO() {
			return _dob;
		}

        @Override
        public String getLookupValue() {
			return _dob.getIdentifier().toString();
        }

        @Override
        public Map<String, Object> getRawValues() {
            Map<String, Object> theMap = new HashMap<>();

            for (String theKey : this._dobAttMap.values()) {
                try {
                    theMap.put(theKey, this._dob.getAttributeValue(theKey));
                }
                catch (NoSuchAttributeException ex) {
                    throw new TopLogicException(DefaultImportValueProvider.class, "values.get", new Object[] {theKey}, ex);
                }
            }

            return theMap;
        }

        // Private methods

        private Map<String, String> createAttributeMapping(DataObject aDO) {
			List<String> theDOAtts = CollectionUtil.toList(((MOStructure) aDO.tTable()).getAttributeNames());
			Map<String, String> theDOAttMap = new HashMap<>();

			for (String theDOAtt : theDOAtts) {
				String theDOAttChg = theDOAtt.toLowerCase();

				theDOAttChg = theDOAttChg.replace("_", "").replace("-", "");
				theDOAttMap.put(theDOAttChg, theDOAtt);
			}

			return theDOAttMap;
		}
	}

}