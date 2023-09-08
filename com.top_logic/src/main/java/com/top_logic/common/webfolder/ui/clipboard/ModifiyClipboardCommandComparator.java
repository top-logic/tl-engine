/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import java.util.Comparator;

import com.top_logic.layout.form.model.ExecutableCommandField;

/**
 * Compare CommandFields containing {@link ModifyClipboardExecutable} by the State of the
 * executable.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class ModifiyClipboardCommandComparator implements Comparator<ExecutableCommandField> {

	/**
	 * Singleton instance of {@link ModifiyClipboardCommandComparator}
	 */
	public static final Comparator<ExecutableCommandField> INSTANCE = new ModifiyClipboardCommandComparator();

	@Override
	public int compare(ExecutableCommandField o1, ExecutableCommandField o2) {
		return ((ModifyClipboardExecutable) o1.getExecutable()).getState().compareTo(
			((ModifyClipboardExecutable) o2.getExecutable()).getState());
	}

}
