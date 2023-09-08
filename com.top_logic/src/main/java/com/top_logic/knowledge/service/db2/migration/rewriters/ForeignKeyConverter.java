/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.service.db2.migration.converters.ReferenceConversion;

/**
 * Rewriter converting an {@link String} attribute value into an {@link ObjectKey} as value for a
 * reference attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ForeignKeyConverter extends ConfiguredItemChangeRewriter<ForeignKeyConverter.Config> {

	private final ReferenceConversion _conversion;

	/**
	 * Configuration of a {@link ForeignKeyConverter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ForeignKeyConverter>, AttributeConfig {

		/**
		 * Name of the reference attribute to put value to.
		 */
		@Name("reference-name")
		String getReferenceName();

		/**
		 * {@link ReferenceConversion} to apply to the referenced object.
		 */
		@Name("reference-conversion")
		PolymorphicConfiguration<ReferenceConversion> getReferenceConversion();

	}

	/**
	 * Creates a {@link ForeignKeyConverter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ForeignKeyConverter(InstantiationContext context, Config config) {
		super(context, config);
		_conversion = context.getInstance(config.getReferenceConversion());
	}

	@Override
	public void rewriteItemChange(ItemChange event) {
		Map<String, Object> values = event.getValues();
		String attribute = getConfig().getAttribute();
		if (!values.containsKey(attribute)) {
			return;
		}
		String dumpValue = (String) values.remove(attribute);

		ObjectKey ref;
		if (StringServices.isEmpty(dumpValue)) {
			_conversion.handleNullReference(event);
			ref = null;
		} else {
			ref = _conversion.convertReference(event, dumpValue);
		}
		values.put(getConfig().getReferenceName(), ref);
	}

}
