/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.config.operations;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.model.search.expr.config.MethodResolver;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod.Builder;

/**
 * {@link MethodResolver} that offers methods in all extensions of {@link TLScriptFunctions} as
 * TL-Script functions.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLScriptMethodResolver extends AbstractConfiguredInstance<TLScriptMethodResolver.Config>
		implements MethodResolver {

	/**
	 * Typed configuration interface definition for {@link TLScriptMethodResolver}.
	 */
	public interface Config extends PolymorphicConfiguration<TLScriptMethodResolver> {

		// No configuration here.

	}

	private Map<String, Builder> _buildersByName;

	private Map<Object, Factory> _factories;

	/**
	 * Create a {@link TLScriptMethodResolver}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TLScriptMethodResolver(InstantiationContext context, Config config) {
		super(context, config);

		Collection<Class<?>> allTLScriptFunctions =
			TypeIndex.getInstance().getSpecializations(TLScriptFunctions.class, true, false, true);

		Map<String, TLScriptMethod.Builder> buildersByName = new HashMap<>();
		for (Class<?> methodsClass : allTLScriptFunctions) {
			for (Method m : methodsClass.getDeclaredMethods()) {
				if (m.isSynthetic()) {
					continue;
				}
				if (!Modifier.isStatic(m.getModifiers())) {
					continue;
				}
				if (!Modifier.isPublic(m.getModifiers())) {
					continue;
				}
				String methodName = m.getName();
				String qualifiedMethodName =
						methodsClass.getCanonicalName() + TLScriptMethod.Builder.METHOD_SEPARATOR + m.getName();
				
				Builder clash = buildersByName.get(methodName);
				if (clash != null) {
					context.error("Ambiguous method name '" + methodName + "': " + qualifiedMethodName + " vs. "
						+ asBuilderConfig(clash.getConfig()).getMethod());
					continue;
				}
				try {
					TLScriptMethod.Builder.Config scriptMethodConfig = asBuilderConfig(
						TypedConfiguration.createConfigItemForImplementationClass(TLScriptMethod.Builder.class));
					scriptMethodConfig.setName(methodName);
					scriptMethodConfig.setMethod(qualifiedMethodName);
					TLScriptMethod.Builder scriptMethod = context.getInstance(scriptMethodConfig);
					buildersByName.put(methodName, scriptMethod);
				} catch (ConfigurationException ex) {
					context.error("Unable to create config for " + qualifiedMethodName, ex);
					continue;
				}
				context.info("Created TL-Script method " + methodName + " => " + qualifiedMethodName);
			}
		}
		_buildersByName = buildersByName;
		_factories = _buildersByName.entrySet()
			.stream()
			.collect(Collectors.toMap(Entry::getKey, e -> new SearchBuilder.BuilderFactory(e.getValue())));
	}

	private static TLScriptMethod.Builder.Config asBuilderConfig(ConfigurationItem c) {
		return (TLScriptMethod.Builder.Config) c;
	}

	@Override
	public Factory getFactory(Object type) {
		return _factories.get(type);
	}

	@Override
	public MethodBuilder<?> getMethodBuilder(String functionName) {
		return _buildersByName.get(functionName);
	}

	@Override
	public Collection<String> getMethodNames() {
		return _buildersByName.keySet();
	}

	@Override
	public Optional<String> getDocumentation(DisplayContext context, String functionName) {
		Builder builder = _buildersByName.get(functionName);
		if (builder == null) {
			return Optional.empty();
		}
		HTMLFragment documentation = builder.documentation();
		if (documentation == null) {
			return Optional.empty();
		}
		StringWriter sw = new StringWriter();
		try (TagWriter out = new TagWriter(sw)) {
			documentation.write(context, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return Optional.of(sw.toString());
	}

}

