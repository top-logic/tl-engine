/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * {@link Mapping} that maps resource key with optional encoded arguments to internationalized
 * messages.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class I18NMapping implements Mapping<ResKey, String> {

    @Override
	public String map(ResKey input) {
		return Resources.getInstance().getString(input);
    }

}
