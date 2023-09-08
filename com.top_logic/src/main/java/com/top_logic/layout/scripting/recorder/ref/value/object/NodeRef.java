/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import java.util.List;

import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.ListBinding;

/**
 * A reference to a node in a structure.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use "StructuredElementNaming"
 */
@Deprecated
public interface NodeRef extends VersionedObjectRef {

	/**
	 * The name of the structure the referenced object is part of.
	 */
	String getStructureName();
	void setStructureName(String value);
	
	/**
	 * List of node names that describe a path from the structure root (exclusive) to the referenced
	 * node (inclusive).
	 */
	@ListBinding(format = StringValueProvider.class, tag = "node", attribute = "name")
	List<String> getStructurePath();
	void setStructurePath(List<String> value);

}
