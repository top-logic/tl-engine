/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.DefaultSimpleCompositeControlRenderer;
import com.top_logic.layout.form.control.OnVisibleControl;

/**
 * Expression language for constructing dynamic forms in a declarative way.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Forms {

	private static final String CSS_FRM_GROUP = "frmGroup";

	private static final String CSS_FRM_KEY_VALUE = "frmKeyValue";

	private static final String CSS_FRM_SUB_GROUP = "frmSubGroup";

	private static final String CSS_FRM_VALUE = "frmValue";

	/** Creates a display of the label, value and error of the given field. */
	public static Control keyValue(FormField field) {
		return keyValues(field,
			Fragments.label(field),
			Fragments.value(field),
			Fragments.error(field));
	}

	/**
	 * Creates a key-value display of the given label and values, visible only if the given field
	 * is visible.
	 */
	public static Control keyValues(FormMember field, Control label, Control... values) {
		return onvisible(field,
			keyValueContainer(
				label,
				valueContainer(values)));
	}

	/** Creates a sub group containing the given contents. */
	public static Control subGroup(Control... contents) {
		return span(CSS_FRM_SUB_GROUP, contents);
	}

	/** Creates a key-value container containing the given contents. */
	public static Control keyValueContainer(Control... contents) {
		return span(CSS_FRM_KEY_VALUE, contents);
	}

	/** Creates a value container containing the given contents. */
	public static Control valueContainer(Control... contents) {
		return span(CSS_FRM_VALUE, contents);
	}

	/**
	 * @deprecated Use {@link Fragments#value(FormMember)}
	 */
	@Deprecated
	public static Control value(FormMember field) {
		return Fragments.value(field);
	}

	/**
	 * @deprecated Use {@link Fragments#error(FormMember)}
	 */
	@Deprecated
	public static Control error(FormField field) {
		return Fragments.error(field);
	}

	/**
	 * Creates a {@code div} with the given CSS class, visible only if the given member is visible.
	 */
	public static OnVisibleControl div(FormMember model, String cssClass, Control... contents) {
		return onvisible(model, div(cssClass, contents));
	}

	/**
	 * Creates a {@code span} with the given CSS class, visible only if the given member is visible.
	 */
	public static OnVisibleControl span(FormMember model, String cssClass, Control... contents) {
		return onvisible(model, span(cssClass, contents));
	}

	/** Creates a control displaying the given text. */
	public static Control text(final ResKey resourceKey) {
		return new AbstractConstantControl() {
			@Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
				out.writeText(context.getResources().getString(resourceKey));
			}
		};
	}

	/** Creates a control displaying the given contents, only if the given member is visible. */
	public static OnVisibleControl onvisible(FormMember model, Control... contents) {
		OnVisibleControl result = new OnVisibleControl(model);
		contents(result, contents);
		return result;
	}

	/** Creates a {@code span} with the given CSS class containing the given contents. */
	public static BlockControl span(String cssClass, Control... contents) {
		BlockControl result = block(contents);
		result.setRenderer(DefaultSimpleCompositeControlRenderer.spanWithCSSClass(cssClass));
		return result;
	}

	/** Creates a floating group containing the given contents. */
	public static BlockControl floatingGroup(Control... contents) {
		return div(CSS_FRM_GROUP, contents);
	}

	/** Creates a {@code div} with the given CSS class containing the given contents. */
	public static BlockControl div(String cssClass, Control... contents) {
		BlockControl result = block(contents);
		result.setRenderer(DefaultSimpleCompositeControlRenderer.divWithCSSClass(cssClass));
		return result;
	}

	/** Creates a block containing the given contents. */
	public static BlockControl block(Control... contents) {
		BlockControl result = new BlockControl();
		contents(result, contents);
		return result;
	}

	private static void contents(AbstractCompositeControl result, Control... contents) {
		for (Control control : contents) {
			result.addChild(control);
		}
	}

	/**
	 * @deprecated Use {@link Fragments#label(FormMember)}
	 */
	@Deprecated
	public static Control label(FormMember member) {
		return Fragments.label(member);
	}

}
