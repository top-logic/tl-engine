/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Dynamically computing the icon for an object that is an instance of the annotated type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@TagName("dynamic-icon")
@TargetType(value = { TLTypeKind.REF, TLTypeKind.COMPOSITION })
public interface TLDynamicIcon extends TLTypeAnnotation {

	/**
	 * The provider computing the dynamic icon for an instance of the annotated type.
	 */
	@Mandatory
	@DefaultContainer
	@Options(fun = AllInAppImplementations.class)
	PolymorphicConfiguration<? extends IconProvider> getIconProvider();

	/**
	 * @see #getIconProvider()
	 */
	void setIconProvider(PolymorphicConfiguration<? extends IconProvider> value);

}
