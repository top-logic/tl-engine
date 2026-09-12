/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.view.ViewContext;

/**
 * Strategy producing the breadcrumb label of a {@link TileFrame} at push time.
 *
 * <p>
 * The result is captured into {@link TileFrame#getLabel()} once when the frame is created and
 * thereafter does not re-evaluate. To compute a label that depends on a value already on a
 * channel of the caller's scope (e.g. the name of the selected business object), pick the
 * {@link ScriptedTileLabel} implementation; for fixed text use {@link StaticTileLabel}.
 * </p>
 */
public interface TileLabelProvider {

	/**
	 * Computes the breadcrumb label.
	 *
	 * @param context
	 *        The {@link ViewContext} of the call site (the frame from which the push originates).
	 *        Use it to resolve channels referenced by the provider.
	 * @return The label, or {@code null} for an unlabeled frame.
	 */
	ResKey compute(ViewContext context);

	/**
	 * The label a computed value stands for.
	 *
	 * @param value
	 *        The result of a label computation: a {@link ResKey}, a {@link String}, a business
	 *        object, or nothing.
	 * @return The label to announce a frame with, or {@code null} for an unlabeled frame.
	 *
	 * @implNote A {@link ResKey} (e.g. an {@code I18NString} attribute value) is answered as-is so
	 *           that it stays localizable; a {@link String} is wrapped via
	 *           {@link ResKey#text(String)}; any other object is labeled via
	 *           {@link MetaLabelProvider}.
	 */
	static ResKey toLabel(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof ResKey key) {
			return key;
		}
		if (value instanceof String text) {
			return ResKey.text(text);
		}
		return ResKey.text(MetaLabelProvider.INSTANCE.getLabel(value));
	}

	/**
	 * Configuration for {@link TileLabelProvider}.
	 */
	interface Config<I extends TileLabelProvider> extends PolymorphicConfiguration<I> {
		// No common properties.
	}
}
