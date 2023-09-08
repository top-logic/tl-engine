/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} for {@link Revision}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		Revision rev = (Revision) object;
		if (rev == null) {
			return null;
		}
		if (rev.isCurrent()) {
			return Resources.getInstance().getString(I18NConstants.CURRENT_REVISION_LABEL);
		}

		String commitNumberString = HTMLFormatter.getInstance().formatLong(rev.getCommitNumber());
		return Resources.getInstance().getString(I18NConstants.REVISION_LABEL__COMMIT_NUMBER.fill(commitNumberString));
	}

}

