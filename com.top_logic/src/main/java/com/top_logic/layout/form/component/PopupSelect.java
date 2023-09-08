/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Underlying component of a popup selection dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PopupSelect {

	public static void closeOnLeavingEditMode(FormMutator self, ChannelListener listener, EditComponent editComponent,
			boolean editMode) {
		FormHandler openingFormHandler = self.getFormHandler();
		if (openingFormHandler.equals(editComponent)) {
			// It's really *our* EditComponent...
			if (!editMode) {
				// We have to close ourselves
				((LayoutComponent) self).closeDialog();
				EditComponent r = ((EditComponent) openingFormHandler);
				// remove Listener. Will be re-added when opening the next
				// time...
				r.editModeChannel().removeListener(listener);
			}
		}
	}
}
