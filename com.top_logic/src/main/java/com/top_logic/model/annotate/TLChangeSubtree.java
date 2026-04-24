/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * Qualifies a reference end as part of a change-subtree relationship.
 *
 * <p>
 * Placed on a reference end {@code E}, the owner of {@code E} is the <em>root side</em>, and the
 * target type of {@code E} is the <em>child side</em>. Changes to instances of the child type
 * that are reachable through this reference end from a root instance are considered part of the
 * change-subtree of that root.
 * </p>
 *
 * <p>
 * The annotation has a different default depending on the kind of reference it sits on:
 * </p>
 * <ul>
 * <li>On the <em>composite end</em> of a composition reference, the annotation defaults to
 * {@code include=true} (implicit). Setting {@code include=false} on any end of the pair opts
 * out of change-subtree membership for that composition.</li>
 * <li>On a non-composition reference end, the annotation is required to opt in and defaults to
 * {@code include=true} when present. Without the annotation, a non-composition reference does
 * not qualify.</li>
 * </ul>
 *
 * <p>
 * Both ends of a reference pair may be annotated independently; each annotation defines its own
 * root direction. Annotating both ends with {@code include=true} means the relation qualifies in
 * both directions.
 * </p>
 *
 * @see com.top_logic.element.changelog.SubtreeFilter
 */
@InApp
@TagName("change-subtree")
@TargetType({ TLTypeKind.REF, TLTypeKind.COMPOSITION })
public interface TLChangeSubtree extends TLAttributeAnnotation {

	/**
	 * @see #getInclude()
	 */
	String INCLUDE = "include";

	/**
	 * Whether the reference end this annotation sits on qualifies as the root side of a
	 * change-subtree relationship.
	 *
	 * <p>
	 * When {@code true}, changes to instances reachable through this reference end belong to the
	 * change-subtree of the instance owning the end.
	 * </p>
	 *
	 * <p>
	 * For composition references the composite end defaults to {@code true} (opt-out with
	 * {@code false}). For non-composition references the default is {@code false} (opt-in with
	 * {@code true}).
	 * </p>
	 */
	@Name(INCLUDE)
	@BooleanDefault(true)
	boolean getInclude();

}
