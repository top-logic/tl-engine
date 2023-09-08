/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateListener;
import com.top_logic.layout.UpdateQueue;

/**
 * The class {@link DummyUpdateListener} is a dummy implementation of {@link UpdateListener} for
 * test uses.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DummyUpdateListener extends DummyNotifyListener implements UpdateListener {

	private final ClientAction REVALIDATION_OBJECT = new ClientAction() {
		@Override
		protected String getXSIType() {
			return null;
		}

		@Override
		protected void writeChildrenAsXML(DisplayContext context, TagWriter writer) throws IOException {
			// no children here
		}
	};

	private boolean invalid;

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;

	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void revalidate(DisplayContext context, UpdateQueue actions) {
		setInvalid(false);
		actions.add(getRevalidationAction());
	}
	
	@Override
	public void notifyDisabled(boolean disabled) {
		setInvalid(true);
	}

	/**
	 * This method returns the {@link ClientAction} added to the {@link UpdateQueue} during
	 * revalidation.
	 * 
	 * @see #revalidate(DisplayContext, UpdateQueue)
	 */
	public final ClientAction getRevalidationAction() {
		return REVALIDATION_OBJECT;
	}
}
