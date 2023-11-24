/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * Configuration of security mechanism that can be used.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface WithSecurity extends ConfigurationItem {

	/** Configuration name for the value of {@link #getSecurity()}. */
	String SECURITY = "security";

	/**
	 * A declaration of which security mechanisms can be used across the API. The list of values
	 * includes alternative security requirement objects that can be used. Only one of the security
	 * requirement objects need to be satisfied to authorize a request.
	 */
	@ListBinding(format = SecurityConfigFormat.class)
	@JsonBinding(SecurityJsonBinding.class)
	@Name(SECURITY)
	List<Map<String, List<String>>> getSecurity();

	/**
	 * Setter for {@link #getSecurity()}.
	 */
	void setSecurity(List<Map<String, List<String>>> value);

	/**
	 * {@link AbstractConfigurationValueProvider} formatting a security configuration in JSON
	 * format.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class SecurityConfigFormat extends AbstractConfigurationValueProvider<Map<String, List<String>>> {

		/**
		 * Creates a new {@link SecurityConfigFormat}.
		 */
		public SecurityConfigFormat() {
			super(Map.class);
		}

		@Override
		protected Map<String, List<String>> getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			try (JsonReader r = new JsonReader(new StringR(propertyValue.toString()))) {
				return SecurityJsonBinding.readSecurityConfig(r);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		}

		@Override
		protected String getSpecificationNonNull(Map<String, List<String>> configValue) {
			StringBuilder out = new StringBuilder();
			try (JsonWriter w = new JsonWriter(out)) {
				SecurityJsonBinding.saveSecurityConfig(w, configValue);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
			return out.toString();
		}

	}


	/**
	 * Serialising algorithm of the value {@link WithSecurity#getSecurity()}.
	 * 
	 * @see "https://spec.openapis.org/oas/v3.0.3.html#security-requirement-object"
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class SecurityJsonBinding implements JsonValueBinding<List<Map<String, List<String>>>> {

		@Override
		public boolean isLegalValue(Object value) {
			return true;
		}

		@Override
		public List<Map<String, List<String>>> defaultValue() {
			return Collections.emptyList();
		}

		@Override
		public Object normalize(Object value) {
			if (value == null) {
				return Collections.emptyList();
			}
			return value;
		}

		@Override
		public List<Map<String, List<String>>> loadConfigItem(PropertyDescriptor property, JsonReader in, List<Map<String, List<String>>> baseValue)
				throws IOException, ConfigurationException {
			List<Map<String, List<String>>> result = baseValue != null ? new ArrayList<>(baseValue) : new ArrayList<>();
			in.beginArray();
			while (in.hasNext()) {
				result.add(readSecurityConfig(in));
			}
			in.endArray();
			return result;
		}

		static Map<String, List<String>> readSecurityConfig(JsonReader in) throws IOException {
			Map<String, List<String>> securityConfig;
			in.beginObject();
			if (in.hasNext()) {
				securityConfig = new HashMap<>();
				do {
					String securityName = in.nextName();
					securityConfig.put(securityName, readScopes(in));
				} while (in.hasNext());
			} else {
				securityConfig = Collections.emptyMap();
			}
			in.endObject();
			return securityConfig;
		}

		private static List<String> readScopes(JsonReader in) throws IOException {
			List<String> scopes;
			in.beginArray();
			if (in.hasNext()) {
				scopes = new ArrayList<>();
				do {
					scopes.add(in.nextString());
				} while (in.hasNext());
			} else {
				scopes = Collections.emptyList();
			}
			in.endArray();
			return scopes;
		}

		@Override
		public void saveConfigItem(PropertyDescriptor property, JsonWriter out, List<Map<String, List<String>>> item) throws IOException {
			out.beginArray();
			for (Map<String, List<String>> securityConfig : item) {
				saveSecurityConfig(out, securityConfig);
			}
			out.endArray();
		}

		static void saveSecurityConfig(JsonWriter out, Map<String, List<String>> securityConfig) throws IOException {
			out.beginObject();
			for (Entry<String, List<String>> scopesByScheme : securityConfig.entrySet()) {
				out.name(scopesByScheme.getKey());
				saveScopes(out, scopesByScheme.getValue());
			}
			out.endObject();
		}

		private static void saveScopes(JsonWriter out, List<String> scopes) throws IOException {
			out.beginArray();
			for (String scope : scopes) {
				out.value(scope);
			}
			out.endArray();
		}

	}

}

