/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.value;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.editor.TokenEditor;
import com.top_logic.service.openapi.client.registry.impl.call.Call;
import com.top_logic.service.openapi.client.registry.impl.call.MethodSpec;

/**
 * Creates a {@link ValueProducer} delivering a secret, e.g. an API key or token that the called
 * service expects as parameter.
 *
 * <p>
 * The secret is read from its {@link SecretSource} each time the service is called, so that a
 * rotated secret is used without restarting the application.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecretValue extends AbstractConfiguredInstance<SecretValue.Config<?>>
		implements ValueProducerFactory {

	/**
	 * Configuration options for {@link SecretValue}.
	 */
	@TagName("secret-value")
	public interface Config<I extends SecretValue> extends ValueProducerFactory.Config<I> {

		/** Configuration name for {@link #getSecret()}. */
		String SECRET = "secret";

		/**
		 * The secret to send.
		 *
		 * <p>
		 * An element without <code>class</code> attribute names a secret in the secret storage of
		 * the installation, so the configuration holds only its name:
		 * </p>
		 *
		 * <pre>
		 * &lt;value class="com.top_logic.service.openapi.client.registry.impl.value.SecretValue"&gt;
		 *   &lt;secret name="my_api_key"/&gt;
		 * &lt;/value&gt;
		 * </pre>
		 *
		 * <p>
		 * An element with <code>class</code> attribute selects another source, e.g. a file. An
		 * attribute value is an encrypted secret (or a value with the prefix
		 * <code>unencrypted:</code>).
		 * </p>
		 */
		@Name(SECRET)
		@Mandatory
		@Format(SecretSourceFormat.class)
		@PropertyEditor(TokenEditor.class)
		PolymorphicConfiguration<? extends SecretSource> getSecret();

		/**
		 * Setter for {@link #getSecret()}.
		 */
		void setSecret(PolymorphicConfiguration<? extends SecretSource> value);

	}

	private final SecretSource _secret;

	/**
	 * Creates a {@link SecretValue} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SecretValue(InstantiationContext context, Config<?> config) {
		super(context, config);
		_secret = context.getInstance(config.getSecret());
	}

	@Override
	public ValueProducer build(MethodSpec method) {
		return new ValueProducer() {

			@Override
			public Object getValue(Call call) {
				return _secret.getSecret();
			}
		};
	}

}
