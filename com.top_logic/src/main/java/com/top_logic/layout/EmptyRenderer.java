/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * {@link Renderer} that does not write anything.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EmptyRenderer<T> implements Renderer<T> {

	/** Singleton {@link EmptyRenderer} instance. */
	@SuppressWarnings("rawtypes")
	static final EmptyRenderer INSTANCE = new EmptyRenderer();

	/**
	 * Creates a new {@link EmptyRenderer}.
	 */
	protected EmptyRenderer() {
		// singleton instance
	}

	/**
	 * Delivers a {@link Renderer} that writes nothing.
	 *
	 * @param <T>
	 *        Type of the values delivered to the requested renderer.
	 */
	@SuppressWarnings("unchecked")
	public static <T> EmptyRenderer<T> getInstance() {
		return INSTANCE;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, T value) throws IOException {
		// Empty
	}

	@Override
	public Renderer<Object> generic() {
		@SuppressWarnings("unchecked")
		Renderer<Object> generic = (Renderer<Object>) this;
		return generic;
	}

	@Override
	public <X> Renderer<? super X> generic(Class<X> expectedType) {
		@SuppressWarnings("unchecked")
		Renderer<? super X> generic = (Renderer<? super X>) this;
		return generic;
	}

}
