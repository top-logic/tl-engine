/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.filter;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLModelPart;

/**
 * Configuration holding a filter computing which {@link TLModelPart parts of the model} should be
 * filtered out.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public interface ModelFilterConfig {

	/**
	 * Property name of {@link #getModelFilter()}.
	 * 
	 * @see #getModelFilter()
	 */
	String MODEL_FILTER = "model-filter";

	/**
	 * Filters {@link TLModelPart}s by a black- and whitelist.
	 * 
	 * @see ModelFilter
	 */
	@InstanceFormat
	@Name(MODEL_FILTER)
	ModelFilter<?> getModelFilter();

}
