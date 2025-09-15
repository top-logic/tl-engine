/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.export;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRefsFormat;

/**
 * {@link ExportCustomization} that excludes a configured attribute or reference.
 */
public class ExcludeParts extends AbstractConfiguredInstance<ExcludeParts.Config<?>> implements ExportCustomization {

	/**
	 * Configuration options for {@link ExcludeParts}.
	 */
	@TagName("exclude-parts")
	public interface Config<I extends ExcludeParts> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * @see #getParts()
		 */
		String PARTS = "parts";

		/**
		 * The type from which to exclude attributes.
		 * 
		 * @implNote This property is only required for selecting attributes in the UI.
		 */
		@Name(TYPE)
		TLModelPartRef getType();

		/**
		 * Attributes to exclude from export.
		 */
		@Name(PARTS)
		@Mandatory
		@Options(fun = AttributesOfType.class, args = @Ref(TYPE), mapping = TLModelPartRef.PartMapping.class)
		@Format(TLModelPartRefsFormat.class)
		List<TLModelPartRef> getParts();

		/**
		 * Option provider for {@link Config#getParts()}.
		 */
		class AttributesOfType extends Function1<List<? extends TLStructuredTypePart>, TLModelPartRef> {
			@Override
			public List<? extends TLStructuredTypePart> apply(TLModelPartRef arg) {
				if (arg == null) {
					return Collections.emptyList();
				}
				try {
					return arg.resolveClass().getAllParts();
				} catch (ConfigurationException ex) {
					return Collections.emptyList();
				}
			}
		}
	}

	/**
	 * Creates a {@link ExcludeParts} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ExcludeParts(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void perform(XMLInstanceExporter exporter) {
		for (TLModelPartRef attr : getConfig().getParts()) {
			exporter.addExclude(attr.resolve());
		}
	}

}
