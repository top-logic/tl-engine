/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import java.util.Map;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.layout.scripting.recorder.ref.misc.AttributeValue;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Base configuration for actions invoking {@link CommandHandler}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface CommandActionBase extends ComponentAction {

	/**
	 * Description of the arguments passed to
	 * {@link CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
	 * the command execution}.
	 */
	@Key(AttributeValue.NAME_ATTRIBUTE)
	Map<String, AttributeValue> getArguments();

	/** @see #getArguments() */
	void setArguments(Map<String, AttributeValue> value);

}
