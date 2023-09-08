/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service;

import java.util.function.Function;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.model.annotate.BooleanAnnotation;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Common super-type for {@link TLExported} and {@link TLImported}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@Abstract
@DefaultStrategy(Strategy.NONE)
public interface TLSynced extends TLAttributeAnnotation, TLTypeAnnotation, BooleanAnnotation {

	/** Property name of {@link #getValueMapping()}. */
	String VALUE_MAPPING = "value-mapping";

	/**
	 * A {@link Function} that converts the value of this attribute into another value that should
	 * be transmitted via TL-Sync instead.
	 * <p>
	 * <em>Export: Primitive values must not be mapped to reference values.</em> But it is okay to
	 * map reference values to primitive values.
	 * </p>
	 * <p>
	 * <em>Import: Reference values must not be mapped to primitive values.</em> But it is okay to
	 * map primitive values to reference values.
	 * </p>
	 * <p>
	 * Null is allowed both as input and as output of this function.
	 * </p>
	 */
	PolymorphicConfiguration<? extends Function<Object, ?>> getValueMapping();

}
