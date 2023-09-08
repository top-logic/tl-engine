/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.access.TLStorageMapping;
import com.top_logic.model.annotate.TLSize;

/**
 * Annotations for defining a model binding to a relational database schema.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RelationalBindingAnnotations {

	/**
	 * Factory method for {@link TLStorageMapping} annotations.
	 * 
	 * @param mapping
	 *        See {@link TLStorageMapping#getImplementation()}
	 * @return The new annotation instance.
	 */
	public static TLStorageMapping createTLStorageMapping(StorageMapping<?> mapping) {
		TLStorageMapping result = TypedConfiguration.newConfigItem(TLStorageMapping.class);
		result.update(result.descriptor().getProperty(TLStorageMapping.IMPLEMENTATION), mapping);
		return result;
	}

	/**
	 * Factory method for {@link TLSize} annotations.
	 * 
	 * @param lowerBound
	 *        See {@link TLSize#getLowerBound()}
	 * @param upperBound
	 *        See {@link TLSize#getUpperBound()}
	 * @return The new annotation instance.
	 */
	public static TLSize createTLSize(long lowerBound, long upperBound) {
		TLSize result = TypedConfiguration.newConfigItem(TLSize.class);
		ConfigurationDescriptor descriptor = result.descriptor();
		result.update(descriptor.getProperty(TLSize.LOWER_BOUND_PROPERTY), lowerBound);
		result.update(descriptor.getProperty(TLSize.UPPER_BOUND_PROPERTY), upperBound);
		return result;
	}

	/**
	 * Utility method to check, whether a {@link TLSize#getUpperBound()} is set.
	 */
	public static boolean hasUpperLimit(TLSize tlSize) {
		return tlSize.getUpperBound() < TLSize.NO_UPPER_BOUND;
	}

	/**
	 * Utility method to check, whether a {@link TLSize#getLowerBound()} is set.
	 */
	public static boolean hasLowerLimit(TLSize tlSize) {
		return tlSize.getLowerBound() > TLSize.NO_LOWER_BOUND;
	}

}
