/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector;

import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion.AssertionPlugin;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * When {@link #executeCommand(DisplayContext)} is called: For every {@link AssertionPlugin} given
 * in the constructor, an assertion is recorded if that assertion is
 * {@link AssertionPlugin#recordAssertionIfRequested() requested}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class RecordAssertionsCommand implements Command {

	private final List<? extends AssertionPlugin<?>> assertions;

	public RecordAssertionsCommand(List<? extends AssertionPlugin<?>> assertions) {
		this.assertions = assertions;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		for (AssertionPlugin<?> assertionType : assertions) {
			assertionType.recordAssertionIfRequested();
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
