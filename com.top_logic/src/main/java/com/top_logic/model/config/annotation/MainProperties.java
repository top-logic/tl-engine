/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config.annotation;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.InAppClassifierConstants;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.PartNamesOptionProvider;
import com.top_logic.layout.table.provider.ColumnOptionLabelProvider;
import com.top_logic.layout.table.provider.ColumnOptionMapping;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} for defining the default columns of a {@link TLClass} when instances are
 * displayed in a table.
 * 
 * <p>
 * Alternatively to defining the default columns as type annotation, they can be configured in the
 * application configuration for a certain module, see
 * <code>com.top_logic.element.model.DynamicModelService.Config.ModuleSetting#getMainProperties()</code>.
 * </p>
 * 
 * @see #getProperties()
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Label("Default columns")
@TagName("main-properties")
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
@InApp(classifiers = { InAppClassifierConstants.FORM_RELEVANT })
public interface MainProperties extends TLAttributeAnnotation, TLTypeAnnotation {

	/** Property name of {@link #getProperties()}. */
	String PROPERTIES = "properties";

	/**
	 * The attributes of the annotated type that should be displayed as columns when instances of
	 * the annotated type are presented as table.
	 */
	@Name(PROPERTIES)
	@Options(fun = PartNamesOptionProvider.class, mapping = ColumnOptionMapping.class)
	@OptionLabels(value = ColumnOptionLabelProvider.class)
	@Format(CommaSeparatedStrings.class)
	List<String> getProperties();

}
