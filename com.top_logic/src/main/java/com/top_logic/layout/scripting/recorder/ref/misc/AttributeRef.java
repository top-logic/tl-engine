/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.misc;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.scripting.recorder.ref.value.object.TypeRef;

/**
 * Reference to an object attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AttributeRef extends ConfigurationItem {

	TypeRef getType();

	void setType(TypeRef value);
	
	/**
	 * The name of the referenced attribute.
	 */
	String getAttributeName();
	void setAttributeName(String value);

}
