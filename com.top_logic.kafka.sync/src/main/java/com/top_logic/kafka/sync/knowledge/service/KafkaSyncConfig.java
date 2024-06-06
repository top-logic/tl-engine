/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service;

import java.util.function.Function;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;

/**
 * Common configuration for export and import using the Kafka based TL-Sync feature.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface KafkaSyncConfig {

	/**
	 * The mapping that should be used to transform the values for the given attribute, before or
	 * after transmitting them.
	 * 
	 * @see TLSynced#getValueMapping()
	 * 
	 * @param ownerTypeId
	 *        The {@link TLObject#tId() id} of the {@link TLTypePart#getOwner() owner} of the
	 *        attribute, whose values should be mapped. Never null.
	 * @param tlAttribute
	 *        The {@link TLStructuredTypePart#getName() simple name} of the
	 *        {@link TLStructuredTypePart} whose values should be mapped. Never null.
	 * @return Null, if the value should not be mapped.
	 */
	Function<Object, ?> getValueMapping(ObjectKey ownerTypeId, String tlAttribute);

}
