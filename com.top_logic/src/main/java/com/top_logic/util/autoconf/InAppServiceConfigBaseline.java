/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.autoconf;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfigurationBaseline;

/**
 * {@link ConfigurationBaseline} of a service configuration stored in the application.
 *
 * <p>
 * The baseline captures the state of the file {@link InAppServiceConfigStore#fileFor(BasicRuntimeModule)}:
 * its content, or that it does not exist.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class InAppServiceConfigBaseline implements ConfigurationBaseline {

	private final File _file;

	/**
	 * The captured content of the file, <code>null</code> if it did not exist.
	 */
	private final byte[] _content;

	private InAppServiceConfigBaseline(File file, byte[] content) {
		_file = file;
		_content = content;
	}

	/**
	 * Captures the current state of the stored configuration of the service of the given module.
	 *
	 * @param module
	 *        The module of the service.
	 * @return The baseline holding the current state.
	 * @throws UncheckedIOException
	 *         When the stored configuration cannot be read.
	 */
	public static InAppServiceConfigBaseline capture(BasicRuntimeModule<?> module) {
		File file = InAppServiceConfigStore.fileFor(module);
		return new InAppServiceConfigBaseline(file, content(file));
	}

	@Override
	public boolean isChanged() {
		return !Arrays.equals(_content, content(_file));
	}

	@Override
	public void restore() throws IOException {
		if (_content == null) {
			Files.deleteIfExists(_file.toPath());
		} else {
			FileUtilities.enforceDirectory(_file.getParentFile());
			Files.write(_file.toPath(), _content);
		}
	}

	private static byte[] content(File file) {
		if (!file.exists()) {
			return null;
		}
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException ex) {
			throw new UncheckedIOException("Cannot read stored service configuration: " + file, ex);
		}
	}

}
