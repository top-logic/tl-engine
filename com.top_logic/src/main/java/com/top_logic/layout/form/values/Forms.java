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

	public static Control keyValue(FormField field) {
		return keyValues(field,
			Fragments.label(field),
			Fragments.value(field),
			Fragments.error(field));
	}

	public static Control keyValues(FormMember field, Control label, Control... values) {
		return onvisible(field,
			keyValueContainer(
				label,
				valueContainer(values)));
	}

	public static Control subGroup(Control... contents) {
		return span(CSS_FRM_SUB_GROUP, contents);
	}

	public static Control keyValueContainer(Control... contents) {
		return span(CSS_FRM_KEY_VALUE, contents);
	}

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

	public static OnVisibleControl div(FormMember model, String cssClass, Control... contents) {
		return onvisible(model, div(cssClass, contents));
	}

	public static OnVisibleControl span(FormMember model, String cssClass, Control... contents) {
		return onvisible(model, span(cssClass, contents));
	}

	public static Control text(final ResKey resourceKey) {
		return new AbstractConstantControl() {
			@Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
				out.writeText(context.getResources().getString(resourceKey));
			}
		};
	}

	public static OnVisibleControl onvisible(FormMember model, Control... contents) {
		OnVisibleControl result = new OnVisibleControl(model);
		contents(result, contents);
		return result;
	}

	public static BlockControl span(String cssClass, Control... contents) {
		BlockControl result = block(contents);
		result.setRenderer(DefaultSimpleCompositeControlRenderer.spanWithCSSClass(cssClass));
		return result;
	}

	public static BlockControl floatingGroup(Control... contents) {
		return div(CSS_FRM_GROUP, contents);
	}

	public static BlockControl div(String cssClass, Control... contents) {
		BlockControl result = block(contents);
		result.setRenderer(DefaultSimpleCompositeControlRenderer.divWithCSSClass(cssClass));
		return result;
	}

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
