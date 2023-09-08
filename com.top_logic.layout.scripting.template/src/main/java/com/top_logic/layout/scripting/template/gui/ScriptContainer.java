/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ActionWriter;

/**
 * Represents an {@link ApplicationAction}. A {@link PersistableScriptContainer} can store its
 * {@link #getAction() action} back to its source file.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptContainer {

	/**
	 * A {@link ScriptContainer} that can {@link #persist()} its {@link #getAction() action} back to
	 * the file from which it was parsed.
	 */
	public static final class PersistableScriptContainer extends ScriptContainer {

		private final File _file;

		@SuppressWarnings("synthetic-access")
		private PersistableScriptContainer(ApplicationAction action, File file) {
			super(action);
			_file = checkNonNull(file);
		}

		/**
		 * Write the {@link #getAction() action} back to its source file.
		 */
		public void persist() {
			writeToFile(ActionWriter.INSTANCE.writeAction(getAction(), true, true));
		}

		private void writeToFile(String content) {
			try {
				// ConfigurationWriter serialises data in standard XML encoding
				Charset fileEncoding = StringServices.CHARSET_UTF_8;
				FileUtilities.writeStringToFile(content, _file, fileEncoding);
			} catch (IOException ex) {
				throw new RuntimeException("Failed to persist the action. File: '"
					+ FileUtilities.getSafeDetailedPath(_file) + "'; Cause: " + ex.getMessage(), ex);
			}
		}

	}

	private ApplicationAction _action;

	private ScriptContainer(ApplicationAction action) {
		_action = checkNonNull(action);
	}

	/**
	 * Creates a {@link ScriptContainer} that cannot be {@link PersistableScriptContainer#persist()
	 * persisted} as it has no source file.
	 */
	public static ScriptContainer createTransient(ApplicationAction action) {
		return new ScriptContainer(action);
	}

	/**
	 * Create a {@link PersistableScriptContainer} with the given {@link ApplicationAction} and the
	 * given source {@link File}.
	 */
	@SuppressWarnings("synthetic-access")
	public static PersistableScriptContainer createPersistable(ApplicationAction action, File file) {
		return new PersistableScriptContainer(action, file);
	}

	/**
	 * The {@link ApplicationAction}s, never null.
	 */
	public ApplicationAction getAction() {
		return _action;
	}

	/**
	 * @see #getAction()
	 */
	public void setAction(ApplicationAction newAction) {
		_action = checkNonNull(newAction);
	}

	private static <T> T checkNonNull(T object) {
		if (object == null) {
			throw new NullPointerException();
		}
		return object;
	}

}
