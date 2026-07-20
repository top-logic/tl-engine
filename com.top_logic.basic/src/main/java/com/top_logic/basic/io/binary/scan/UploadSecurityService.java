/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary.scan;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;

/**
 * Service inspecting the content of uploaded files through the configured
 * {@link UploadContentChecker}s.
 *
 * <p>
 * The service is consulted from every upload-accepting control. It runs the configured checkers in
 * order and reports the first rejection. When the service is inactive or has no checkers, uploads
 * pass unchanged, so enabling upload scanning is a pure deployment decision with no effect on other
 * installations.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UploadSecurityService extends ConfiguredManagedClass<UploadSecurityService.Config> {

	/**
	 * Configuration of the {@link UploadSecurityService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<UploadSecurityService> {

		/**
		 * The {@link UploadContentChecker}s applied to every uploaded file, in order.
		 */
		@InstanceFormat
		List<UploadContentChecker> getCheckers();

	}

	private final List<UploadContentChecker> _checkers;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link UploadSecurityService}.
	 *
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 *
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public UploadSecurityService(InstantiationContext context, Config config) {
		super(context, config);
		_checkers = Collections.unmodifiableList(config.getCheckers());
	}

	/**
	 * Runs all configured {@link UploadContentChecker}s on the given content.
	 *
	 * @param data
	 *        The uploaded content to inspect. Must not be <code>null</code>.
	 *
	 * @return The error message key of the first checker that rejects the content, or
	 *         <code>null</code> if all checkers accept it.
	 */
	public ResKey check(BinaryData data) {
		for (UploadContentChecker checker : _checkers) {
			ResKey error = checker.check(data);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	/**
	 * The singleton {@link UploadSecurityService} instance.
	 */
	public static UploadSecurityService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Checks the given uploaded content against the active {@link UploadSecurityService}.
	 *
	 * <p>
	 * Convenience for call sites in upload-accepting controls: when the {@link Module} is not active,
	 * the content is accepted without any check, so callers need no service-availability guard.
	 * </p>
	 *
	 * @param data
	 *        The uploaded content to inspect. Must not be <code>null</code>.
	 *
	 * @return The error message key describing the rejection, or <code>null</code> if the content is
	 *         accepted.
	 */
	public static ResKey checkUpload(BinaryData data) {
		if (!Module.INSTANCE.isActive()) {
			return null;
		}
		return getInstance().check(data);
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link UploadSecurityService}.
	 */
	public static final class Module extends TypedRuntimeModule<UploadSecurityService> {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<UploadSecurityService> getImplementation() {
			return UploadSecurityService.class;
		}

	}

}
