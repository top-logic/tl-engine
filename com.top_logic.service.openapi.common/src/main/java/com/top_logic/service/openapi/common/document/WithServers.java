/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of target servers.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface WithServers extends ConfigurationItem {

	/** Configuration name for the value of {@link #getServers()}. */
	String SERVERS = "servers";

	/**
	 * An array of {@link ServerObject}, which provide connectivity information to a target server.
	 */
	@Name(SERVERS)
	List<ServerObject> getServers();

}

