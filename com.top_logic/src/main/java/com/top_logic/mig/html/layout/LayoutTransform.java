/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

/**
 * Function transforming a {@link LayoutComponent.Config} before component instantiation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LayoutTransform {

	/**
	 * The identity transform.
	 */
	LayoutTransform NONE = l -> l;

	/**
	 * Transforms the given configuration.
	 * 
	 * <p>
	 * The transformation is not required to copy the configuration but may destructively modify the
	 * given value.
	 * </p>
	 *
	 * @param config
	 *        The original configuration.
	 * @return The transformed configuration. This may be identical to the given configuation.
	 */
	LayoutComponent.Config transform(LayoutComponent.Config config);

}
