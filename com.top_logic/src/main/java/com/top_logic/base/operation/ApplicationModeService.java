/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.operation;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.mig.html.ContainerDetector;

/**
 * Single source of truth for the application's operational environment.
 *
 * <p>
 * The {@link #getMode() environment axis} ({@link OperationMode#DEVELOPMENT development} /
 * {@link OperationMode#TEST test} / {@link OperationMode#PRODUCTION production}) is boot-time
 * configuration and does not change while the application is running.
 * </p>
 *
 * <p>
 * Runtime maintenance is a separate, orthogonal axis exposed via
 * {@link #isMaintenanceActive() a maintenance flag}. A consumer folds the two axes as follows: if
 * maintenance is active, treat the application as being in maintenance regardless of the
 * environment; otherwise behaviour keys off {@link #getMode() the environment mode}.
 * </p>
 *
 * @author <a href="mailto:jonathan.huesing@top-logic.com">Jonathan Hüsing</a>
 */
public class ApplicationModeService extends ConfiguredManagedClass<ApplicationModeService.Config> {

	/**
	 * Configuration of the {@link ApplicationModeService}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ApplicationModeService> {

		/**
		 * The configured operational environment.
		 *
		 * <p>
		 * If left unset, the service {@link ApplicationModeService#getMode() derives} the mode from
		 * the runtime signals of the installation, so existing applications keep working with zero
		 * configuration.
		 * </p>
		 */
		@Name("mode")
		@Nullable
		OperationMode getMode();

		/** @see #getMode() */
		void setMode(OperationMode mode);
	}

	/**
	 * The effective environment, computed lazily on first access and then cached (the environment
	 * axis is static for the lifetime of the application).
	 */
	private volatile OperationMode _effectiveMode;

	/**
	 * Creates a new {@link ApplicationModeService}.
	 */
	public ApplicationModeService(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * The effective operational environment of this installation.
	 *
	 * <p>
	 * Returns the {@link Config#getMode() configured} mode if one is set. Otherwise the mode is
	 * derived so that existing applications behave unchanged without configuration:
	 * </p>
	 * <ul>
	 * <li>running under an automated test container: {@link OperationMode#TEST},</li>
	 * <li>otherwise a non-deployed (developer) workspace: {@link OperationMode#DEVELOPMENT},</li>
	 * <li>otherwise: {@link OperationMode#PRODUCTION}.</li>
	 * </ul>
	 */
	public OperationMode getMode() {
		OperationMode result = _effectiveMode;
		if (result == null) {
			result = computeMode();
			_effectiveMode = result;
		}
		return result;
	}

	private OperationMode computeMode() {
		OperationMode configured = getConfig().getMode();
		if (configured != null) {
			return configured;
		}
		return deriveMode(isUnderTest(), Environment.isDeployed());
	}

	/**
	 * Derives the {@link OperationMode} from the environment signals.
	 *
	 * <p>
	 * Pure function so that each derivation branch can be tested in isolation.
	 * </p>
	 *
	 * @param underTest
	 *        Whether the application runs inside an automated test container.
	 * @param deployed
	 *        Whether the application is a deployed install, see {@link Environment#isDeployed()}.
	 */
	public static OperationMode deriveMode(boolean underTest, boolean deployed) {
		if (underTest) {
			return OperationMode.TEST;
		}
		if (!deployed) {
			return OperationMode.DEVELOPMENT;
		}
		return OperationMode.PRODUCTION;
	}

	private static boolean isUnderTest() {
		if (!ContainerDetector.Module.INSTANCE.isActive()) {
			// The container detector needs a servlet context and is absent in minimal setups.
			return false;
		}
		ContainerDetector detector = ContainerDetector.getInstance();
		return detector != null && detector.isTesting();
	}

	/**
	 * Whether the application currently is in a maintenance window.
	 *
	 * <p>
	 * This is the orthogonal maintenance axis, delegating to the {@link MaintenanceWindowManager}.
	 * It is null-safe: if the manager is absent (minimal setups), maintenance is reported inactive.
	 * </p>
	 */
	public boolean isMaintenanceActive() {
		if (!MaintenanceWindowManager.Module.INSTANCE.isActive()) {
			return false;
		}
		MaintenanceWindowManager manager = MaintenanceWindowManager.getInstance();
		if (manager == null) {
			return false;
		}
		return manager.getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE;
	}

	/**
	 * The currently active {@link ApplicationModeService}.
	 */
	public static ApplicationModeService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for instantiation of the {@link ApplicationModeService}.
	 */
	public static class Module extends TypedRuntimeModule<ApplicationModeService> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ApplicationModeService> getImplementation() {
			return ApplicationModeService.class;
		}
	}
}
