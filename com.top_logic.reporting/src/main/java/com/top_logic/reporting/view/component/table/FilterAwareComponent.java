/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.table;



/**
 * Component whose model is computed by a {@link FilterAwareModelBuilder}.
 *
 * @author     <a href="mailto:olb@top-logic.com">olb</a>
 */
public interface FilterAwareComponent {
	/** The builder computing the model of this component. */
	FilterAwareModelBuilder getModelBuilder();
}
