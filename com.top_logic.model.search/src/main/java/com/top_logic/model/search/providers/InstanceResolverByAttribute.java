/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.exporter.Resolvers;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.util.TLModelPartRef;

/**
 * {@link InstanceResolver} that identifies objects by attributes with unique values.
 * 
 * <p>
 * This resolver should only be used, if the resolved type has a table mapping and the attribute of
 * primitive type and mapped to a DB column with a unique index. If this is not the case, resolving
 * objects becomes inefficient and memory consuming. In that case, make sure, that there is only a
 * small amount of such objects.
 * </p>
 */
@InApp
public class InstanceResolverByAttribute extends AbstractConfiguredInstance<InstanceResolverByAttribute.Config<?>>
		implements InstanceResolver {

	/**
	 * Configuration options for {@link InstanceResolverByIndex}.
	 */
	@TagName("resolver-by-attribute")
	@DisplayOrder({
		Config.TYPE,
		Config.ATTRIBUTE,
	})
	public interface Config<I extends InstanceResolverByAttribute> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * @see #getAttribute()
		 */
		String ATTRIBUTE = "attribute";

		/**
		 * The type to resolve.
		 */
		@Name(TYPE)
		@Mandatory
		TLModelPartRef getType();

		/**
		 * The identifying attribute.
		 */
		@Name(ATTRIBUTE)
		@Mandatory
		@Options(fun = PrimitiveAttributesOfType.class, args = @Ref(TYPE), mapping = TLModelPartRef.PartMapping.class)
		TLModelPartRef getAttribute();

		/**
		 * Option provider function for {@link Config#getAttribute()}.
		 */
		class PrimitiveAttributesOfType extends Function1<List<? extends TLStructuredTypePart>, TLModelPartRef> {
			@Override
			public List<? extends TLStructuredTypePart> apply(TLModelPartRef arg) {
				if (arg == null) {
					return Collections.emptyList();
				}
				try {
					return arg.resolveClass().getAllParts().stream()
						.filter(p -> p.getType().getModelKind() == ModelKind.DATATYPE).toList();
				} catch (ConfigurationException ex) {
					return Collections.emptyList();
				}
			}
		}
	}

	private final InstanceResolver _resolver;

	/**
	 * Creates a {@link InstanceResolverByIndex} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InstanceResolverByAttribute(InstantiationContext context, Config<?> config)
			throws ConfigurationException, ParseException {
		super(context, config);

		TLClass type = config.getType().resolveClass();
		TLStructuredTypePart part = (TLStructuredTypePart) config.getAttribute().resolvePart();

		InstanceResolver resolver = Resolvers.idColumnResolver(type, part);
		if (resolver == null) {
			resolver = new InstanceResolverByIndex(type, part);
		}
		_resolver = resolver;
	}

	@Override
	public TLObject resolve(Log log, String kind, String id) {
		return _resolver.resolve(log, kind, id);
	}

	@Override
	public String buildId(TLObject obj) {
		return _resolver.buildId(obj);
	}

}
