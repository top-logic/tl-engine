/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function0;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.StringAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} that defines an additional scope name for the definition of
 * enumerations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("enum-scope")
@TargetType(TLTypeKind.ENUMERATION)
@InApp
public interface EnumScope extends StringAnnotation, TLTypeAnnotation {

	/**
	 * The default scope name, that is assumed, if this annotation is not given.
	 */
	String DEFAULT_ENUM_SCOPE = "Classification";

	/**
	 * The name of the scope of the annotated enumeration.
	 * 
	 * <p>
	 * The scope of the enumeration controls whether the enumeration is displayed in a certain
	 * context.
	 * </p>
	 * 
	 * @see com.top_logic.knowledge.gui.layout.list.ClassificationTableModelBuilder.Config#getType()
	 */
	@Override
	@Options(fun = AllClassificationTypes.class)
	String getValue();

	/**
	 * Options for {@link EnumScope#getValue()}.
	 */
	class AllClassificationTypes extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return new ArrayList<>(FastList.getAllListTypes());
		}
	}

}
