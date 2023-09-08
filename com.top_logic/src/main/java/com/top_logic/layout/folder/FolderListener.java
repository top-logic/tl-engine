/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import java.util.EventListener;

import com.top_logic.layout.table.TableData;

/**
 * The class {@link FolderListener} is currently just a marker interface.
 * 
 * {@link FolderListener} can be attached to some {@link FolderData} to force it
 * to observe its {@link TableData}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FolderListener extends EventListener {

}

