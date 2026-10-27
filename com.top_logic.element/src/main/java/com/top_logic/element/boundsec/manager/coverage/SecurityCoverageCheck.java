/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.boundsec.manager.coverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.ServiceExtensionPoint;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.AccessControlCategory;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;

/**
 * Reports the gaps in the model based access definition of the application.
 *
 * <p>
 * For every concrete type the check answers the two questions that decide whether a user can ever
 * see an object of that type: does a rule deliver a role on it, and is a role granted the read
 * operation on it? A type that fails one of the two is invisible for every user, and a grant to a
 * role that no rule delivers on the type never takes effect. A type with an access parent delegates
 * every decision to another object and needs no definition of its own; it is reported as delegated.
 * </p>
 *
 * <p>
 * Every gap found is written to the application log during startup as an informational message. An
 * application decides which of the gaps it accepts, so the log is a report, not an alarm: the
 * coverage tab of the security administration shows the same findings interactively, and a test
 * that must not tolerate a gap asserts on them.
 * </p>
 *
 * <p>
 * Besides the read access, the check reports the operations: an operation declared for a type that
 * no role holds there, and an operation that a view, a command, a form or a privileged script checks
 * on a type it does not apply to, or that is not known at all. The places checking an operation are
 * reported by the configured {@link Config#getOperationUses() providers}; the startup log covers
 * the access definition alone, since those places are read on demand.
 * </p>
 *
 * <p>
 * Types of a module that deliberately has no access definition are kept out of the check by naming
 * the module in the excluded modules. The check only reads the definitions, it never changes them.
 * </p>
 *
 * @implNote The check is an extension of the {@link AccessManager} and therefore starts with it and
 *           only with it, so that a test setup with a minimal type system, which does not start the
 *           access manager, is not dragged into starting the whole model stack.
 * @implNote The result is computed by {@link SecurityCoverageAnalysis} and handed to the caller of
 *           {@link #analyze()}, so that the log, a user interface and a test all see the same
 *           findings under the same configured exclusions. The reusable test is
 *           {@code test.com.top_logic.element.boundsec.manager.coverage.TestSecurityCoverage}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	SecurityConfigurationService.Module.class,
	ModelService.Module.class,
})
@ServiceExtensionPoint(AccessManager.Module.class)
@Label("Security coverage check")
@ServiceCategory.Of(AccessControlCategory.class)
public class SecurityCoverageCheck extends ConfiguredManagedClass<SecurityCoverageCheck.Config> {

	/** Separator between the type of a finding and its message. */
	private static final String TYPE_SEPARATOR = ": ";

	/** What a finding about places not declaring the type they check is reported for. */
	private static final String UNKNOWN_TYPE = "?";

	/**
	 * Configuration options for {@link SecurityCoverageCheck}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<SecurityCoverageCheck> {

		/** Configuration name for the value of {@link #getExcludedModules()}. */
		String EXCLUDED_MODULES = "excluded-modules";

		/** Configuration name for the value of {@link #getLogFindings()}. */
		String LOG_FINDINGS = "log-findings";

		/** Configuration name for the value of {@link #getOperationUses()}. */
		String OPERATION_USES = "operation-uses";

		/** Tag of an entry of {@link #getOperationUses()}. */
		String PROVIDER = "provider";

		/**
		 * Names of the model modules that are deliberately left without an access definition.
		 *
		 * <p>
		 * The types of these modules are not analyzed and therefore never produce a finding.
		 * </p>
		 */
		@Name(EXCLUDED_MODULES)
		@Format(CommaSeparatedStringSet.class)
		Set<String> getExcludedModules();

		/**
		 * Whether the findings are written to the application log during startup.
		 *
		 * <p>
		 * The findings are informational: they report which types the access definition leaves
		 * open, they do not mark the startup as faulty. Switching the log off does not switch the
		 * analysis off; the coverage tab of the security administration and a test asserting on the
		 * findings work either way.
		 * </p>
		 */
		@Name(LOG_FINDINGS)
		@BooleanDefault(true)
		boolean getLogFindings();

		/**
		 * The sources of the places checking an operation: views, commands, forms, privileged
		 * scripts.
		 *
		 * <p>
		 * The analysis reports an operation such a place uses that is unknown or does not apply to
		 * the type it is checked on. A module defining such places contributes its provider here.
		 * </p>
		 */
		@Name(OPERATION_USES)
		@EntryTag(PROVIDER)
		List<PolymorphicConfiguration<? extends OperationUseProvider>> getOperationUses();

	}

	private final List<OperationUseProvider> _useProviders;

	/**
	 * Creates a {@link SecurityCoverageCheck}.
	 *
	 * @param context
	 *        The context to instantiate sub configurations in.
	 * @param config
	 *        The configuration of this service.
	 */
	public SecurityCoverageCheck(InstantiationContext context, Config config) {
		super(context, config);
		_useProviders = new ArrayList<>();
		for (PolymorphicConfiguration<? extends OperationUseProvider> provider : config.getOperationUses()) {
			OperationUseProvider instance = context.getInstance(provider);
			if (instance != null) {
				_useProviders.add(instance);
			}
		}
	}

	@Override
	protected void startUp() {
		super.startUp();

		if (getConfig().getLogFindings()) {
			// The places checking an operation - the views above all - are read on demand, not
			// while the services start up.
			ThreadContext.inSystemContext(SecurityCoverageCheck.class,
				() -> logCoverage(SecurityCoverageAnalysis.newInstance(getConfig().getExcludedModules()).analyze()));
		}
	}

	/**
	 * Analyzes the access definition of every type that is not excluded by
	 * {@link Config#getExcludedModules()}.
	 *
	 * @return The coverage of every analyzed type, ordered by the qualified type name.
	 */
	public List<TypeCoverage> analyze() {
		return newAnalysis().analyze();
	}

	/**
	 * A new analysis of the access definition in effect, with the exclusions and the
	 * {@link #getOperationUses() operation uses} of this check.
	 *
	 * <p>
	 * An analysis answers more than the findings: which roles a user can hold on a type, which
	 * places check an operation. A user interface working with these asks the analysis of the
	 * check, so that it sees what the check reports.
	 * </p>
	 */
	public SecurityCoverageAnalysis newAnalysis() {
		return SecurityCoverageAnalysis.newInstance(getConfig().getExcludedModules(), getOperationUses());
	}

	/**
	 * The places of the application checking an operation, as the configured
	 * {@link Config#getOperationUses() providers} report them.
	 *
	 * <p>
	 * A provider failing to report is logged and contributes nothing, so that a broken source of
	 * uses does not hide the findings of the access definition itself.
	 * </p>
	 */
	public List<OperationUse> getOperationUses() {
		if (_useProviders.isEmpty()) {
			return Collections.emptyList();
		}
		List<OperationUse> result = new ArrayList<>();
		for (OperationUseProvider provider : _useProviders) {
			try {
				result.addAll(provider.getOperationUses());
			} catch (RuntimeException ex) {
				Logger.error("Cannot determine the operation uses of " + provider + ".", ex,
					SecurityCoverageCheck.class);
			}
		}
		return result;
	}

	/**
	 * Writes the given coverage to the application log: an informational message per finding,
	 * followed by a summary of the analyzed types.
	 *
	 * <p>
	 * The findings report the state of the access definition, they do not report an error: which
	 * gaps an application accepts is its own decision. The log therefore stays at the informational
	 * level, so that a test asserting a clean startup log is not broken by a reported gap.
	 * </p>
	 *
	 * @param coverage
	 *        The result of {@link #analyze()}.
	 */
	public void logCoverage(List<TypeCoverage> coverage) {
		int incomplete = 0;
		for (TypeCoverage typeCoverage : coverage) {
			if (typeCoverage.findings().isEmpty()) {
				continue;
			}
			incomplete++;
			for (CoverageFinding finding : typeCoverage.findings()) {
				Logger.info(describe(finding), SecurityCoverageCheck.class);
			}
		}

		Logger.info(coverage.size() + " types analysed, " + incomplete + " with findings.",
			SecurityCoverageCheck.class);
	}

	/**
	 * The given finding as a single line of text: the type it was reported for, followed by its
	 * message in the log language.
	 *
	 * @param finding
	 *        The finding to describe.
	 * @return The line to write to the log, or to a test failure report.
	 */
	public static String describe(CoverageFinding finding) {
		TLClass type = finding.getType();
		return (type == null ? UNKNOWN_TYPE : TLModelUtil.qualifiedName(type)) + TYPE_SEPARATOR
			+ Resources.getLogInstance().getString(finding.getMessage());
	}

	/**
	 * The {@link SecurityCoverageCheck} of the running application.
	 */
	public static SecurityCoverageCheck getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link SecurityCoverageCheck}.
	 */
	public static final class Module extends TypedRuntimeModule<SecurityCoverageCheck> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton instance.
		}

		@Override
		public Class<SecurityCoverageCheck> getImplementation() {
			return SecurityCoverageCheck.class;
		}

	}

}
