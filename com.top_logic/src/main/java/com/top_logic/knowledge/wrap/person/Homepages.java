/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Key;

/**
 * Configuration for the {@link Homepage}s of the current user.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Homepages extends ConfigurationItem {

	/**
	 * All known homepages for the current user indexed by {@link Homepage#getMainLayout()}.
	 */
	@Key(Homepage.MAINLAYOUT_NAME)
	Map<String, Homepage> getPages();

}

