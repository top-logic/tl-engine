/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.LabelProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.Resources;

/**
 * {@link LabelProvider} for {@link Revision}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionLabelProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		Revision rev = (Revision) object;
		if (rev == null) {
			return null;
		}

		return Resources.getInstance().getString(resKey(rev));
	}

	@Override
	public String getTooltip(Object object) {
		Revision rev = (Revision) object;
		if (rev == null) {
			return null;
		}

		return Resources.getInstance().getString(resKey(rev).tooltipOptional());
	}

	private ResKey resKey(Revision rev) {
		if (rev.isCurrent()) {
			return I18NConstants.CURRENT_REVISION_LABEL;
		}
		if (rev.getCommitNumber() < Revision.FIRST_REV) {
			return I18NConstants.INITIAL_REVISION_LABEL;
		}

		Formatter formatter = HTMLFormatter.getInstance();
		String commitNumberString = formatter.formatLong(rev.getCommitNumber());
		String date = formatter.formatShortDateTime(rev.resolveDate());
		return I18NConstants.REVISION_LABEL__REV_DATE.fill(commitNumberString, date);
	}

}

