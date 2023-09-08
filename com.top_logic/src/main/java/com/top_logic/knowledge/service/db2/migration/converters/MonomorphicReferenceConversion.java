/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link AbstractReferenceConversion} building an {@link ObjectKey} with a fixed
 * {@link ObjectKey#getObjectType() type}.
 * 
 * @see ByTypeAttributeConversion
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MonomorphicReferenceConversion extends AbstractReferenceConversion {

	/**
	 * Configuration of a {@link MonomorphicReferenceConversion}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ReferenceConversion> {

		/** Name of {@link #getTargetType()}. */
		String TARGET_TYPE = "target-type";

		/**
		 * Name of the {@link ObjectKey#getObjectType() type} of the created {@link ObjectKey}.
		 */
		@Name(TARGET_TYPE)
		String getTargetType();
	}

	private final Config _config;

	private MetaObject _targetType;

	/**
	 * Creates a {@link MonomorphicReferenceConversion} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MonomorphicReferenceConversion(InstantiationContext context, Config config) {
		_config = config;
	}

	/**
	 * Initialises this {@link MonomorphicReferenceConversion} with the {@link MORepository} to
	 * resolve {@link Config#getTargetType()}.
	 */
	@Inject
	public void initMORepository(MORepository typeRepository) {
		try {
			_targetType = typeRepository.getType(_config.getTargetType());
		} catch (UnknownTypeException ex) {
			throw new ConfigurationError("Unknown type " + _config.getTargetType(), ex);
		}
	}

	@Override
	public ObjectKey convertReference(ItemChange event, String dumpValue) {
		if (StringServices.isEmpty(dumpValue)) {
			return null;
		}
		TLID dumpId = StringID.valueOf(dumpValue);
		ObjectBranchId selfKey = event.getObjectId();
		return new DefaultObjectKey(selfKey.getBranchId(), Revision.CURRENT_REV, _targetType, dumpId);
	}

}
