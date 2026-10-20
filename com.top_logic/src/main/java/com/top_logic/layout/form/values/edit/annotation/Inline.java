/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TagName;

/**
 * States that the nested configuration of an item-valued property is displayed without a group of
 * its own.
 *
 * <p>
 * The properties of the nested configuration appear directly in the form that displays the
 * annotated property, as if they were properties of the surrounding configuration. The form shows
 * no heading, no border and no actions for the nested configuration: The item is displayed as the
 * property holds it. For a {@link PolymorphicConfiguration} item, the form offers no selection of
 * its configuration type, and it offers no button for removing the item. The annotation is
 * therefore meant for an item that is always present, for example a mandatory one or one with a
 * default value, and whose type is decided by the context the form is opened in.
 * </p>
 *
 * <p>
 * The annotation applies to {@link PropertyKind#ITEM} properties whose value is edited as a nested
 * form. It has no effect on a property of another kind (e.g. a list of items, whose entries keep
 * their groups), and on an item property that is displayed as a single value (see
 * {@link ItemDisplay}).
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("inline")
@ControlAnnotation
public @interface Inline {

	// Pure marker.

}
