/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Label;
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

	/** Configuration name for the value of {@link #getPrimitiveTypes()}. */
	String PRIMITIVE_TYPES = "primitive-types";

	/** Configuration name for the value of {@link #getConstraints()}. */
	String CONSTRAINTS = "constraints";

	/** Configuration name for the value of {@link #getPrimitiveObjectTypes()}. */
	String PRIMITIVE_OBJECT_TYPES = "primitive-object-types";

	/** Configuration name for the value of {@link #getPrimitiveObjectTypesNonNullDefault()}. */
	String PRIMITIVE_OBJECT_TYPES_NON_NULL_DEFAULT = "primitive-object-types-non-null-default";

	/** Configuration name for the value of {@link #getNonNullableObjectTypes()}. */
	String NON_NULLABLE_OBJECT_TYPES = "non-nullable-object-types";

	/** Configuration name for the value of {@link #getNullableObjectTypes()}. */
	String NULLABLE_OBJECT_TYPES = "nullable-object-types";

	/** Configuration name for the value of {@link #getNullableObjectTypesWithNullDefault()}. */
	String NULLABLE_OBJECT_TYPES_WITH_NULL_DEFAULT = "nullable-object-types-with-null-default";

	/** Configuration name for the value of {@link #getNullableObjectTypesWithNonNullDefault()}. */
	String NULLABLE_OBJECT_TYPES_WITH_NON_NULL_DEFAULT = "nullable-object-types-with-non-null-default";

	/** Configuration name for the value of {@link #getObjectTypesMandatory()}. */
	String OBJECT_TYPES_MANDATORY = "object-types-mandatory";

	/** Configuration name for the value of {@link #getObjectTypesReadOnly()}. */
	String OBJECT_TYPES_READ_ONLY = "object-types-read-only";

	/** Configuration name for the value of {@link #getObjectTypesOptions()}. */
	String OBJECT_TYPES_OPTIONS = "object-types-options";

	/** Configuration name for the value of {@link #getObjectTypesWholeLine()}. */
	String OBJECT_TYPES_WHOLE_LINE = "object-types-whole-line";

	/** Configuration name for the value of {@link #getSpecialEditors()}. */
	String SPECIAL_EDITORS = "special-editors";

	/** Demo of properties with primitive types. */
	@NonNullable
	@ItemDefault
	@Name(PRIMITIVE_TYPES)
	PrimitiveTypes getPrimitiveTypes();

	/** Demo of properties with value constraints. */
	@NonNullable
	@ItemDefault
	@Name(CONSTRAINTS)
	@DisplayMinimized
	DemoDeclarativeConstraints getConstraints();

	/** Demo of properties with the wrapper types of primitive values. */
	@NonNullable
	@ItemDefault
	@Name(PRIMITIVE_OBJECT_TYPES)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	PrimitiveObjectTypes getPrimitiveObjectTypes();

	/**
	 * Demo of properties with the wrapper types of primitive values having a non-null default
	 * value.
	 */
	@NonNullable
	@ItemDefault
	@Name(PRIMITIVE_OBJECT_TYPES_NON_NULL_DEFAULT)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	PrimitiveObjectTypesNonNullDefault getPrimitiveObjectTypesNonNullDefault();

	/** Demo of object-valued properties that must not be empty. */
	@NonNullable
	@ItemDefault
	@Name(NON_NULLABLE_OBJECT_TYPES)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NonNullableObjectTypes getNonNullableObjectTypes();

	/** Demo of object-valued properties that may be empty. */
	@NonNullable
	@ItemDefault
	@Name(NULLABLE_OBJECT_TYPES)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NullableObjectTypes getNullableObjectTypes();

	/** Demo of object-valued properties that may be empty and are empty by default. */
	@NonNullable
	@ItemDefault
	@Name(NULLABLE_OBJECT_TYPES_WITH_NULL_DEFAULT)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NullableObjectTypesWithNullDefault getNullableObjectTypesWithNullDefault();

	/** Demo of object-valued properties that may be empty but have a default value. */
	@NonNullable
	@ItemDefault
	@Name(NULLABLE_OBJECT_TYPES_WITH_NON_NULL_DEFAULT)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	NullableObjectTypesWithNonNullDefault getNullableObjectTypesWithNonNullDefault();

	/** Demo of mandatory object-valued properties. */
	@NonNullable
	@ItemDefault
	@Name(OBJECT_TYPES_MANDATORY)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ObjectTypesMandatory getObjectTypesMandatory();

	/** Demo of read-only object-valued properties. */
	@NonNullable
	@ItemDefault
	@Name(OBJECT_TYPES_READ_ONLY)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ObjectTypesReadOnly getObjectTypesReadOnly();

	/** Demo of object-valued properties with a fixed set of options. */
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

	/**
	 * {@link TypeDemos} part to demonstrate special editors.
	 */
	@NonNullable
	@ItemDefault
	@Name(SPECIAL_EDITORS)
	@Label("Properties with special GUI representations")
	SpecialEditors getSpecialEditors();

}
