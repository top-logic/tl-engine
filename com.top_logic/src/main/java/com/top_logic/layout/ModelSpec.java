/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.False;
import com.top_logic.layout.channel.linking.Provider;
import com.top_logic.layout.channel.linking.True;
import com.top_logic.layout.channel.linking.Union;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.channel.linking.ref.NamedComponent;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.component.model.StaticMethodModelProvider;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Specification of a model linkage between components (where to take a model object from).
 * 
 * @see com.top_logic.mig.html.layout.LayoutComponent.Config#getModelSpec()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
@com.top_logic.basic.config.annotation.Format(ModelSpec.Format.class)
@Label("component channel")
@Options(fun = AllInAppImplementations.class)
public interface ModelSpec extends PolymorphicConfiguration<ChannelLinking>, ConfigPart {

	/**
	 * {@link ConfigurationValueProvider} defining a short-cut textual representation for
	 * {@link ModelSpec} configurations.
	 * 
	 * <p>
	 * The understood format is the following:
	 * </p>
	 * 
	 * <dl>
	 * <dt>model(componentName)</dt>
	 * <dd>The value is taken from the <code>model</code> channel of the component with name
	 * <code>componentName</code>. The same format can be used with all component channels (see
	 * {@link LayoutComponent#getChannel(String)}). E.g. to reference the selection of a named
	 * component, use <code>selection(componentName)</code>.</dd>
	 * 
	 * <dt>model(dialogParent())</dt>
	 * <dd>The value is taken form the <code>model</code> channel of the component that opened the
	 * surrounding dialog. This allows referencing the opener from within a dialog without
	 * explicitly knowing its name. The same format can be used with all component channels (see
	 * {@link LayoutComponent#getChannel(String)}). E.g. to reference the selection of the component
	 * that opened the surrounding dialog, use <code>selection(dialogParent())</code>.</dd>
	 * 
	 * <dt>model(self())</dt>
	 * <dd>The value is taken form the <code>model</code> channel of the current component. This is
	 * especially useful for referencing the component that defines a command, from within a
	 * command's {@link com.top_logic.tool.boundsec.CommandHandler.Config#getTarget() target} model
	 * specification. The same format can be used with all component channels (see
	 * {@link LayoutComponent#getChannel(String)}). E.g. to reference the selection of the current
	 * component, use <code>selection(self())</code>.</dd>
	 * 
	 * <dt>null()</dt>
	 * <dd>Use the value <code>null</code> as model.</dd>
	 * 
	 * <dt>true</dt>
	 * <dd>Use the value {@link Boolean#TRUE} as model.</dd>
	 * 
	 * <dt>false</dt>
	 * <dd>Use the value {@link Boolean#FALSE} as model.</dd>
	 * 
	 * <dt>provider(my.package.MyModelProvider)</dt>
	 * <dd>Call the method {@link ModelProvider#getBusinessModel(LayoutComponent)} of the
	 * {@link ModelProvider} with the given class name and pass the current component as argument.
	 * The resulting value is used as model.</dd>
	 * 
	 * <dt>provider(my.package.MyClass#myStaticProviderMethod)</dt>
	 * <dd>Call the static no-argument method with the given name on the given class and use the
	 * returned value as model.</dd>
	 * </dl>
	 */
	public class Format extends AbstractConfigurationValueProvider<ModelSpec> {
	
		private static final int CLASS_NAME_GROUP = 1;

		private static final int METHOD_NAME_GROUP = 2;
	
		private static final int NULL_GROUP = 3;

		private static final int TRUE_GROUP = 4;

		private static final int FALSE_GROUP = 5;
	
		private static final int CHANNEL_NAME_GROUP = 6;
	
		private static final int RELATION_NAME_GROUP = 7;
	
		private static final int COMPONENT_NAME_GROUP = 8;
	
		private static final int COMMA_GROUP = 9;
	
		/** {@link Pattern} that describes a legal value for a {@link ModelSpec}. */
		public static final Pattern MODEL_PATTERN = modelPattern();
	
		/**
		 * Singleton {@link ModelSpec.Format} instance.
		 */
		public static final Format INSTANCE = new Format();

		private Format() {
			super(ModelSpec.class);
		}

		@Override
		protected ModelSpec getValueEmpty(String propertyName) throws ConfigurationException {
			return defaultValue();
		}

		@Override
		public boolean isLegalValue(Object value) {
			return value == null || (value instanceof ModelSpec && canSerialize((ModelSpec) value));
		}
	
		private boolean canSerialize(ModelSpec value) {
			ChannelLinking linking;
			try {
				linking = TypedConfigUtil.createInstance(value);
			} catch (RuntimeException ex) {
				/* It is possible that the model specification contains invalid references, e.g.
				 * references to not longer existing model elements. In this case instantiation of
				 * the ChannelLinking fails. */
				return false;
			}
			return linking.hasCompactRepresentation();
		}

		@Override
		protected ModelSpec getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			ModelSpec spec = null;
			
			Matcher matcher = MODEL_PATTERN.matcher(propertyValue);
			while (matcher.lookingAt()) {
				String providerClassName = matcher.group(CLASS_NAME_GROUP);
				String channelKind = matcher.group(CHANNEL_NAME_GROUP);
				if (providerClassName != null) {
					Provider provider = newConfigItem(Provider.class);

					String methodName = matcher.group(METHOD_NAME_GROUP);
					if (methodName == null) {
						// provider(class)
						Class<? extends ModelProvider> implClass =
							ConfigUtil.getClassForName(ModelProvider.class, "providerClass", providerClassName, null);

						Factory factory = DefaultConfigConstructorScheme.getFactory(implClass);
						Class<? extends PolymorphicConfiguration<ModelProvider>> configurationInterface =
							(Class<? extends PolymorphicConfiguration<ModelProvider>>) factory
								.getConfigurationInterface();
						PolymorphicConfiguration<ModelProvider> providerConfig =
							TypedConfiguration.newConfigItem(configurationInterface);
						providerConfig.setImplementationClass(implClass);
						provider.setImpl(providerConfig);
					} else {
						// provider(class#method())

						Class<?> implClass =
							ConfigUtil.getClassForName(Object.class, "providerClass", providerClassName, null);

						StaticMethodModelProvider.Config providerConfig =
							TypedConfiguration.newConfigItem(StaticMethodModelProvider.Config.class);
						providerConfig.setType(implClass);
						providerConfig.setMethod(methodName);
						provider.setImpl(providerConfig);
					}
					spec = join(spec, provider);
				}
				else if (matcher.group(NULL_GROUP) != null) {
					// Null is ignored in combination with other specifications. Only if it is the
					// only specification, it is returned as defaultValue(), see below.
				}
				else if (matcher.group(TRUE_GROUP) != null) {
					spec = join(spec, newConfigItem(True.class));
				} else if (matcher.group(FALSE_GROUP) != null) {
					spec = join(spec, newConfigItem(False.class));
				}
				else if (channelKind != null) {
					// channel(componentRef)
					
					String relationName = matcher.group(RELATION_NAME_GROUP);
					if (relationName != null) {
						ComponentRelation componentRelation = newConfigItem(ComponentRelation.class);
						ComponentRelation.Kind relationKind;
						try {
							relationKind = ComponentRelation.Kind.valueOf(relationName);
						} catch (IllegalArgumentException ex) {
							throw new ConfigurationException("Invalid component relation in property '" + propertyName
								+ "': " + relationName, ex);
						}
						componentRelation.setKind(relationKind);
	
						spec = joinComponentRef(spec, channelKind, componentRelation);
					} else {
						String componentNames = matcher.group(COMPONENT_NAME_GROUP);
						if (componentNames != null) {
							for (String componentName : componentNames.trim().split("\\s*,\\s*")) {
								NamedComponent namedComponent = newConfigItem(NamedComponent.class);
								namedComponent.setName(ComponentName.newConfiguredName(propertyName, componentName));

								spec = joinComponentRef(spec, channelKind, namedComponent);
							}
						} else {
							// Empty component ref: channel(). For compatibility and easy migration,
							// this format is accepted and interpreted as null(). When migrating
							// definitions like master="${master}", where the value is an alias,
							// this alias variable may be empty meaning no master. By syntactically
							// replacing with model="selection(${master})", the variable ${master}
							// might evaluate to the empty string leading to selection(). Ignore.
						}
					}
				}
	
				boolean hasComma = matcher.group(COMMA_GROUP) == null;
	
				matcher.region(matcher.end(), propertyValue.length());
				if (hasComma) {
					break;
				}
			}
	
			if (matcher.regionStart() < propertyValue.length()) {
				throw new ConfigurationException("Model spec not understood: "
					+ propertyValue.subSequence(matcher.regionStart(), propertyValue.length()));
			}
	
			return spec == null ? defaultValue() : spec;
		}

		private ModelSpec joinComponentRef(ModelSpec spec, String channelName, ComponentRef componentRef) {
			if (componentRef != null) {
				Channel channel = newConfigItem(Channel.class);
				channel.setName(channelName);
				channel.setComponentRef(componentRef);

				spec = join(spec, channel);
			}
			return spec;
		}
	
		private ModelSpec join(ModelSpec spec1, ModelSpec spec2) {
			if (spec1 == null) {
				return spec2;
			}
			if (spec1 instanceof Union) {
				((Union) spec1).getSpecs().add(spec2);
			}
			Union union = newConfigItem(Union.class);
			union.getSpecs().add(spec1);
			union.getSpecs().add(spec2);
			return union;
		}
	
		@Override
		protected String getSpecificationNonNull(ModelSpec configValue) {
			StringBuilder result = new StringBuilder();
			TypedConfigUtil.createInstance(configValue).appendTo(result);
			return result.toString();
		}
	

		private static Pattern modelPattern() {
			String S = "\\s*";
			String name = "[a-zA-Z][a-zA-Z0-9]*";
			String relationName = name;
			String componentName = "[^\\s\\(\\)]+";
			String className = "[a-zA-Z\\$_][a-zA-Z\\$_0-9\\.]*";
			String methodName = "[a-zA-Z\\$_][a-zA-Z\\$_0-9]*";
			String relationSpec = group(RELATION_NAME_GROUP, relationName) + "\\(" + S + "\\)";
			String nameSpec = group(COMPONENT_NAME_GROUP, componentName);
			String refSpec = "(?:" + relationSpec + "|" + nameSpec + "|" + /* empty */")";
			String channelName = name;
			String relation = group(CHANNEL_NAME_GROUP, channelName) + S + "\\(" + S + refSpec + S + "\\)";
			String nullSpec = group(NULL_GROUP, "null") + S + "\\(" + S + "\\)";
			String trueSpec = group(TRUE_GROUP, "true");
			String falseSpec = group(FALSE_GROUP, "false");
			String provider = "provider" + S + "\\(" + S
				+ group(CLASS_NAME_GROUP, className)
				+ "(?:" + "#" + group(METHOD_NAME_GROUP, methodName) + "(?:" + "\\(\\)" + ")?" + ")?"
				+ S + "\\)";
			return Pattern.compile(S + "(?:" + provider + "|" + nullSpec + "|" + trueSpec + "|" + falseSpec + "|"
				+ relation + ")" + S + group(COMMA_GROUP, "," + S)
				+ "?");
		}
	
		/**
		 * @param id
		 *        For documentation only.
		 */
		private static String group(int id, String pattern) {
			return "(" + pattern + ")";
		}
	
	}

}
