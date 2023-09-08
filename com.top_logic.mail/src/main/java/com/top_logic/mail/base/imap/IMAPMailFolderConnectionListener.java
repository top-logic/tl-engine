/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base.imap;

import java.lang.ref.WeakReference;

import javax.mail.Folder;
import javax.mail.Store;
import javax.mail.event.ConnectionEvent;
import javax.mail.event.ConnectionListener;

import com.top_logic.basic.Logger;

/**
 * Removes the {@link IMAPMailFolder#getOriginalFolder() original folder} from the
 * {@link IMAPMailFolder} when its connection is closed.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class IMAPMailFolderConnectionListener implements ConnectionListener {

	private final WeakReference<IMAPMailFolder> _folder;

	private final Folder _registry;

	IMAPMailFolderConnectionListener(IMAPMailFolder folder) {
		_folder = new WeakReference<>(folder);
		_registry = folder.origFolder;
	}

	@Override
	public void closed(ConnectionEvent connectionEvent) {
		boolean unregistered = shutDown(connectionEvent);
		if (unregistered) {
			logDebug("Folder '" + connectionEvent.getSource() + "' closed!");
		}
	}

	@Override
	public void disconnected(ConnectionEvent connectionEvent) {
		boolean unregistered = shutDown(connectionEvent);
		if (unregistered) {
			logDebug("Folder '" + connectionEvent.getSource() + "' disconnected!");
		}
	}

	private boolean shutDown(ConnectionEvent connectionEvent) {
		IMAPMailFolder folder = _folder.get();
		if ((folder == null) || (folder.origFolder == null)) {
			unregister();
			return true;
		}
		Object eventSource = connectionEvent.getSource();
		if ((eventSource instanceof Store) || isFromOriginalFolder(folder, eventSource)) {
			folder.origFolder = null;
			unregister();
			return true;
		}
		return false;
	}

	private boolean isFromOriginalFolder(IMAPMailFolder folder, Object eventSource) {
		return eventSource instanceof Folder && ((Folder) eventSource).getName().equals(folder.origFolder.getName());
	}

	private void unregister() {
		_registry.removeConnectionListener(this);
	}

	private void logDebug(String message) {
		Logger.debug(message + " " + Thread.currentThread(), IMAPMailFolderConnectionListener.class);
	}

	@Override
	public void opened(ConnectionEvent connectionEvent) {
		// Nothing to do on opening event.
	}

}
