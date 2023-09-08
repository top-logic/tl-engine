/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.ConstantDisplayValue;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Helper class to manage opening and closing of {@link LayoutComponent} in dialogs.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogSupport {

	/** The list containing all opened dialogs */
	private final LinkedHashMap<LayoutComponent, DialogComponent> _openedDialogs =
		new LinkedHashMap<>();

	private final Map<LayoutComponent, DialogComponent> _dialogsView = Collections.unmodifiableMap(_openedDialogs);

	/** {@link WindowScope} opening dialogs. */
	private final WindowScope _window;

	/**
	 * Creates a new {@link DialogSupport}.
	 * 
	 * @param window
	 *        {@link WindowScope} that is used to open the dialogs.
	 */
	public DialogSupport(WindowScope window) {
		_window = window;
	}

	/**
	 * This method registers an {@link LayoutComponent} as opened dialog.
	 * 
	 * @param dialog
	 *        The content of the dialog.
	 * @param info
	 *        The dialog info for the opened dialog.
	 * @param dialogTitle
	 *        The internationalized title for the dialog.
	 */
	public void registerOpenedDialog(LayoutComponent dialog, DialogInfo info, HTMLFragment dialogTitle) {
		if (isDialogOpened(dialog)) {
			Logger.warn("Dialog " + dialog.getName() + " already opened.", this);
		}
		else {
			DialogComponent newDialog = DialogComponent.newDialog(dialog, info, dialogTitle);
			DialogWindowControl dialogControl =
				dialog.getMainLayout().getLayoutFactory().createDialogLayout(newDialog);
			_openedDialogs.put(dialog, newDialog);
			_window.openDialog(dialogControl);
		}
	}

	/**
	 * This method determines whether the given {@link LayoutComponent} was opened as dialog.
	 * 
	 * @param component
	 *        The {@link LayoutComponent} under test.
	 * 
	 * @return The programmatic open state.
	 */
	public boolean isDialogOpened(LayoutComponent component) {
		return _openedDialogs.containsKey(component);
	}

	/**
	 * This method deregisters a {@link LayoutComponent} which was currently shown as dialog.
	 * 
	 * @param component
	 *        A {@link LayoutComponent} which was formerly registered as dialog.
	 * 
	 * @exception IllegalArgumentException
	 *            if given <code>component</code> was not registered as dialog
	 */
	public void deregisterOpenedDialog(LayoutComponent component) {
		DialogComponent dialog = _openedDialogs.remove(component);

		if (dialog == null) {
			throw new IllegalArgumentException("'component' was not registered as dialog.");
		}
		dialog.closeDirectly();
	}

	/**
	 * Returns an unmodifiable view to the mapping of the {@link LayoutComponent}s that where
	 * {@link #registerOpenedDialog(LayoutComponent, DialogInfo, HTMLFragment)} in the order in
	 * where they were opened to their {@link DialogModel}.
	 */
	public Map<LayoutComponent, DialogComponent> getOpenedDialogs() {
		return _dialogsView;
	}

	/**
	 * This method opens the Dialog of the given {@link LayoutComponent}. It also makes the dialog
	 * parent of the component visible.
	 */
	public void openDialogContaining(LayoutComponent component) {
		assert component.getDialogParent() != null : "The component to open in dialog is not defined in a dialog.";
		LayoutComponent theDialog = component.getDialogTopLayout();
		if (theDialog.isVisible()) {
			return;
		}
		/* necessary to set the dialog parent visible. */
		LayoutComponent dialogParent = component.getDialogParent();
		dialogParent.makeVisible();

		/* Set the title of dialog to name of dialog component */
		DialogInfo theInfo = theDialog.getDialogInfo();

		HTMLFragment dialogTitle;
		if (theInfo.getTitle() != null) {
			dialogTitle = TypedConfigUtil.createInstance(theInfo.getTitle()).createTitle(theDialog);
		} else {
			ResKey prefix = component.getResPrefix().key(".dialogTitle");
			if (theInfo.getDefaultI18N() != null) {
				prefix = ResKey.fallback(theInfo.getDefaultI18N().suffix(".dialogTitle"), prefix);
			}
			dialogTitle = new ResourceText(prefix, ConstantDisplayValue.EMPTY_STRING);
		}

		OpenModalDialogCommandHandler.openDialog(theDialog, dialogTitle);
	}

}
