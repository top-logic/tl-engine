/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.base.locking.Lock;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.component.edit.CanLock;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A base class for all apply {@link Command}s. It ensures, that changes made in context of an
 * {@link EditComponent} will be applied in case of an acquired {@link Lock} only.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractApplyCommand implements Command {

	private final LayoutComponent component;

	/**
	 * Create a new AbstractApplyCommand
	 * 
	 * @param component
	 *        - the {@link LayoutComponent}, which represents the context of the apply command
	 */
	protected AbstractApplyCommand(LayoutComponent component) {
		this.component = component;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final HandlerResult executeCommand(DisplayContext context) {

		if (component instanceof CanLock) {
			CanLock editComponent = (CanLock) this.component;
			editComponent.getLockHandler().updateLock();
		}

		return executeApplyCommand(context, component);
	}

	/**
	 * This method executes some apply command. It is triggered by pressing the client side
	 * representation of this {@link CommandModel} on the GUI.
	 * 
	 * @param context
	 *        - the {@link DisplayContext} holding request related data.
	 * @param component
	 *        - the {@link FormComponent}, which represents the context of the apply command
	 * @return The result of the processing, must not be <code>null</code>.
	 */
	protected abstract HandlerResult executeApplyCommand(DisplayContext context, LayoutComponent component);
}
