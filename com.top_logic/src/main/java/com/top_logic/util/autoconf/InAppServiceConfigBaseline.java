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

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ConfigurationBaseline;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;

/**
 * {@link ConfigurationBaseline} of a service configuration stored in the application.
 *
 * <p>
 * The baseline captures the state of the file {@link InAppServiceConfigStore#fileFor(BasicRuntimeModule)}
 * - its content, or that it does not exist - and the effective configuration of the service at that
 * time. The stored configuration counts as changed while the effective configuration differs from
 * the captured one, compared by value: storing the configuration in use again, even into a file
 * that did not exist before, is no change.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class InAppServiceConfigBaseline implements ConfigurationBaseline {

	private final BasicRuntimeModule<?> _module;

	private final File _file;

	/**
	 * The captured content of the file, <code>null</code> if it did not exist.
	 */
	private final byte[] _content;

	/**
	 * A copy of the captured effective configuration, <code>null</code> if the service had none.
	 */
	private final ServiceConfiguration<?> _config;

	private InAppServiceConfigBaseline(BasicRuntimeModule<?> module, File file, byte[] content,
			ServiceConfiguration<?> config) {
		_module = module;
		_file = file;
		_content = content;
		_config = config;
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
		ServiceConfiguration<?> config = effectiveConfig(module);
		return new InAppServiceConfigBaseline(module, file, content(file),
			config == null ? null : ConfigCopier.copy(config));
	}

	@Override
	public boolean isChanged() {
		if (Arrays.equals(_content, content(_file))) {
			return false;
		}
		ServiceConfiguration<?> current = effectiveConfig(_module);
		if (current == null || _config == null) {
			return current != _config;
		}
		return !ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(_config, current);
	}

	/**
	 * The configuration the service of the given module is started with, <code>null</code> if it
	 * has none.
	 */
	private static ServiceConfiguration<?> effectiveConfig(BasicRuntimeModule<?> module) {
		try {
			return ApplicationConfig.getInstance().getServiceConfiguration(module.getImplementation());
		} catch (ConfigurationException exception) {
			return null;
		}
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
