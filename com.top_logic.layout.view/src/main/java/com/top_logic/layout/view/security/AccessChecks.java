/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.function.Function;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelCondition;

/**
 * Shared access-control enforcement for UI units (navigation items, sidebar groups, tabs, accordion
 * sections, tiles).
 *
 * <p>
 * Every securable unit runs the same steps, each accepting {@code null} for a unit without
 * {@link AccessControl}:
 * </p>
 * <ol>
 * <li>{@link #isOffered(UnitAccess)} when the unit is built: a unit the user can never see is
 * omitted.</li>
 * <li>{@link #contentContext(UnitAccess, ViewContext)} when its content context is created: the
 * {@link SecuredObject} of the unit is published to the content.</li>
 * <li>{@link #guard(UnitAccess, ChannelCondition, ViewContext, Function)} when the content is
 * created: the content is displayed as the access check decides for the current object of the
 * unit.</li>
 * <li>{@link #hiding(UnitAccess, ChannelCondition, ViewContext)} when the container builds the
 * entry of the unit: a unit {@link DeniedUnitDisplay#HIDE hidden} while it is denied is left out of
 * its container for as long as the check fails on its object.</li>
 * </ol>
 *
 * <p>
 * The last two steps also take the visibility condition of the unit, if it has one: while it does
 * not hold, the unit is left out of its container and shows no content, and the access check is
 * not consulted.
 * </p>
 */
public class AccessChecks {

	/**
	 * Whether the UI unit with the given access check is displayed to the current user at all.
	 *
	 * @param access
	 *        The access check of the unit, {@code null} when the unit is not guarded.
	 * @return {@code true} when the unit is built.
	 *
	 * @see UnitAccess#isOffered()
	 */
	public static boolean isOffered(UnitAccess access) {
		return access == null || access.isOffered();
	}

	/**
	 * The context the content of a UI unit with the given access check is built in.
	 *
	 * <p>
	 * Publishes the {@link SecuredObject} of the unit in the given context, so that commands in the
	 * content can check their operation on it (see {@link SecuredObject#of(ViewContext)}).
	 * </p>
	 *
	 * @param access
	 *        The access check of the unit, {@code null} when the unit is not guarded.
	 * @param context
	 *        The context of the content without the unit's object.
	 */
	public static ViewContext contentContext(UnitAccess access, ViewContext context) {
		if (access == null) {
			return context;
		}
		return context.withScope(SecuredObject.class, access.bind(context));
	}

	/**
	 * The state telling whether a UI unit is currently left out of its container: while its
	 * visibility condition does not hold, or while its operation is refused on its object and it is
	 * configured to be {@link DeniedUnitDisplay#HIDE hidden} then.
	 *
	 * <p>
	 * Each container implements the state once for its kind of entry: it builds the entry as
	 * {@link UnitHiding#isHidden() hidden} or not, and {@link UnitHiding#bind(ReactControl,
	 * java.util.function.Consumer) binds} the state to its control to draw or leave out the entry
	 * when the state changes.
	 * </p>
	 *
	 * @param access
	 *        The access check of the unit, {@code null} when the unit is not guarded.
	 * @param visible
	 *        The visibility condition of the unit bound to the session, {@code null} when the unit
	 *        has none.
	 * @param context
	 *        The context the unit is displayed in.
	 * @return {@code null} when the unit is never left out: it has no visibility condition, and it
	 *         is not guarded, is checked on the security root, or shows a placeholder when denied.
	 */
	public static UnitHiding hiding(UnitAccess access, ChannelCondition visible, ViewContext context) {
		boolean hidesDenied =
			access != null && access.isReactive() && access.getDenied() == DeniedUnitDisplay.HIDE;
		if (!hidesDenied) {
			if (visible == null) {
				return null;
			}
			return new UnitHiding(visible, null, null, context);
		}
		return new UnitHiding(visible, access, access.bind(context), context);
	}

	/**
	 * Creates the content of a UI unit with the given access check.
	 *
	 * <p>
	 * For a check over an object, the content is wrapped in an {@link AccessGuardControl} that
	 * displays it while the operation is allowed on the current object. While the given visibility
	 * condition does not hold, the guard shows no content and does not consult the check.
	 * </p>
	 *
	 * @param access
	 *        The access check of the unit, {@code null} when the unit is not guarded.
	 * @param visible
	 *        The visibility condition of the unit bound to the session, {@code null} when the unit
	 *        has none.
	 * @param contentContext
	 *        The context created by {@link #contentContext(UnitAccess, ViewContext)}.
	 * @param content
	 *        Creates the content in the given context.
	 */
	public static ReactControl guard(UnitAccess access, ChannelCondition visible, ViewContext contentContext,
			Function<ViewContext, ReactControl> content) {
		if (access == null || !access.isReactive()) {
			return content.apply(contentContext);
		}
		return new AccessGuardControl(contentContext, access, access.bind(contentContext), visible,
			() -> content.apply(contentContext));
	}

}
