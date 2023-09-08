/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import java.util.List;

import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.ListBinding;

/**
 * A path of labels, used to identify an object.
 * 
 * Root is not contained in the path and represented by an empty path.
 * 
 * @see ValuePath More powerful but also more verbose variant.
 * 
 * @deprecated Use the much more user-friendly {@link CompactLabelPath}
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface LabelPath extends ContextRef {

	/**
	 * List of node names that describe a path from the structure root to the referenced node.
	 */
	@ListBinding(format = StringValueProvider.class, tag = "label", attribute = "name")
	List<String> getLabelPath();
	void setLabelPath(List<String> labels);

}
