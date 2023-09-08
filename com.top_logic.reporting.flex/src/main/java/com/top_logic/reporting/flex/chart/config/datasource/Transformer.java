/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;

import java.util.Collection;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * A {@link Transformer} transforms a {@link Collection} of {@link Wrapper}s. E.g. translate wrappes
 * into an historic {@link Revision}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public interface Transformer {

	/**
	 * @param wrappers
	 *        the values to be transformed
	 * @return the transformed input
	 */
	public Collection<Wrapper> transform(Collection<Wrapper> wrappers);

}