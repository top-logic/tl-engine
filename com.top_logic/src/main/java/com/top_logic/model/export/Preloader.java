/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.table.model.NoPrepare;

/**
 * Composed {@link PreloadOperation} that can be built through calls to
 * {@link #addPreload(PreloadOperation)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Preloader implements PreloadBuilder, PreloadOperation {

	private final Set<PreloadOperation> preloads = new LinkedHashSet<>();

	/**
	 * Creates a {@link Preloader}.
	 */
	public Preloader() {
		super();
	}

	@Override
	public void addPreload(PreloadOperation operation) {
		preloads.add(operation);
	}

	@Override
	public AccessContext prepare(Collection<?> baseObjects) {
		if (preloads.isEmpty()) {
			return NoPrepare.INSTANCE;
		}

		return PreloadOperation.super.prepare(baseObjects);
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		if (baseObjects.isEmpty()) {
			return;
		}
		internalPrepare(context, baseObjects);
	}

	private void internalPrepare(PreloadContext context, Collection<?> baseObjects) {
		for (PreloadOperation preload : preloads) {
			preload.prepare(context, baseObjects);
		}
	}

	@Override
	public void contribute(PreloadBuilder preloadBuilder) {
		for (PreloadOperation preload : preloads) {
			preloadBuilder.addPreload(preload);
		}
	}
}
