/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;
import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.history.BrowserHistory;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * A representative of a browser window.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WindowScope extends BrowserHistory {

	/**
	 * The (internal, technical) name of the window.
	 */
	ComponentName getName();

	/**
	 * Unmodifiable {@link List} of {@link DialogWindowControl}s which are
	 * currently logically open.
	 * 
	 * <p>
	 * Dialogs in the returned list are either currently visible at the GUI and
	 * not being closed in this command, or being
	 * {@link #openDialog(DialogWindowControl) opened} within the current
	 * command. Dialogs that were visible at the GUI before the current command,
	 * and programmatically closed are no longer in the list. The top-most open
	 * dialog is the last element of the returned list. At position 0 there is
	 * the first opened dialog.
	 * </p>
	 */
	public List<DialogWindowControl> getDialogs();

	/**
	 * Unmodifiable {@link List} of {@link PopupDialogControl}s which are
	 * currently logically open.
	 * 
	 * <p>
	 * Dialogs in the returned list are either currently visible at the GUI and
	 * not being closed in this command, or being
	 * {@link #openPopupDialog(PopupDialogControl) opened} within the current
	 * command. Dialogs that were visible at the GUI before the current command,
	 * and programmatically closed are no longer in the list. The top-most open
	 * dialog is the last element of the returned list. At position 0 there is
	 * the first opened dialog.
	 * </p>
	 */
	public List<PopupDialogControl> getPopupDialogs();

	/**
	 * Returns the active dialog or null, if there is no dialog.
	 */
	public DialogWindowControl getActiveDialog();

	/**
	 * Returns the active popup dialog or null, if there is no dialog.
	 */
	public PopupDialogControl getActivePopupDialog();

	/**
	 * This method registers and opens a new dialog. The control for
	 * <code>aDialog</code> is <code>not</code> in the list returned by
	 * {@link #getDialogs()}.
	 * 
	 * @param aDialog
	 *        a dialog to open. must not be <code>null</code>.
	 * 
	 * @see DialogModel#getCloseAction()
	 * @see DialogModel#isClosed()
	 */
	public void openDialog(DialogWindowControl aDialog);

	/**
	 * This method registers and opens a new popup dialog. The control for
	 * <code>aPopupDialog</code> is <code>not</code> in the list returned by
	 * {@link #getDialogs()}.
	 * 
	 * @param aPopupDialog
	 *        a popup dialog to open. must not be <code>null</code>.
	 * 
	 * @see PopupDialogModel#isClosed()
	 */
	public void openPopupDialog(PopupDialogControl aPopupDialog);

	/**
	 * Returns the opener window of this window or <code>null</code> if it is
	 * the main window. It is expected that the window of the document
	 * represented by the {@link #getTopLevelFrameScope() top frame scope} of
	 * the returned opener is the client side window which has opened this
	 * window.
	 */
	public WindowScope getOpener();

	/**
	 * Returns the frame scope corresponding to the top level document displayed
	 * in the window represented by this {@link WindowScope}.
	 * 
	 * <p>
	 * Must not be called before the WindowScope has a client side
	 * representation.
	 * </p>
	 */
	public FrameScope getTopLevelFrameScope();

	/**
	 * This method returns a client-side reference from the
	 * {@link #getTopLevelFrameScope() top level frame scope} of this
	 * {@link WindowScope} to the top level {@link FrameScope} of the given
	 * <code>openedWindow</code>.
	 * 
	 * @param out
	 *        the {@link Appendable} to append reference to. never
	 *        <code>null</code>.
	 * @param openedWindow
	 *        a window opened by this window, i.e.
	 *        {@link WindowScope#getOpener()} applied in
	 *        <code>openedWindow</code> returns this {@link WindowScope}.
	 * 
	 * @return a reference to the given {@link Appendable}
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws some
	 */
	public <T extends Appendable> T appendReference(T out, WindowScope openedWindow) throws IOException;

	/**
	 * Appends a Javascript command to close all windows which were opened by
	 * this window
	 * 
	 * @param <T>
	 *        the concrete type of the {@link Appendable} to add the command to.
	 * @param out
	 *        the stream to write the command to.
	 * @param source
	 *        the FrameScope which represents the client side document to render
	 *        the command in.
	 * 
	 * @return a reference to the given {@link Appendable}
	 * 
	 * @throws IOException
	 *         iff the given {@link Appendable} throws some.
	 */
	public <T extends Appendable> T appendCloseAllWindowsCommand(T out, FrameScope source) throws IOException;
	
	/**
	 * Delivers the given {@link BinaryData} to the client
	 * 
	 * @param data
	 *        the item to offer the user for download
	 * @param showInline
	 *        whether the browser should try to open the content inline or
	 *        directly offer for download
	 */
	public void deliverContent(BinaryDataSource data, boolean showInline);

	/**
	 * Delivers the given {@link BinaryData} as download.
	 * 
	 * @see #deliverContent(BinaryDataSource, boolean)
	 */
	default void deliverContent(BinaryDataSource data) {
		deliverContent(data, false);
	}

	/**
	 * Delivers the given {@link BinaryData} displayed inline.
	 * 
	 * @see #deliverContent(BinaryDataSource, boolean)
	 */
	default void deliverContentInline(BinaryDataSource data) {
		deliverContent(data, true);
	}

}
