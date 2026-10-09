/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.resource;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A client resource that is a CSS stylesheet.
 *
 * <p>
 * A stylesheet without {@link #getLayer() layer} is emitted as a {@code <link rel="stylesheet">}
 * and its rules are unlayered. A stylesheet with a layer is emitted as a {@code <style>} element
 * importing it into that CSS cascade layer.
 * </p>
 */
public interface StyleSheetConfig extends ResourceConfig {

	/** Element tag of a stylesheet resource declaration. */
	String TAG_NAME = "stylesheet";

	/** Configuration name for {@link #getResource()}. */
	String RESOURCE = "resource";

	/**
	 * The context-relative path of the stylesheet, e.g. {@code "/style/tlReactControls.css"}, or a
	 * {@code webjar:} reference.
	 */
	@Name(RESOURCE)
	@Mandatory
	String getResource();

	/** Configuration name for {@link #getLayer()}. */
	String LAYER = "layer";

	/**
	 * The CSS cascade layer the rules of this stylesheet belong to.
	 *
	 * <p>
	 * The layer must be one of the {@link ClientResources.Config#getLayers()}.
	 * </p>
	 *
	 * <p>
	 * The stylesheets of the engine and of the libraries it bundles belong to the layer
	 * {@value ClientResources#ENGINE_LAYER}. Without a layer, the rules of the stylesheet are
	 * unlayered and win against the rules of every layer, whatever their specificity; this is the
	 * place of the stylesheets of an application.
	 * </p>
	 */
	@Name(LAYER)
	String getLayer();

}
