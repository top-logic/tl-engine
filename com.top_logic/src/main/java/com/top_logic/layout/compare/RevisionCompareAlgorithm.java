/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link AbstractCompareAlgorithm} returning a {@link Wrapper} in a given revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionCompareAlgorithm extends AbstractCompareAlgorithm {

	private Revision _revision;

	/**
	 * Creates a new {@link RevisionCompareAlgorithm}.
	 * 
	 * @param revision
	 *        The revision to search the {@link Wrapper} in.
	 */
	public RevisionCompareAlgorithm(Revision revision) {
		_revision = revision;
	}

	@Override
	public Object getCompareObject(LayoutComponent component, Object model) {
		if (!(model instanceof Wrapper)) {
			return null;
		}
		return WrapperHistoryUtils.getWrapper(_revision, (Wrapper) model);
	}
}
