/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.routing.ParamBindingConfig;
import com.top_logic.model.TLStructuredType;

/**
 * Strategy producing the breadcrumb label of a {@link TileFrame} at push time.
 *
 * <p>
 * The result is captured into {@link TileFrame#getLabel()} when the frame is created. To compute a
 * label that depends on a value already on a channel of the caller's scope (e.g. the name of the
 * selected business object), pick the {@link ScriptedTileLabel} implementation; for fixed text use
 * {@link StaticTileLabel}.
 * </p>
 *
 * <p>
 * A user of the provider that names something for as long as it is displayed - the
 * {@link FrameRouteConfig#getLabel() declaration of a frame} naming it in the breadcrumb, a
 * {@link ParamBindingConfig#getLabel() route parameter} naming the page in the browser tab -
 * computes the label again whenever one of the {@link #observedChannels(ViewContext) observed
 * channels} takes a new value, whenever one of the objects these channels hold is changed, and
 * whenever an object of one of the {@link #observedTypes() observed types} is created, changed or
 * deleted (see {@link com.top_logic.layout.view.model.ChannelObjectObserver}).
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
	 * The channels the label {@link #compute(ViewContext) computed} in the given context depends
	 * on.
	 *
	 * <p>
	 * A user of the provider that keeps a label up to date follows them and computes the label
	 * again whenever one takes a new value, so that the label follows what the channels hold.
	 * </p>
	 *
	 * @param context
	 *        The context the label is computed in, the same one {@link #compute(ViewContext)} is
	 *        asked with.
	 * @return The channels; empty for a label that is the same for the whole session.
	 */
	default List<ViewChannel> observedChannels(ViewContext context) {
		return List.of();
	}

	/**
	 * The types whose objects the label reads beyond the objects on the
	 * {@link #observedChannels(ViewContext) observed channels}.
	 *
	 * <p>
	 * A user of the provider that keeps a label up to date computes the label again whenever an
	 * object of one of these types is created, changed or deleted - so that a label reading an object
	 * it reaches only through the object on a channel (the customer of a project, say) follows that
	 * object being renamed.
	 * </p>
	 *
	 * @return The types; empty for a label that reads no object beyond those on its channels.
	 */
	default Set<TLStructuredType> observedTypes() {
		return Set.of();
	}

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
