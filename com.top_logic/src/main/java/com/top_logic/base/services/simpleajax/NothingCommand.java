/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.PrintWriter;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * The class {@link NothingCommand} is useful to force an AJAX request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class NothingCommand extends AbstractCommandHandler {

	public static final String COMMAND = "noOpAJAXCommand";

	public static final NothingCommand INSTANCE = newInstance(NothingCommand.class, COMMAND);

	public NothingCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Never need confirm to do nothing.
	 * 
	 * @return <code>false</code>
	 * @see com.top_logic.tool.boundsec.AbstractCommandHandler#needsConfirm()
	 */
	@Override
	public boolean needsConfirm() {
		return false;
	}

	/**
	 * This method writes the client side call for this command. The sender of this call will be the
	 * given {@link LayoutComponent}, so it must be a {@link LayoutComponent} where this command
	 * was registered.
	 * 
	 * @param anOut
	 *            the {@link PrintWriter} to write the call statement.
	 * @param aComponent
	 *            the {@link LayoutComponent} which will be the sender of this demand.
	 */
	public void writeCallStatement(PrintWriter anOut, LayoutComponent aComponent) {
		internalWriteCallStatement(anOut, aComponent);
	}
	
	/**
	 * This method writes the client side call for this command. The sender of this call will be the
	 * {@link LayoutComponent} for which the call statement is written. That means especially that
	 * this command needs to be registered at the {@link LayoutComponent} which writes this call
	 * statement.
	 * 
	 * @param anOut
	 *            the {@link PrintWriter} to write the call statement.
	 */
	public void writeCallStatement(PrintWriter anOut) {
		internalWriteCallStatement(anOut, null);
	}

	private void internalWriteCallStatement(PrintWriter anOut, LayoutComponent aComponent) {
		anOut.write("invokeCommand('");
		anOut.write(COMMAND);
		anOut.write("', null, ");
		if (aComponent != null) {
			anOut.write(aComponent.getJSContextInformation());
		} else {
			anOut.write("null");
		}
		anOut.write(");");
	}
	
	/**
	 * The {@link NothingCommand} is always executable.
	 * 
	 * @see AbstractCommandHandler#createExecutabilityRule()
	 */
	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return AlwaysExecutable.INSTANCE;
	}
}
