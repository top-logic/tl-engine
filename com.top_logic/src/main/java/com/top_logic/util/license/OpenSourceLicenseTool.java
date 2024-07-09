/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.license;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;

/**
 * License management for the open-source version of TopLogic.
 */
public class OpenSourceLicenseTool extends LicenseTool {

	/**
	 * The license information for an active AGPL license.
	 */
	public interface AGPL extends TLLicense {
		@Deprecated
		@Override
		@IntDefault(Integer.MAX_VALUE)
		int getUsers();

		@Deprecated
		@Override
		@IntDefault(Integer.MAX_VALUE)
		int getRestrictedUsers();

		@Deprecated
		@Override
		@IntDefault(Integer.MAX_VALUE)
		int getClusterSize();
	}

	private static final TLLicense LICENSE;

	static {
		LICENSE = TypedConfiguration.newConfigItem(AGPL.class);
	}

	@Override
	public void printHeader() {
		String text =
			  "  _____           _                _                    \n"
			+ " |_   _|__  _ __ | |    ___   __ _(_) ___               \n"
			+ "   | |/ _ \\| '_ \\| |   / _ \\ / _` | |/ __|              \n"
			+ "   | | (_) | |_) | |__| (_) | (_| | | (__               \n"
			+ "   |_|\\___/| .__/|_____\\___/ \\__, |_|\\___|              \n"
			+ "   ___     |_|           ____|___/                      \n"
			+ "  / _ \\ _ __   ___ _ __ / ___|  ___  _   _ _ __ ___ ___ \n"
			+ " | | | | '_ \\ / _ \\ '_ \\\\___ \\ / _ \\| | | | '__/ __/ _ \\\n"
			+ " | |_| | |_) |  __/ | | |___) | (_) | |_| | | | (_|  __/\n"
			+ "  \\___/| .__/ \\___|_| |_|____/ \\___/ \\__,_|_|  \\___\\___|\n"
			+ "       |_|                                              ";
		System.out.println();
		System.out.println(text);
		System.out.println();
		System.out.println(
			"Licensed under GNU Affero General Public License - AGPL");
		System.out.println();
	}

	@Override
	public TLLicense getLicense() {
		return LICENSE;
	}

	@Override
	public boolean includeFeature(String featureName) {
		return true;
	}

	@Override
	protected ResKey checkLicenseState(TLLicense license) {
		return I18NConstants.LICENCE_VALID;
	}

	@Override
	protected String getProductType() {
		return "TopLogic OpenSource - AGPL";
	}

	@Override
	protected void startup() {
		// Ignore.
	}

}
