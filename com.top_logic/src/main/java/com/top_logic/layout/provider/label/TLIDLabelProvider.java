/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.label;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.layout.LabelProvider;

/**
 * LabelProvider for {@link TLID}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLIDLabelProvider implements LabelProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return IdentifierUtil.toExternalForm((TLID) object);
	}

}

