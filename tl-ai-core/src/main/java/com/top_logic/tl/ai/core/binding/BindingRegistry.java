/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.binding;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of all {@link ThreadLocalBinding} instances.
 *
 * <p>
 * Each {@link ThreadLocalBinding} registers itself on construction. {@link BindingSnapshot} uses
 * the registry to capture and re-establish every binding without callers having to enumerate them,
 * removing the "forgot to add the new binding in two places" failure mode.
 * </p>
 */
public final class BindingRegistry {

	private static final List<ThreadLocalBinding<?>> BINDINGS = new CopyOnWriteArrayList<>();

	private BindingRegistry() {
		// Static utility.
	}

	static void register(ThreadLocalBinding<?> binding) {
		BINDINGS.add(binding);
	}

	/**
	 * All currently registered bindings.
	 */
	static List<ThreadLocalBinding<?>> all() {
		return BINDINGS;
	}
}
