/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.json.JsonSchemaProvider;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.json.schema.model.StringSchema;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.format.ObjectType;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.TLContext;

/**
 * Format of a {@link TLObject}-valued configuration property whose value an agent supplies.
 *
 * <p>
 * The value is the identifier tools exchange with agents ({@link ReadFilter#idOf(TLObject)}),
 * resolved by {@link TidResolver} with the read rights of the current user: an object the user may
 * not read is refused exactly like one that does not exist. The refusal carries the
 * {@link TidResolver} diagnosis, which tells the model what is wrong with the identifier.
 * </p>
 *
 * <p>
 * The JSON schema of such a property is a string whose description names the form of the identifier
 * and, if the property is annotated with {@link ObjectType}, the expected model type. The type itself
 * is checked by the {@link ObjectType} constraint.
 * </p>
 */
public class AgentObjectFormat extends AbstractConfigurationValueProvider<TLObject> implements JsonSchemaProvider {

	/**
	 * Description of the identifier form in the JSON schema.
	 */
	public static final String ID_DESCRIPTION = "The id of an object in the form 'TypeName:id', as tools report it"
		+ " (also accepted: an entry 'label [id:TypeName:id]').";

	/**
	 * Description of the type restriction in the JSON schema, followed by the qualified type name.
	 */
	public static final String TYPE_DESCRIPTION = " The object must be of the model type ";

	/**
	 * The {@link AgentObjectFormat} instance.
	 */
	public static final AgentObjectFormat INSTANCE = new AgentObjectFormat();

	/**
	 * Creates an {@link AgentObjectFormat}.
	 *
	 * <p>
	 * Use {@link #INSTANCE}.
	 * </p>
	 */
	protected AgentObjectFormat() {
		super(TLObject.class);
	}

	@Override
	protected TLObject getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		try {
			return TidResolver.resolve(propertyValue.toString(), security());
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationException(ResKey.text(ex.getMessage()), propertyName, propertyValue, ex);
		}
	}

	/**
	 * The rights an identifier is resolved with.
	 *
	 * @return The read rights of the current user.
	 */
	protected ModelSecurity security() {
		return new ModelAccessRightsSecurity(TLContext.currentUser());
	}

	@Override
	protected String getSpecificationNonNull(TLObject configValue) {
		return ReadFilter.idOf(configValue);
	}

	@Override
	public Schema buildSchema(PropertyDescriptor property) {
		StringBuilder description = new StringBuilder(ID_DESCRIPTION);
		ObjectType objectType = property.getAnnotation(ObjectType.class);
		if (objectType != null) {
			description.append(TYPE_DESCRIPTION).append('\'').append(objectType.value())
				.append("' or one of its specializations.");
		}
		return StringSchema.create().setDescription(description.toString());
	}

}
