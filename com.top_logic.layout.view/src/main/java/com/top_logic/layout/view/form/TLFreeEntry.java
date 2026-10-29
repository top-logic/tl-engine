/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotationTargetCheck;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link TLAttributeAnnotation} letting the user enter a text that is none of the options, wherever
 * the attribute is edited by choosing from a list that opens on demand.
 *
 * <p>
 * The annotated attribute is edited by a {@link SelectControlProvider select} wherever it appears,
 * unless the form field or a {@link TLInputControl} annotation names another control. It offers
 * the options of the attribute - those of an options annotation, of the referenced type, of an
 * enumeration -, and where the attribute has none, the values the edited object holds.
 * </p>
 *
 * <p>
 * For a value that is mostly one of a known set but now and then a new one: the keywords of a
 * document, the tags of a task, the place of a meeting. The user types into the list that opens on
 * demand ({@link SelectDisplay#DROPDOWN}); a text matching the label of an option, ignoring case,
 * chooses that option, any other text becomes a value of its own. A field holding several values
 * adds it to them, a field holding one value takes it as its value. A text entered twice yields
 * one value, not two. The shapes of a selection that show every option have no input to type into
 * and ignore free entry.
 * </p>
 *
 * <p>
 * The value a text becomes is the result of the {@link #getCreate() create} function. Without a
 * function, the text is read as a value of the attribute's type: a string as it is, a number in
 * the {@link FieldControlService#numberFormat(com.top_logic.model.TLStructuredTypePart) number
 * format} of the attribute, a point in time in its
 * {@link FieldControlService#dateFormat(com.top_logic.model.TLStructuredTypePart) date format}. A
 * text that is no value of that type is refused. An attribute of any other type - a reference, a
 * classification, a boolean - needs the function; without one, the annotation is rejected when the
 * model is loaded. A number or a point in time read from the text that equals an option chooses
 * that option. The values of an attribute stored as one comma separated text cannot contain a
 * comma: such a text is refused.
 * </p>
 *
 * <p>
 * A form field deviates from what the annotation says with the {@link SelectControlProvider.Config#getFreeEntry()
 * free entry} setting of its select control: it switches free entry off for that field, or makes
 * the value by a script of its own.
 * </p>
 *
 * @see SelectControlProvider
 */
@TagName(TLFreeEntry.TAG_NAME)
@InApp
public interface TLFreeEntry extends TLAttributeAnnotation, TLAnnotationTargetCheck {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** The tag name of the annotation in a model definition. */
	String TAG_NAME = "free-entry";

	/** Configuration name for {@link #getCreate()}. */
	String CREATE = "create";

	/**
	 * The function making the value a text the user entered becomes.
	 *
	 * <p>
	 * A TL-Script function taking the entered text without leading and trailing white space and the
	 * edited object (nothing where no object is edited yet). Typically it creates a new object named
	 * by the text, which becomes the value of a reference:
	 * </p>
	 *
	 * <pre>
	 * text -&gt; self -&gt; new(`my:Keyword`, transient: true)..set(`my:Keyword#name`, $text)
	 * </pre>
	 *
	 * <p>
	 * The new object is created as transient object, as the editing of a form keeps everything the
	 * user changes: storing the form creates it, cancelling the form drops it. A text matching the
	 * label of an option, of a value of the field, or of a value created before, ignoring case,
	 * chooses that value without the function being called. A result of nothing leaves the value of
	 * the field unchanged.
	 * </p>
	 *
	 * <p>
	 * Without a function, the text is read as a value of the attribute's type, which works for
	 * strings, numbers and points in time only.
	 * </p>
	 */
	@Name(CREATE)
	Expr getCreate();

	/** @see #getCreate() */
	void setCreate(Expr value);

	/**
	 * Rejects the annotation without a {@link #getCreate() create function} on an attribute whose
	 * values cannot be read from a text.
	 */
	@Override
	default String checkTarget(TLTypePart target) {
		if (getCreate() != null || TextConversion.takesText(target.getType())) {
			return null;
		}
		return "The values of '" + TLModelUtil.qualifiedName(target) + "' of type '"
			+ TLModelUtil.qualifiedName(target.getType()) + "' cannot be read from a text, and no '" + CREATE
			+ "' function makes a value from an entered text.";
	}

}
