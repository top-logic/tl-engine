/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.history.HistoryEntry;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.mig.html.layout.ComponentName;

/**
 * The class {@link SimpleWindowScope} is used by {@link SimpleControlScope}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SimpleWindowScope implements WindowScope {
	
	/**
	 * The dialogs which are currently visible on the GUI. The dialogs are kept in "open-order": The
	 * first opened dialog is the first one in the list. The most recently opened dialog is the last
	 * in the list. As the most recently opened dialog is also the active one, this means that the
	 * active dialog is the last one in the list.
	 */
	private ArrayList<DialogWindowControl> dialogs = new ArrayList<>();
	
	/**
	 * The popup dialogs which are currently visible on the GUI. The dialogs are kept in
	 * "open-order": The first opened dialog is the first one in the list. The most recently opened
	 * dialog is the last in the list. As the most recently opened dialog is also the active one,
	 * this means that the active dialog is the last one in the list.
	 */
	private ArrayList<PopupDialogControl> popupDialogs = new ArrayList<>();

	private final WindowScope opener;
	private final FrameScope topLevelScope;

	@Override
	public ComponentName getName() {
		/* Good enough. This class is only used for tests, as it lies in the 'test' package. And the
		 * tests using it don't need the name. */
		return null;
	}

	@Override
	public List<DialogWindowControl> getDialogs() {
		return dialogs;
	}

	@Override
	public List<PopupDialogControl> getPopupDialogs() {
		return Collections.unmodifiableList(popupDialogs);
	}

	@Override
	public DialogWindowControl getActiveDialog() {
		return CollectionUtil.getLast(dialogs);
	}

	@Override
	public PopupDialogControl getActivePopupDialog() {
		return CollectionUtil.getLast(popupDialogs);
	}

	public SimpleWindowScope(WindowScope opener, FrameScope topLevelScope) {
		this.opener = opener;
		this.topLevelScope = topLevelScope;
	}

	@Override
	public void openDialog(DialogWindowControl dialog) {
		dialogs.add(dialog);
	}

	@Override
	public void openPopupDialog(PopupDialogControl aPopupDialog) {
		popupDialogs.add(aPopupDialog);
	}

	@Override
	public WindowScope getOpener() {
		return opener;
	}

	@Override
	public FrameScope getTopLevelFrameScope() {
		return topLevelScope;
	}

	@Override
	public <T extends Appendable> T appendReference(T out, WindowScope openedWindow) throws IOException {
		return out;
	}
	
	@Override
	public <T extends Appendable> T appendCloseAllWindowsCommand(T out, FrameScope source) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void deliverContent(BinaryDataSource data, boolean showInline) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addHistory(HistoryEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void pop() {
	}

	@Override
	public void push() {
	}

}
