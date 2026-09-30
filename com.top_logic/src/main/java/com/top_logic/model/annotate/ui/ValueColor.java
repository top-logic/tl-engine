/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The color role a value is displayed with: wherever the value appears, it is drawn as a pill
 * tinted in that role.
 *
 * <p>
 * A role names what the color is for, not the color itself; the design system decides how a role
 * looks, in each theme and each mode. {@link #NEUTRAL}, {@link #BRAND} and the four meanings
 * {@link #ERROR}, {@link #WARNING}, {@link #SUCCESS} and {@link #INFO} carry a meaning of their own.
 * The eight categories carry a meaning the application defines: they tell values apart that have
 * to be distinguishable, such as the projects of a portfolio or the series of a chart, and never say
 * by their color that something went wrong or well.
 * </p>
 *
 * @see ValueColorProvider
 */
public enum ValueColor implements ExternallyNamed {
	/** A value set apart from the text around it, without emphasis. */
	NEUTRAL("neutral"),

	/** The color of the brand: the value the application points at. */
	BRAND("brand"),

	/** A failure, a rejection, something that has to be dealt with. */
	ERROR("error"),

	/** A condition to act on before it becomes a failure. */
	WARNING("warning"),

	/** An outcome that went well. */
	SUCCESS("success"),

	/** A note, neither good nor bad. */
	INFO("info"),

	/** The first of eight categories whose meaning the application defines: a project, a division, a series of a chart - values that have to be told apart, and never say by their color that something went wrong or well. */
	CATEGORY_1("category-1"),

	/** The second category whose meaning the application defines. */
	CATEGORY_2("category-2"),

	/** The third category whose meaning the application defines. */
	CATEGORY_3("category-3"),

	/** The fourth category whose meaning the application defines. */
	CATEGORY_4("category-4"),

	/** The fifth category whose meaning the application defines. */
	CATEGORY_5("category-5"),

	/** The sixth category whose meaning the application defines. */
	CATEGORY_6("category-6"),

	/** The seventh category whose meaning the application defines. */
	CATEGORY_7("category-7"),

	/** The eighth category whose meaning the application defines. */
	CATEGORY_8("category-8");

	private static final Map<String, ValueColor> BY_NAME =
		Stream.of(values()).collect(Collectors.toMap(ValueColor::getExternalName, Function.identity()));

	private final String _externalName;

	ValueColor(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The role the given {@link ColorSpec} names.
	 *
	 * @param spec
	 *        The specification to resolve. May be <code>null</code>.
	 * @return The specified role, or <code>null</code> if the specification is absent or names no
	 *         role.
	 */
	public static ValueColor of(ColorSpec spec) {
		return spec == null ? null : spec.getRole();
	}

	/**
	 * The role with the given {@link #getExternalName() external name}.
	 *
	 * @param name
	 *        The external name, such as <code>warning</code> or <code>category-3</code>. May be
	 *        <code>null</code>.
	 * @return The role of that name, or <code>null</code> if no role has it.
	 */
	public static ValueColor byExternalName(String name) {
		return name == null ? null : BY_NAME.get(name);
	}
}
