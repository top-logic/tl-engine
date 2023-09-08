/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;

/**
 * Demonstrated how the different types are displayed.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface TypeDemos extends ConfigurationItem {

	String PRIMITIVE_TYPES = "primitive-types";

	String CONSTRAINTS = "constraints";

	String PRIMITIVE_OBJECT_TYPES = "primitive-object-types";

	String PRIMITIVE_OBJECT_TYPES_NON_NULL_DEFAULT = "primitive-object-types-non-null-default";

	String NON_NULLABLE_OBJECT_TYPES = "non-nullable-object-types";

	String NULLABLE_OBJECT_TYPES = "nullable-object-types";

	String NULLABLE_OBJECT_TYPES_WITH_NULL_DEFAULT = "nullable-object-types-with-null-default";

	String NULLABLE_OBJECT_TYPES_WITH_NON_NULL_DEFAULT = "nullable-object-types-with-non-null-default";

	String OBJECT_TYPES_MANDATORY = "object-types-mandatory";

	String OBJECT_TYPES_READ_ONLY = "object-types-read-only";

	String OBJECT_TYPES_OPTIONS = "object-types-options";

	/** Configuration name for the value of {@link #getObjectTypesWholeLine()}. */
	String OBJECT_TYPES_WHOLE_LINE = "object-types-whole-line";

	@NonNullable
	@ItemDefault
	@Name(PRIMITIVE_TYPES)
	PrimitiveTypes getPrimitiveTypes();

	@NonNullable
	@ItemDefault
	@Name(CONSTRAINTS)
	@DisplayMinimized
	DemoDeclarativeConstraints getConstraints();

	@NonNullable
	@ItemDefault
	@Name(PRIMITIVE_OBJECT_TYPES)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	PrimitiveObjectTypes getPrimitiveObjectTypes();

	@NonNullable
	@ItemDefault
	@Name(PRIMITIVE_OBJECT_TYPES_NON_NULL_DEFAULT)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	PrimitiveObjectTypesNonNullDefault getPrimitiveObjectTypesNonNullDefault();

	@NonNullable
	@ItemDefault
	@Name(NON_NULLABLE_OBJECT_TYPES)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NonNullableObjectTypes getNonNullableObjectTypes();

	@NonNullable
	@ItemDefault
	@Name(NULLABLE_OBJECT_TYPES)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NullableObjectTypes getNullableObjectTypes();

	@NonNullable
	@ItemDefault
	@Name(NULLABLE_OBJECT_TYPES_WITH_NULL_DEFAULT)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NullableObjectTypesWithNullDefault getNullableObjectTypesWithNullDefault();

	@NonNullable
	@ItemDefault
	@Name(NULLABLE_OBJECT_TYPES_WITH_NON_NULL_DEFAULT)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NullableObjectTypesWithNonNullDefault getNullableObjectTypesWithNonNullDefault();

	@NonNullable
	@ItemDefault
	@Name(OBJECT_TYPES_MANDATORY)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ObjectTypesMandatory getObjectTypesMandatory();

	@NonNullable
	@ItemDefault
	@Name(OBJECT_TYPES_READ_ONLY)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ObjectTypesReadOnly getObjectTypesReadOnly();

	@NonNullable
	@ItemDefault
	@Name(OBJECT_TYPES_OPTIONS)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ObjectTypesOptions getObjectTypesOptions();

	/**
	 * {@link TypeDemos} part for demonstrating how object values are displayed when rendered over
	 * whole line.
	 */
	@NonNullable
	@ItemDefault
	@Name(OBJECT_TYPES_WHOLE_LINE)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ObjectTypesWholeLine getObjectTypesWholeLine();

}
