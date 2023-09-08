/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.knowledge.KIReferenceConfig;

/**
 * Special {@link ReferenceAttributeConfig} defining default values for the canonical references of
 * an association.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AssociationReference extends KIReferenceConfig {

	/**
	 * {@link EnumDefaultValue} constantly returning {@link HistoryType#CURRENT}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class CurrentDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return HistoryType.CURRENT;
		}
	}

	/**
	 * {@link EnumDefaultValue} constantly returning {@link DeletionPolicy#DELETE_REFERER}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class DeleteRefererDefault extends EnumDefaultValue {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return DeletionPolicy.DELETE_REFERER;
		}
	}

	@Override
	@BooleanDefault(true)
	boolean isMandatory();

	@Override
	boolean isBranchGlobal();

	@Override
	@ComplexDefault(CurrentDefault.class)
	HistoryType getHistoryType();

	@Override
	@BooleanDefault(false)
	boolean isMonomorphic();

	@Override
	@BooleanDefault(false)
	boolean isContainer();

	@Override
	@ComplexDefault(DeleteRefererDefault.class)
	DeletionPolicy getDeletionPolicy();
}
