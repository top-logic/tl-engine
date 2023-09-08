/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization for webfolder package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix CLIPBOARD_RESOURCES = legacyPrefix("tl.clipboard.");

	/**
	 * The current model is <code>null</code>, it cannot be copied to clipboard.
	 */
	public static ResKey ADD_ERROR_NO_MODEL ;

	/**
	 * Paste is not possible, because the underlying component has no model.
	 */
	public static ResKey NO_PASTE_NO_MODEL;
	
	/**
	 * Paste is not possible, because underlying model is not a container.
	 */
	public static ResKey NO_PASTE_NO_CONTAINER_MODEL;

	/**
	 * Clipboard already contains the given model.
	 */
	public static ResKey CLIPBOARD_CONTAINS_MODEL;

	/**
	 * Trying to add model to clipboard fails.
	 */
	public static ResKey ADD_TO_CLIPBOARD_FAILED;
	
	public static ResKey ALREADY_IN_CLIPBOARD = legacyKey("tl.executable.disabled.alreadyInClipboard");

	public static ResKey CONFIRM_DELETE = legacyKey("base.edit.confirmDelete");

	public static ResKey FILE = legacyKey("tl.dialog.folder.upload.message.testFilename.file");

	public static ResKey LOCK;

	public static ResKey NOT_IN_HISTORIC_STATE = legacyKey("tl.executable.disabled.notInHistoricState");

	public static ResKey NO_CLIPBOARD = legacyKey("tl.executable.disabled.noClipboard");

	public static ResKey NO_FOLDER;

	public static ResKey NO_WRAPPER = legacyKey("tl.executable.disabled.noWrapper");

	public static ResKey REMOVE = legacyKey("webfolder.error.object.remove");

	static {
		initConstants(I18NConstants.class);
	}

}
