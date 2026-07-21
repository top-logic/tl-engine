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
		 * If left unset, the service {@link ApplicationModeService#getMode() derives} the mode: an
		 * automated test install becomes {@link OperationMode#TEST test}, everything else defaults
		 * to {@link OperationMode#PRODUCTION production}. A developer workspace is selected by setting
		 * this property (typically through the {@code %OPERATION_MODE%} alias backed by the
		 * {@code tl_operation_mode} variable) to {@link OperationMode#DEVELOPMENT development}.
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
	 * Returns the {@link Config#getMode() configured} mode if one is set (a developer workspace or
	 * IDE run selects {@link OperationMode#DEVELOPMENT} this way, typically through the
	 * {@code %OPERATION_MODE%} alias backed by the {@link Environment#OPERATION_MODE} variable).
	 * Otherwise the mode is derived:
	 * </p>
	 * <ul>
	 * <li>running under an automated test container: {@link OperationMode#TEST},</li>
	 * <li>otherwise: {@link OperationMode#PRODUCTION}.</li>
	 * </ul>
	 *
	 * <p>
	 * Production is therefore the zero-configuration default: an installation with neither a
	 * configured mode nor the {@link Environment#OPERATION_MODE} variable set is treated as
	 * production.
	 * </p>
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
		return deriveMode(isUnderTest());
	}

	/**
	 * Derives the {@link OperationMode} when no mode is explicitly configured.
	 *
	 * <p>
	 * An automated test install becomes {@link OperationMode#TEST}; everything else defaults to
	 * {@link OperationMode#PRODUCTION}. {@link OperationMode#DEVELOPMENT} is never derived here - it
	 * is selected explicitly via the configured mode (see {@link #getMode()}), which mirrors
	 * {@link Environment#isDeployed()} keying off the same {@link Environment#OPERATION_MODE}
	 * variable.
	 * </p>
	 *
	 * <p>
	 * Pure function so that each derivation branch can be tested in isolation.
	 * </p>
	 *
	 * @param underTest
	 *        Whether the application runs inside an automated test container.
	 */
	public static OperationMode deriveMode(boolean underTest) {
		if (underTest) {
			return OperationMode.TEST;
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
