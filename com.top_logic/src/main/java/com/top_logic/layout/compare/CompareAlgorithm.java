/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.form.decorator.CompareService;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Algorithm that computes the compare object for a given model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompareAlgorithm {

	/**
	 * Retrieves the comparison object.
	 * 
	 * @param component
	 *        The component the executes the comparison algorithm.
	 * @param model
	 *        The object to compute the comparison object for.
	 * @return The object to compare the given model to, or <code>null</code> when no comparison
	 *         is possible.
	 */
	Object getCompareObject(LayoutComponent component, Object model);

	/**
	 * Returns the plugin for the {@link CompareService} identified by the given property.
	 * 
	 * @param pluginKey
	 *        The {@link Property} identifying the plugin and delivering the default value for the
	 *        plugin, when no special is given.
	 * 
	 * @return The plugin. When this {@link CompareAlgorithm} has no special plugin, the default
	 *         value of the <code>pluginKey</code> is returned. May be <code>null</code> iff
	 *         {@link Property#getDefaultValue(TypedAnnotatable) default value} is <code>null</code> and this
	 *         {@link CompareAlgorithm} has no special value for the plugin.
	 */
	<T> T getPlugin(Property<T> pluginKey);

}
