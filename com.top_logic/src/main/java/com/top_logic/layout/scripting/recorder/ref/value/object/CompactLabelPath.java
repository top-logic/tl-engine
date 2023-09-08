/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import java.util.List;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * A path of labels, used to identify an object.
 * <p>
 * A {@link CompactLabelPath} is similar to a {@link LabelPath}, but the path is not specified with
 * individual tags but in an attribute in the form "Bli > Bla > Blub".
 * </p>
 * 
 * @deprecated See {@link ValueRef}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Deprecated
public interface CompactLabelPath extends ContextRef {

	/**
	 * A path of labels, used to identify an object.
	 * <p>
	 * Root is not contained in the path and represented by an empty path.
	 * </p>
	 */
	@Format(BreadcrumbStrings.class)
	List<String> getLabelPath();

	/**
	 * @see #getLabelPath()
	 */
	void setLabelPath(List<String> labels);

}
