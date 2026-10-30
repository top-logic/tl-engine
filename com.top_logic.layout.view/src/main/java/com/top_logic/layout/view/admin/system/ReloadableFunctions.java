/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.ReloadableManager.ReloadFailure;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.Resources;

/**
 * TL-Script functions about the parts of the application that reload their configuration on
 * request, the {@link Reloadable}s registered with the {@link ReloadableManager}.
 *
 * <p>
 * A {@link Reloadable} is identified by its key, the {@link Reloadable#getName() name} it is
 * registered under. {@link #ofService(BasicRuntimeModule)} gives the key of a service that is a
 * {@link Reloadable}, the other functions describe or reload the {@link Reloadable}s given by their
 * keys, and {@link #report(ReloadOutcome)} tells the user what became of a reload, including why a
 * {@link Reloadable} could not be reloaded. A key no {@link Reloadable} is registered under is
 * described by nothing and cannot be reloaded.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #reload(Collection)} is called <code>reloadableReload</code>. Reloading
 *           changes the state of the running application, which only an administrator may do from
 *           a script entered interactively.
 */
@ScriptPrefix(ReloadableFunctions.PREFIX)
@AdminOnly
public class ReloadableFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "reloadable";

	/** Separator between the labels joined by {@link #labels(Collection)}. */
	public static final String LABEL_SEPARATOR = ", ";

	/**
	 * The key of the {@link Reloadable} a service is.
	 *
	 * @param module
	 *        The module of the service.
	 * @return The key the running instance of the service is registered under as
	 *         {@link Reloadable}, <code>null</code> if the service is not running, is no
	 *         {@link Reloadable} or is not registered as one.
	 */
	@Label("Key of the reloadable a service is")
	public static String ofService(BasicRuntimeModule<?> module) {
		if (module == null || !module.isActive()) {
			return null;
		}
		if (!(module.getImplementationInstance() instanceof Reloadable reloadable)) {
			return null;
		}
		String key = reloadable.getName();
		if (StringServices.isEmpty(key) || !manager().isReloadableRegistered(reloadable)) {
			return null;
		}
		return key;
	}

	/**
	 * The name of the kind of the {@link Reloadable} registered under the given key.
	 *
	 * @param key
	 *        The key of the {@link Reloadable}.
	 * @return The label of the implementation of the {@link Reloadable}, <code>null</code> if
	 *         none is registered under the given key.
	 */
	@Label("Name of a reloadable")
	public static String label(String key) {
		Reloadable reloadable = reloadable(key);
		return reloadable == null ? null : I18NClassNameProvider.INSTANCE.getLabel(reloadable.getClass());
	}

	/**
	 * The names of the {@link Reloadable}s registered under the given keys, for a message to the
	 * user.
	 *
	 * @param keys
	 *        The keys of the {@link Reloadable}s.
	 * @return The {@link #label(String) names} in the order of the given keys, each followed by
	 *         its key in parentheses, joined by {@link #LABEL_SEPARATOR}.
	 */
	@Label("Names of reloadables")
	public static String labels(Collection<String> keys) {
		if (keys == null) {
			return "";
		}
		return keys.stream()
			.map(key -> {
				String label = label(key);
				return label == null ? key : label + " (" + key + ")";
			})
			.collect(Collectors.joining(LABEL_SEPARATOR));
	}

	/**
	 * Reloads the {@link Reloadable}s registered under the given keys.
	 *
	 * <p>
	 * A {@link Reloadable} whose reload fails does not keep the others from being reloaded.
	 * </p>
	 *
	 * @param keys
	 *        The keys of the {@link Reloadable}s to reload.
	 * @return Which {@link Reloadable}s were reloaded and why the others could not be. A key no
	 *         {@link Reloadable} is registered under counts as failed.
	 *
	 * @see ReloadableManager#reloadKnownReporting(Collection)
	 * @see #report(ReloadOutcome)
	 */
	@Label("Reload reloadables")
	public static ReloadOutcome reload(Collection<String> keys) {
		List<String> given = keys == null ? List.of() : List.copyOf(keys);
		Map<String, ReloadFailure> failures =
			given.isEmpty() ? Map.of() : manager().reloadKnownReporting(given);
		List<String> reloaded = given.stream().filter(key -> !failures.containsKey(key)).toList();
		return new ReloadOutcome(reloaded, failures);
	}

	/**
	 * Whether a {@link Reloadable} could not be reloaded.
	 *
	 * @param outcome
	 *        The result of {@link #reload(Collection)}.
	 * @return Whether the outcome reports a failure, <code>false</code> without an outcome.
	 */
	@Label("Whether reloading failed")
	public static boolean failed(ReloadOutcome outcome) {
		return outcome != null && outcome.failed();
	}

	/**
	 * What the user is told about the given reload.
	 *
	 * @param outcome
	 *        The result of {@link #reload(Collection)}.
	 * @return The {@link #labels(Collection) names} of the reloaded {@link Reloadable}s, followed
	 *         by one line for each {@link Reloadable} that could not be reloaded, naming it and
	 *         the reason. <code>null</code> without an outcome.
	 */
	@Label("Report of a reload")
	public static String report(ReloadOutcome outcome) {
		if (outcome == null) {
			return null;
		}
		Resources resources = Resources.getInstance();
		StringBuilder report = new StringBuilder();
		if (!outcome.reloaded().isEmpty()) {
			report.append(resources.getString(I18NConstants.RELOAD_DONE__NAMES.fill(labels(outcome.reloaded()))));
		}
		if (outcome.failed()) {
			if (report.length() > 0) {
				report.append('\n');
			}
			report.append(resources.getString(I18NConstants.RELOAD_FAILED));
			for (Entry<String, ReloadFailure> failure : outcome.failures().entrySet()) {
				report.append('\n');
				report.append(resources.getString(I18NConstants.RELOAD_FAILURE__NAME_REASON
					.fill(labels(List.of(failure.getKey())), failure.getValue().reason())));
			}
		}
		return report.toString();
	}

	private static Reloadable reloadable(String key) {
		return key == null ? null : manager().get(key);
	}

	private static ReloadableManager manager() {
		return ReloadableManager.getInstance();
	}

}
