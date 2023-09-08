/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.converters;

import com.google.inject.Inject;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.Revision;

/**
 * {@link AbstractReferenceConversion} that expects that an additional attribute contains the type
 * of the referenced object. This {@link AbstractReferenceConversion} removes the type attribute and
 * uses it to build the desired {@link ObjectKey}.
 * 
 * @see MonomorphicReferenceConversion
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ByTypeAttributeConversion extends AbstractReferenceConversion {

	/**
	 * Configuration of a {@link ByTypeAttributeConversion}.
	 * 
	 * @since 5.8.0
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ReferenceConversion> {

		/**
		 * Name of the attribute holding the name of the desired type.
		 */
		@Mandatory
		String getTypeAttribute();
	}

	private final String _typeAttribute;

	private MORepository _types;

	/**
	 * Creates a new {@link ByTypeAttributeConversion} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ByTypeAttributeConversion}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ByTypeAttributeConversion(InstantiationContext context, Config config) throws ConfigurationException {
		_typeAttribute = config.getTypeAttribute();
	}

	/**
	 * Initialises the {@link MORepository} of this {@link ByTypeAttributeConversion}.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		_types = types;
	}

	@Override
	public ObjectKey convertReference(ItemChange event, String dumpValue) {
		try {
			Object typeName = event.getValues().remove(_typeAttribute);
			MetaObject type = _types.getMetaObject((String) typeName);
			TLID dumpId = StringID.valueOf(dumpValue);
			ObjectBranchId selfKey = event.getObjectId();
			return new DefaultObjectKey(selfKey.getBranchId(), Revision.CURRENT_REV, type, dumpId);
		} catch (UnknownTypeException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void handleNullReference(ItemChange event) {
		event.getValues().remove(_typeAttribute);
	}

}

