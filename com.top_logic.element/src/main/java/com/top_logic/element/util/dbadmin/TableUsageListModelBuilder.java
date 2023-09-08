/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.ReferenceAttributeConfig;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} that finds references to a given table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TableUsageListModelBuilder<C extends TableUsageListModelBuilder.Config<?>>
		extends AbstractConfiguredInstance<C> implements ListModelBuilder {

	/**
	 * Configuration options for {@link TableUsageListModelBuilder}.
	 */
	public interface Config<I extends TableUsageListModelBuilder<?>> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getSchema()
		 */
		String SCHEMA = "schema";

		/**
		 * The property to access for generating the contents.
		 */
		@Name(SCHEMA)
		ModelSpec getSchema();

	}

	private final ChannelLinking _schema;

	/**
	 * Creates a {@link TableUsageListModelBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableUsageListModelBuilder(InstantiationContext context, C config) {
		super(context, config);
		_schema = context.getInstance(config.getSchema());
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		if (businessModel == null) {
			return Collections.emptyList();
		}

		SchemaConfiguration schema = (SchemaConfiguration) ChannelLinking.eval(component, _schema);
		MetaObjectName model = (MetaObjectName) businessModel;

		String name = model.getObjectName();
		if (StringServices.isEmpty(name)) {
			return Collections.emptyList();
		}

		return usages(schema, name);
	}

	/**
	 * All {@link Usage}s of the table type with the given name in the given schema.
	 */
	public static List<Usage> usages(SchemaConfiguration schema, String name) {
		return usageStream(schema, name).collect(Collectors.toList());
	}

	/**
	 * All {@link Usage}s of the table type with the given name in the given schema.
	 */
	public static Stream<Usage> usageStream(SchemaConfiguration schema, String typeName) {
		return Stream.concat(
			Stream.concat(
				typeStream(schema)
					.filter(t -> t instanceof AlternativeConfig)
					.map(t -> (AlternativeConfig) t)
					.filter(t -> t.getSpecialisations()
						.stream()
						.filter(s -> typeName.equals(s.getName()))
						.findFirst()
						.isPresent())
					.map(t -> new Usage(t, null)),
				tableStream(schema)
					.filter(t -> typeName.equals(t.getSuperClass()))
					.map(t -> new Usage(t, null))),
			tableStream(schema)
				.flatMap(t -> t.getAttributes()
					.stream()
					.filter(a -> a instanceof ReferenceAttributeConfig)
					.map(a -> (ReferenceAttributeConfig) a)
					.filter(r -> typeName.equals(r.getValueType()))
					.map(r -> new Usage(t, r)))
		);
	}

	private static Stream<MetaObjectConfig> tableStream(SchemaConfiguration schema) {
		return typeStream(schema)
			.filter(t -> t instanceof MetaObjectConfig)
			.map(t -> (MetaObjectConfig) t);
	}

	private static Stream<MetaObjectName> typeStream(SchemaConfiguration schema) {
		return schema.getMetaObjects()
			.getTypes()
			.values()
			.stream();
	}

	/**
	 * Value holder for the usage table.
	 */
	public static class Usage {
		private MetaObjectName _type;

		private ReferenceAttributeConfig _reference;

		/**
		 * Creates a {@link Usage}.
		 */
		public Usage(MetaObjectName type, ReferenceAttributeConfig reference) {
			_type = type;
			_reference = reference;
		}

		/**
		 * The type using the context type.
		 */
		public MetaObjectName getType() {
			return _type;
		}

		/**
		 * The reference using the context type.
		 */
		public ReferenceAttributeConfig getReference() {
			return _reference;
		}

		@Override
		public String toString() {
			if (_reference == null) {
				return _type.getObjectName();
			} else {
				return _type.getObjectName() + "." + _reference.getAttributeName();
			}
		}
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model == null || model instanceof MetaObjectName;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return true;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}
