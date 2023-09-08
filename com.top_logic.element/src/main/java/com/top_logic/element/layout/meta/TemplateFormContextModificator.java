/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link FormContextModificator} used in layout templates.
 * 
 * <p>
 * For example to limit the amount of editable columns
 * </p>
 * 
 * @see FormContextModificatorTemplateParameters
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TemplateFormContextModificator extends DefaultFormContextModificator
		implements ConfiguredInstance<TemplateFormContextModificator.Config<?>> {

	/**
	 * Configuration of the {@link TemplateFormContextModificator}.
	 *
	 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
	 */
	public interface Config<I extends TemplateFormContextModificator>
			extends PolymorphicConfiguration<I>, FormContextModificatorTemplateParameters {
		// Empty.
	}

	private Config<?> _config;

	private Collection<String> _readOnlyColumns;

	/**
	 * Creates a {@link TemplateFormContextModificator} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateFormContextModificator(InstantiationContext context, Config<?> config) {
		_config = config;
		_readOnlyColumns = new HashSet<>(config.getReadOnlyColumns());
	}

	@Override
	public void modify(LayoutComponent component, String name, FormMember member, TLStructuredTypePart part,
			TLClass type, TLObject object, AttributeUpdate update, AttributeFormContext context,
			FormContainer group) {
		if (_readOnlyColumns.contains(name)) {
			member.setImmutable(true);
		}
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

}
