/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.license;

import java.util.Optional;
import java.util.ServiceLoader;

import com.top_logic.basic.util.ResKey;

/**
 * Entry point for license validation.
 */
public abstract class LicenseTool {

	/**
	 * Called during application startup to show some info on system output.
	 */
	public abstract void printHeader();

	/**
	 * The current license.
	 */
	public abstract TLLicense getLicense();

	/**
	 * Whether the user limits set in the license are exceeded.
	 * 
	 * @deprecated No longer in use.
	 */
	@Deprecated
	public boolean usersExceeded(@SuppressWarnings("unused") TLLicense license) {
		return false;
	}

	/**
	 * Whether the feature with the given name is included in the current license.
	 */
	public abstract boolean includeFeature(String featureName);

	/**
	 * Whether only a single user session is allowed due to license restrictions.
	 * 
	 * @deprecated No longer in use.
	 */
	@Deprecated
	public boolean limitToOneSession(@SuppressWarnings("unused") TLLicense license) {
		return false;
	}

	/**
	 * Computes a description of the currently running product.
	 */
	public static String productType() {
		return getInstance().getProductType();
	}

	/**
	 * Implementation of {@link #productType()}.
	 */
	protected abstract String getProductType();

	/**
	 * Computes the validity state of the current license.
	 */
	public static ResKey licenseState(TLLicense license) {
		return getInstance().checkLicenseState(license);
	}

	/**
	 * Implementation of {@link #licenseState(TLLicense)}.
	 */
	protected abstract ResKey checkLicenseState(TLLicense license);

	/**
	 * Initializes the license system.
	 */
	public static void init() {
		getInstance().startup();
	}

	/**
	 * Implementation of {@link #init()}.
	 */
	protected abstract void startup();

	/**
	 * The singleton instance of {@link LicenseTool}.
	 */
	public static LicenseTool getInstance() {
		return Loader.INSTANCE;
	}

	private static class Loader {
		public static final LicenseTool INSTANCE = loadLicenseTool();

		private static LicenseTool loadLicenseTool() {
			ServiceLoader<LicenseTool> loader = ServiceLoader.load(LicenseTool.class);
			Optional<LicenseTool> toolOption = loader.findFirst();
			if (toolOption.isEmpty()) {
				return new OpenSourceLicenseTool();
			}
			return toolOption.get();
		}
	}

}
