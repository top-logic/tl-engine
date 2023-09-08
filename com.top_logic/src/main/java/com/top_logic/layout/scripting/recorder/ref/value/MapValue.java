/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * A {@link Map} value.
 * 
 * @see ListValue
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 * 
 * @deprecated Use {@link MapNaming}.
 */
@Deprecated
public interface MapValue extends ValueRef {

	/**
	 * A {@link java.util.Map.Entry} value.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	interface MapEntryValue extends ConfigurationItem {
	
		/**
		 * Key of the entry.
		 * 
		 * @see java.util.Map.Entry#getKey()
		 */
		ModelName getKey();
	
		/**
		 * Setter for {@link #getKey()}.
		 */
		void setKey(ModelName keyRef);
	
		/**
		 * Value of the entry.
		 * 
		 * @see java.util.Map.Entry#getValue()
		 */
		ModelName getValue();
	
		/**
		 * Setter for {@link #getValue()}.
		 */
		void setValue(ModelName valueRef);
	
	}

	/**
	 * The entries of the map.
	 */
	List<MapValue.MapEntryValue> getEntries();

	/**
	 * Setter for {@link #getEntries()}
	 */
	void setEntries(List<MapValue.MapEntryValue> entries);

}
