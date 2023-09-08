/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.AbstractJsonListBinding;
import com.top_logic.basic.config.json.JsonConfigurationReader;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.io.character.StringContent;
import com.top_logic.basic.json.JSON;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link AbstractJsonListBinding} reading and writing {@link List}s of {@link IParameterObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ParameterObjectBinding extends AbstractJsonListBinding<IParameterObject> {

	@Override
	public List<IParameterObject> loadConfigItem(PropertyDescriptor property, JsonReader in, List<IParameterObject> baseValue)
			throws IOException, ConfigurationException {
		List<IParameterObject> result = CollectionUtil.nonNull(baseValue);
		in.beginArray();
		if (in.hasNext()) {
			result = new ArrayList<>(result);
			do {
				Object value = JsonUtilities.readValue(in);
				if (!(value instanceof Map)) {
					throw new ConfigurationException(
						I18NConstants.PARAMETER_IS_NOT_AN_OBJECT__PARAMETER.fill(value),
						property.getPropertyName(),
						JSON.toString(value));
				}
				IParameterObject readParamObject;
				Object reference = ((Map<?, ?>) value).get(ReferencingObject.$REF);
				if (reference != null) {
					if (!(reference instanceof String)) {
						throw new ConfigurationException(
							I18NConstants.REFERENCE_PARAMETER_IS_NOT_A_STRING__REFERENCE.fill(reference),
							property.getPropertyName(),
							JSON.toString(value));
					}
					ReferencingParameterObject paramReference = TypedConfiguration.newConfigItem(ReferencingParameterObject.class);
					paramReference.setReference((String) reference);
					readParamObject = paramReference;
				} else {
					JsonConfigurationReader innerReader = new JsonConfigurationReader(
						SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, paramObjectDescr());
					innerReader.treatUnexpectedEntriesAsWarn(true);
					innerReader.setSource(new StringContent(JSON.toString(value)));
					ParameterObject param = (ParameterObject) innerReader.read();
					readParamObject = param;
				}
				result.add(readParamObject);
			} while (in.hasNext());
		}
		in.endArray();
		return result;
	}

	private ConfigurationDescriptor paramObjectDescr() {
		return TypedConfiguration.getConfigurationDescriptor(ParameterObject.class);
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, List<IParameterObject> items) throws IOException {
		out.beginArray();
		for (IParameterObject item : items) {
			if (item instanceof ReferencingParameterObject) {
				out.beginObject();
				out.name(ReferencingObject.$REF);
				out.value(((ReferencingParameterObject) item).getReference());
				out.endObject();
			} else {
				new JsonConfigurationWriter(out).prettyPrint().write(paramObjectDescr(), item);
			}
		}
		out.endArray();
	}

}

