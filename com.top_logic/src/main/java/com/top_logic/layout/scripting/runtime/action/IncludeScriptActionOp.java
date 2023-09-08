/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import static java.util.Collections.*;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.action.IncludeScriptAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.ActionReader;

/**
 * A {@link DynamicActionOp} that includes a script file.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class IncludeScriptActionOp extends DynamicActionOp<IncludeScriptAction> {

	/** {@link TypedConfiguration} constructor for {@link IncludeScriptActionOp}. */
	public IncludeScriptActionOp(InstantiationContext context, IncludeScriptAction config) {
		super(context, config);
	}

	@Override
	public final List<ApplicationAction> createActions(ActionContext context) {
		BinaryContent script = (BinaryContent) context.resolve(getConfig().getScript());
		return singletonList(parse(script));
	}

	/** Parses the {@link ApplicationAction} tree. */
	private ApplicationAction parse(BinaryContent script) {
		try {
			return ActionReader.INSTANCE.readAction(script);
		} catch (ConfigurationException exception) {
			String message = "Failed to read the script: " + script + ". Cause: " + exception.getMessage();
			throw ApplicationAssertions.fail(getConfig(), message, exception);
		}
	}

}
