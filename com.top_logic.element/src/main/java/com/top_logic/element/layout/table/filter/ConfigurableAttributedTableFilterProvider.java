/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.filter;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.model.TLClass;
import com.top_logic.layout.table.component.TableFilterProvider;

/**
 * A {@link TableFilterProvider} which creates
 * {@link com.top_logic.layout.table.TableFilter} for
 * {@link com.top_logic.model.TLStructuredTypePart} for a configured
 * {@link TLClass}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigurableAttributedTableFilterProvider extends AttributedTableFilterProvider {

	public interface Config extends PolymorphicConfiguration<TableFilterProvider> {
		@Name(META_ELEMENT_PROPERTY)
		@Mandatory
		String getMetaElement();
	}

	/**
	 * Property for configuring {@link #metaElementName}.
	 */
	private static final String META_ELEMENT_PROPERTY = "metaElement";

	/**
	 * The (configured) name of the {@link TLClass} for which this provider
	 * instantiates {@link com.top_logic.layout.table.TableFilter}.
	 */
	private final String metaElementName;

	/**
	 * base {@link TLClass}
	 */
	private TLClass me;

	/**
	 * Creates a {@link ConfigurableAttributedTableFilterProvider} which
	 * initializes {@link com.top_logic.layout.table.TableFilter} based on a
	 * configured {@link TLClass}.
	 */
	public ConfigurableAttributedTableFilterProvider(InstantiationContext context, Config config) throws ConfigurationException {
		this.metaElementName = config.getMetaElement();
		me = MetaElementFactory.getInstance().getGlobalMetaElement(metaElementName);
		if (me == null) {
			throw new ConfigurationException("No global " + TLClass.class.getSimpleName() + " with name '"
				+ metaElementName + "' available");
		}
	}

	@Override
	protected TLClass getMetaElement() {
		return me;
	}

}
