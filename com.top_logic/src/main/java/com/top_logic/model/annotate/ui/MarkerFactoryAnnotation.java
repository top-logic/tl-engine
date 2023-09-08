/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.control.CalendarMarker;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAnnotation} that defines the {@link MarkerFactory} to set the {@link CalendarMarker}.
 *
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
@TagName("marker-factory")
@TargetType(TLTypeKind.DATE)
public interface MarkerFactoryAnnotation extends TLAttributeAnnotation {

	/**
	 * Configuration name for the {@link #getImplementation()} property.
	 */
	String IMPLEMENTATION_PROPERTY = "implementation";

	/**
	 * The getter to retrieve the {@link MarkerFactory} implementation.
	 * 
	 * @see MarkerFactory
	 */
	@Name(IMPLEMENTATION_PROPERTY)
	PolymorphicConfiguration<? extends MarkerFactory> getImplementation();

	/**
	 * @see #getImplementation()
	 */
	void setImplementation(PolymorphicConfiguration<? extends MarkerFactory> markerFactory);

}