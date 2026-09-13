/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.annotate.TLClassifierAnnotation;

/**
 * The color the annotated enumeration literal is displayed with.
 *
 * <p>
 * A colored literal is what tells a status, a priority or a severity apart at a glance. A literal
 * without this annotation has no color.
 * </p>
 *
 * @see ColorSpec
 * @see TLDynamicColor
 *
 * @implNote {@link ValueColorProvider#colorOf(Object)} answers this annotation for a
 *           {@link TLClassifier}.
 */
@TagName(TLColor.TAG_NAME)
@InApp
@DisplayOrder({
	ColorSpec.VALUE,
	ColorSpec.TOKEN,
})
public interface TLColor extends TLClassifierAnnotation, ColorSpec {

	/** Name of the tag defining a {@link TLColor} annotation. */
	String TAG_NAME = "color";

	// Marker interface.

}
