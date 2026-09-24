/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.element.boundsec.manager.coverage.SecurityCoverageCheck;
import com.top_logic.element.boundsec.manager.coverage.TypeCoverage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.command.ViewAction;

/**
 * {@link ViewAction} serving the security coverage display: it analyses the model based access
 * definition and returns the analyzed types ({@code List<TypeCoverage>}) to write to the channel
 * feeding the {@link SecurityCoverageTable}.
 * <p>
 * App-specific action, referenced by {@code class=} in the coverage view rather than claiming a
 * global {@code @TagName}.
 * </p>
 *
 * @implNote The analysis is delegated to {@link SecurityCoverageCheck#analyze()}, so that the
 *           display, the startup log and the application test see the same findings.
 */
public class SecurityCoverageAction implements ViewAction {

	/**
	 * Configuration for {@link SecurityCoverageAction}.
	 */
	public interface Config extends PolymorphicConfiguration<SecurityCoverageAction> {

		@Override
		@ClassDefault(SecurityCoverageAction.class)
		Class<? extends SecurityCoverageAction> getImplementationClass();

	}

	/**
	 * Creates a new {@link SecurityCoverageAction} from configuration.
	 */
	@CalledByReflection
	public SecurityCoverageAction(InstantiationContext context, Config config) {
		// No configuration to keep.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		return analyze();
	}

	/**
	 * A fresh analysis of the access definition.
	 */
	private static List<TypeCoverage> analyze() {
		return SecurityCoverageCheck.getInstance().analyze();
	}
}
