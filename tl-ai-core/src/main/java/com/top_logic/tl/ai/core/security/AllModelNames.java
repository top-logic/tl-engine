/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.func.Function0;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelGrant;
import com.top_logic.tl.ai.core.security.ToolAccessPolicy.ModelRoleAccess;

/**
 * The language models an administrator can pick from when writing a {@link ToolAccessPolicy}.
 *
 * <p>
 * Every model this application has configured, plus every model name the policy in effect already
 * stores - a model an administrator wrote the policy for before configuring it stays offered, so that
 * opening the editor and saving it cannot drop the entry.
 * </p>
 *
 * @see AllAgentNames
 * @see AllMcpServerNames
 */
public class AllModelNames extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		return union(configuredModelNames(), AiToolAccessService.policy());
	}

	/** The names of the models the application has configured, empty while no provider service runs. */
	static Collection<String> configuredModelNames() {
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			return List.of();
		}
		return LLMProviderService.getInstance().getConfiguredModelNames();
	}

	/**
	 * The models to offer: the configured ones, and the ones the policy already names.
	 *
	 * @param configuredModels
	 *        The names of the models the application has configured.
	 * @param policy
	 *        The policy in effect, may be <code>null</code> if the service is not running.
	 * @return The option list, alphabetically and without duplicates.
	 */
	static List<String> union(Collection<String> configuredModels, ToolAccessPolicy policy) {
		Set<String> names = new TreeSet<>();
		for (String name : configuredModels) {
			if (name != null && !name.isBlank()) {
				names.add(name);
			}
		}
		if (policy != null) {
			for (ModelRoleAccess role : policy.getModelAccess().getRoles().values()) {
				addNames(names, role.getGrants());
				addNames(names, role.getDenies());
			}
		}
		return new ArrayList<>(names);
	}

	private static void addNames(Set<String> names, Collection<ModelGrant> entries) {
		for (ModelGrant entry : entries) {
			names.addAll(entry.getModels());
		}
	}

}
