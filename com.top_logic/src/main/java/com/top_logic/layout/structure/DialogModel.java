/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.awt.Dimension;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.NoBubblingEventType;
import com.top_logic.layout.basic.Command;


/**
 * Model of {@link DialogWindowControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DialogModel extends WindowModel, TypedAnnotatable {

	/**
	 * @see #isClosed()
	 * 
	 * @see DialogClosedListener
	 */
	EventType<DialogClosedListener, Object, Boolean> CLOSED_PROPERTY =
		new NoBubblingEventType<>("closed") {

			@Override
			protected void internalDispatch(DialogClosedListener listener, Object sender, Boolean oldValue,
					Boolean newValue) {
				listener.handleDialogClosed(sender, oldValue, newValue);
			}

		};

	/**
	 * Whether the dialog should have a close button in its title bar.
	 */
	boolean hasCloseButton();

	/**
	 * Whether the dialog can be resized.
	 */
	boolean isResizable();

	/**
	 * Whether this dialog is closed.
	 */
	boolean isClosed();

	/**
	 * The {@link Command} that closes this dialog by finally setting the
	 * {@link #isClosed()} property.
	 */
	Command getCloseAction();
	
	/**
	 * Returns a help ID that can be used for this dialog. Might be
	 * <code>null</code>
	 */
	String getHelpID();

	/**
	 * Saves the given size as new dialog size. Maybe an NOOP, if the actual model does not support
	 * customizable dialog sizes.
	 */
	void saveCustomizedSize(Dimension size);

	/**
	 * true, if the size of the dialog has been customized, false otherwise.
	 */
	boolean hasCustomizedSize();

	/**
	 * the customized size of dialog, if {@link #hasCustomizedSize()} is true. Otherwise
	 *         behavior is undetermined.
	 */
	Dimension getCustomizedSize();
}
