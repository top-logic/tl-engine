/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.Step;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.ModelKind;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.access.WithStorageAttribute;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.model.instance.exporter.ResolverByColumnAttribute;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
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
	public interface Config<I extends InstanceResolverByAttribute> extends PolymorphicConfiguration<I>, ConfigPart {
		/**
		 * @see #getAnnotation()
		 */
		String ANNOTATION = "annotation";

		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * @see #getAttribute()
		 */
		String ATTRIBUTE = "attribute";

		/**
		 * The container of this configuration for accessing context.
		 */
		@Hidden
		@Name(ANNOTATION)
		@Container
		TLAnnotation getAnnotation();

		/**
		 * The type to resolve (only accessible in the config editor).
		 */
		@Hidden
		@Name(TYPE)
		@DerivedRef(steps={ @Step(ANNOTATION), @Step(TLAnnotation.ANNOTATED),
			@Step(
				type = com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder.EditModel.class, 
				value = com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder.EditModel.EDITED_TYPE) })
		TLStructuredType getType();

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
		class PrimitiveAttributesOfType extends Function1<List<? extends TLStructuredTypePart>, TLStructuredType> {
			@Override
			public List<? extends TLStructuredTypePart> apply(TLStructuredType arg) {
				if (arg == null) {
					return Collections.emptyList();
				}
				return arg.getAllParts().stream()
						.filter(p -> p.getType().getModelKind() == ModelKind.DATATYPE).toList();
			}
		}
	}

	private InstanceResolver _resolver;

	/**
	 * Creates a {@link InstanceResolverByIndex} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public InstanceResolverByAttribute(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public TLObject resolve(I18NLog log, String kind, String id) {
		return _resolver.resolve(log, kind, id);
	}

	@Override
	public String buildId(TLObject obj) {
		return _resolver.buildId(obj);
	}

	@Override
	public void initType(TLStructuredType type) {
		TLStructuredTypePart part = (TLStructuredTypePart) getConfig().getAttribute().resolvePart();

		InstanceResolver resolver = idColumnResolver(type, part);
		if (resolver == null) {
			resolver = new InstanceResolverByIndex(part);
		}
		resolver.initType(type);

		_resolver = resolver;
	}

	/**
	 * Builds an {@link InstanceResolver} for a DB column-mapped attribute.
	 *
	 * @return The resolver or <code>null</code>, when there is no column mapping.
	 */
	public static InstanceResolver idColumnResolver(TLStructuredType type, TLStructuredTypePart idAttribute) {
		TableName annotation = type.getAnnotation(TableName.class);
		if (annotation == null) {
			return null;
		}

		String tableName = annotation.getName();
		StorageDetail storage = idAttribute.getStorageImplementation();

		String idColumn = idAttribute.getName();
		if (storage instanceof ConfiguredInstance<?> configuredInstance) {
			PolymorphicConfiguration<?> storageConfig = configuredInstance.getConfig();
			if (storageConfig instanceof WithStorageAttribute columnStorageConfig) {
				idColumn = columnStorageConfig.getStorageAttribute();
			}
		}

		TLType idType = idAttribute.getType();
		if (idType instanceof TLPrimitive idValueType) {
			return new ResolverByColumnAttribute(idAttribute, idValueType, tableName, idColumn);
		}

		return null;
	}

}
