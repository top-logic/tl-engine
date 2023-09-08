/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.misc;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.func.Function0;

/**
 * Container for the two {@link Function0} returning the multiplicity value "single value" and
 * "collection value". They are used in {@link Derived} annotations for constant "multiplicity"
 * properties.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class Multiplicity {

	/**
	 * {@link Function0} returning {@link Multiplicity#SINGLE_VALUE}.
	 */
	public static final class SingleValue extends Function0<Boolean> {

		/** The {@link Multiplicity.SingleValue} instance. */
		public static final Multiplicity.SingleValue INSTANCE = new Multiplicity.SingleValue();

		@Override
		public Boolean apply() {
			return SINGLE_VALUE;
		}

	}

	/**
	 * {@link Function0} returning {@link Multiplicity#COLLECTION_VALUE}.
	 */
	public static final class CollectionValue extends Function0<Boolean> {

		/** The {@link Multiplicity.CollectionValue} instance. */
		public static final Multiplicity.CollectionValue INSTANCE = new Multiplicity.CollectionValue();

		@Override
		public Boolean apply() {
			return COLLECTION_VALUE;
		}

	}

	/** The multiplicity value "single value". */
	public static final boolean SINGLE_VALUE = false;

	/** The multiplicity value "collection value". */
	public static final boolean COLLECTION_VALUE = true;

}
