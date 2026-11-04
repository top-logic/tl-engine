/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link TLAttributeAnnotation} naming the unit a number is measured in, written after the value
 * wherever the number is shown or edited: {@code 120 min}, {@code 3,5 kg}.
 *
 * <pre>
 * &lt;unit value="'min'"/&gt;
 * </pre>
 *
 * <p>
 * The unit is display only. The user does not type it, it is not part of the format the number is
 * written in, and it does not change the value: an attribute holding minutes holds the number of
 * minutes, with or without the unit. No value is converted from one unit to another.
 * </p>
 *
 * <p>
 * Annotated at a numeric type, the unit applies to every attribute of that type; an annotation at
 * an attribute wins over the one of its type. A form field states a unit of its own with the
 * {@link NumberInputControlProvider.Config#getUnit() unit} of its number input, which wins over
 * the annotation.
 * </p>
 *
 * @see FieldControlService#unit(com.top_logic.model.annotate.AnnotationLookup,
 *      com.top_logic.model.TLType, Object)
 */
@TagName(TLUnit.TAG_NAME)
@InApp
@TargetType({ TLTypeKind.INT, TLTypeKind.FLOAT })
public interface TLUnit extends TLAttributeAnnotation, TLTypeAnnotation {

	/** The tag name of the annotation in a model definition. */
	String TAG_NAME = "unit";

	/** Configuration name for {@link #getValue()}. */
	String VALUE = "value";

	/**
	 * The unit written after the value.
	 *
	 * <p>
	 * A TL-Script expression: a constant for a unit that is the same for every object, such as
	 * {@code 'min'}, or {@code #("min"@en, "Min."@de)} for a unit written differently in each
	 * language. For a unit that depends on the object, a function taking the object owning the
	 * attribute, such as {@code x -> $x.get(`my:Measurement#unit`)}.
	 * </p>
	 *
	 * <p>
	 * An internationalized text is written in the language of the user, any other value as its
	 * text. A result of nothing or an empty text writes no unit.
	 * </p>
	 *
	 * <p>
	 * The unit is computed when the field showing the value is created, and again when the field
	 * switches to another object. Where no object owns
	 * the value - a field outside of a form, for example - a function is not called and no unit is
	 * written, while a constant still is. The expression is evaluated without access checks: the
	 * unit is part of how a value is written, not data the user reads.
	 * </p>
	 */
	@Name(VALUE)
	@Mandatory
	Expr getValue();

	/** @see #getValue() */
	void setValue(Expr value);

}
