/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.ModelSpec;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.securityObjectProvider.ConfiguredModelSecurityProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.ModelSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityObjectProviderFormat;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;

/**
 * Configuration of a {@link SecurityObjectProvider}.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
@Abstract
public interface SecurityObjectProviderConfig extends ConfigurationItem {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** @see #getSecurityObject() */
	public static final String SECURITY_OBJECT = "securityObject";

	/**
	 * The {@link SecurityObjectProvider algorithm} that navigates to the {@link Object} on which
	 * the security is checked.
	 */
	@Name(SECURITY_OBJECT)
	@FormattedDefault(CompactSecurityObjectProviderFormat.SECURITY_ROOT)
	@Format(CompactSecurityObjectProviderFormat.class)
	PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();

	/**
	 * Resolves {@link #getSecurityObject()} within the given context.
	 */
	default SecurityObjectProvider resolveSecurityObject(InstantiationContext context) {
		return SecurityObjectProvider.fromConfiguration(context, getSecurityObject());
	}

	/**
	 * Format to serialize an In App configured {@link SecurityObjectProvider} in compact form.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class CompactSecurityObjectProviderFormat
			extends AbstractConfigurationValueProvider<PolymorphicConfiguration<? extends SecurityObjectProvider>> {

		static final String SECURITY_ROOT = "securityRoot";

		static final String MODEL = "model";

		/**
		 * Regular expression source matching a single qualified type reference
		 * (<code>module:Type</code>).
		 *
		 * <p>
		 * Used to recognize the compact serialization <code>model(module:Type, ...)</code> of a
		 * {@link ModelSecurityObjectProvider} that restricts the model to certain
		 * {@link com.top_logic.tool.boundsec.securityObjectProvider.ModelSecurityObjectProvider.Config#getModelTypes()
		 * types}. The mandatory {@link TLModelUtil#QUALIFIED_NAME_SEPARATOR colon} distinguishes a
		 * type reference from a {@link com.top_logic.mig.html.layout.ComponentName component name},
		 * which never contains a colon. A bare <code>model(component)</code> therefore keeps its
		 * meaning of "the model channel of the given component".
		 * </p>
		 */
		static final String MODEL_TYPE_SRC = TLModelUtil.MODULE_NAME_PATTERN_SRC
			+ TLModelUtil.QUALIFIED_NAME_SEPARATOR + TLModelUtil.QNAME_PATTERN_SRC;

		/**
		 * Regular expression source matching a non-empty comma-separated list of
		 * {@link #MODEL_TYPE_SRC type references}.
		 */
		static final String MODEL_TYPES_SRC = MODEL_TYPE_SRC + "(?:\\s*,\\s*" + MODEL_TYPE_SRC + ")*";

		/** Format for the comma-separated type list in the <code>model(...)</code> serialization. */
		private static final TLModelPartRef.CommaSeparatedTLModelPartRefs MODEL_TYPES_FORMAT =
			new TLModelPartRef.CommaSeparatedTLModelPartRefs();

		private static final String REFERENCE_PREFIX = "ref:";

		/** {@link Pattern} for allowed values. */
		public static final Pattern PATTERN = pattern();

		/**
		 * Creates a new {@link CompactSecurityObjectProviderFormat}.
		 */
		public CompactSecurityObjectProviderFormat() {
			super(PolymorphicConfiguration.class);
		}

		private static Pattern pattern() {
			return Pattern
				.compile("(?:"
					+ MODEL + "\\(" + group(MODEL_TYPES_SRC) + "\\)" + "|"
					+ group(MODEL) + "|"
					+ group(SECURITY_ROOT) + "|"
					+ group(SecurityObjectProviderManager.PATH_SECURITY_OBJECT_PROVIDER + ".*") + "|"
					+ REFERENCE_PREFIX + group("[\\w_\\.]+") + "|"
					+ group(ModelSpec.Format.MODEL_PATTERN.pattern()) +
					")");
		}

		private static String group(String pattern) {
			return "(" + pattern + ")";
		}

		@Override
		protected PolymorphicConfiguration<? extends SecurityObjectProvider> getValueNonEmpty(String propertyName,
				CharSequence propertyValue) throws ConfigurationException {
			Matcher matcher = PATTERN.matcher(propertyValue);
			if (matcher.matches()) {
				if (matcher.group(1) != null) {
					ModelSecurityObjectProvider.Config result =
						TypedConfiguration.newConfigItem(ModelSecurityObjectProvider.Config.class);
					result.setModelTypes(MODEL_TYPES_FORMAT.getValue(propertyName, matcher.group(1)));
					return result;
				} else if (matcher.group(2) != null) {
					return TypedConfiguration.createConfigItemForImplementationClass(ModelSecurityObjectProvider.class);
				} else if (matcher.group(3) != null) {
					return TypedConfiguration.createConfigItemForImplementationClass(SecurityRootObjectProvider.class);
				} else if (matcher.group(4) != null) {
					return SecurityObjectProviderFormat.INSTANCE.getValue(propertyName, matcher.group(4));
				} else if (matcher.group(5) != null) {
					ReferencedSecurityObjectProvider.Config result =
						TypedConfiguration.newConfigItem(ReferencedSecurityObjectProvider.Config.class);
					result.setReference(matcher.group(5));
					return result;
				} else if (matcher.group(6) != null) {
					ConfiguredModelSecurityProvider.Config result =
						TypedConfiguration.newConfigItem(ConfiguredModelSecurityProvider.Config.class);
					ModelSpec modelSpec = ModelSpec.Format.INSTANCE
						.getValue(ConfiguredModelSecurityProvider.Config.MODEL, matcher.group(6));
					result.setModel(modelSpec);
					return result;
				} else {
					throw new UnreachableAssertion("At least one group is not null.");
				}
			} else {
				throw new ConfigurationException(I18NConstants.INVALID_SECURITY_OBJECT_PROVIDER_SERIALIZATION,
					propertyName, propertyValue);
			}
		}

		@Override
		protected String getSpecificationNonNull(
				PolymorphicConfiguration<? extends SecurityObjectProvider> configValue) {
			if (configValue instanceof ModelSecurityObjectProvider.Config modelProvider) {
				List<TLModelPartRef> modelTypes = modelProvider.getModelTypes();
				if (!modelTypes.isEmpty()) {
					return MODEL + "(" + MODEL_TYPES_FORMAT.getSpecification(modelTypes) + ")";
				}
				return MODEL;
			}
			if (isSecurityRootObjectProvider(configValue)) {
				return SECURITY_ROOT;
			}
			if (configValue instanceof PathSecurityObjectProvider.Config) {
				return SecurityObjectProviderFormat.INSTANCE.getSpecification(configValue);
			}
			if (configValue instanceof ReferencedSecurityObjectProvider.Config ref) {
				return REFERENCE_PREFIX + ref.getReference();
			}
			if (configValue instanceof ConfiguredModelSecurityProvider.Config modelRef) {
				return ModelSpec.Format.INSTANCE.getSpecification(modelRef.getModel());
			}
			throw new IllegalArgumentException();
		}

		@Override
		public boolean isLegalValue(Object value) {
			if (!super.isLegalValue(value)) {
				return false;
			}
			if (value == null) {
				return true;
			}
			return (value instanceof ConfiguredModelSecurityProvider.Config
				&& ModelSpec.Format.INSTANCE.isLegalValue(((ConfiguredModelSecurityProvider.Config) value).getModel()))
				|| value instanceof PathSecurityObjectProvider.Config
				|| value instanceof ReferencedSecurityObjectProvider.Config
				|| isSecurityRootObjectProvider((PolymorphicConfiguration<?>) value)
				|| value instanceof ModelSecurityObjectProvider.Config;
		}

		private boolean isSecurityRootObjectProvider(PolymorphicConfiguration<?> value) {
			return SecurityRootObjectProvider.class.isAssignableFrom(value.getImplementationClass());
		}

	}
}
