/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import java.util.Collection;

/**
 * {@link GraphPart} that can be decorated with {@link Label}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface LabelOwner extends GraphPart {

	/**
	 * The {@link Label}s displayed on this instance.
	 */
	Collection<? extends Label> getLabels();

	/**
	 * Creates a new {@link Label} for this instance.
	 * 
	 * @see #getLabels()
	 */
	Label createLabel();

}
