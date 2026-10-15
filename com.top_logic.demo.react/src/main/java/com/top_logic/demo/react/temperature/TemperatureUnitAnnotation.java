/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Annotation of an attribute of type <code>demo.temperature:Temperature</code> telling in which
 * unit its value is displayed and entered.
 *
 * <pre>
 * &lt;temperature-unit unit="s -&gt; $s.container().get(`demo.temperature:DemoSite#temperatureUnit`)"/&gt;
 * </pre>
 *
 * <p>
 * An attribute without this annotation displays its value in degrees Celsius.
 * </p>
 *
 * @see TemperatureStorage
 */
@TagName(TemperatureUnitAnnotation.TAG_NAME)
public interface TemperatureUnitAnnotation extends TLAttributeAnnotation {

	/**
	 * Tag name of the annotation.
	 */
	String TAG_NAME = "temperature-unit";

	/**
	 * Configuration name of {@link #getUnit()}.
	 */
	String UNIT = "unit";

	/**
	 * Function computing the unit from the object owning the attribute.
	 *
	 * <p>
	 * The function receives the object and returns a classifier of the enumeration
	 * <code>demo.temperature:TemperatureUnit</code>. If it returns nothing, the value is displayed in
	 * degrees Celsius. The function is evaluated without access checks: the unit is part of the
	 * display, not of the data the user reads.
	 * </p>
	 */
	@Name(UNIT)
	@Mandatory
	Expr getUnit();

	/** @see #getUnit() */
	void setUnit(Expr value);

}
