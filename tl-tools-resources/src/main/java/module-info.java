/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
/**
 * Module for the tool resources.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
module com.top_logic.tools.resources {

	requires de.haumacher.msgbuf;

	requires org.slf4j;
	
	exports com.top_logic.tools.resources;
	exports com.top_logic.tools.resources.translate;
	exports com.top_logic.tools.resources.translate.deepl;

}