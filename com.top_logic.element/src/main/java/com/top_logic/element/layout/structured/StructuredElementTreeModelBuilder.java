/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import java.io.IOException;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.grid.StructuredElementTreeGridModelBuilder;
import com.top_logic.element.layout.table.tree.StructuredElementTreeTableBuilder;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.form.treetable.component.ConfigurableStructureEditComponent;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * This is an builder class which creates tree models for a whole element
 * structure.
 * <p>
 * To be used in the {@link ConfigurableStructureEditComponent} as
 * {@link com.top_logic.layout.form.treetable.component.ConfigurableStructureEditComponent.Config#getTreeModelBuilder()
 * model builder}.
 * </p>
 * 
 * @see StructuredElementTreeTableBuilder
 * @see StructuredElementTreeBuilder
 * @see StructuredElementTreeGridModelBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StructuredElementTreeModelBuilder
		extends AbstractConfiguredInstance<StructuredElementTreeModelBuilder.Config> implements ModelBuilder {

	public interface Config extends PolymorphicConfiguration<ModelBuilder> {
		@Name(TREE_FILTER_ATTRIBUTE)
		@InstanceFormat
		Filter<? super StructuredElement> getTreeFilter();
	}

	public static final String TREE_FILTER_ATTRIBUTE = "treeFilter";
	protected final Filter<? super StructuredElement> _treeFilter;

	public StructuredElementTreeModelBuilder(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		Filter<? super StructuredElement> filter = config.getTreeFilter();
		if (filter == null) {
			filter = FilterFactory.trueFilter();
		}
		this._treeFilter = filter;
	}

	public void writeFieldsAsXML(TagWriter anOut) throws IOException {
		if (_treeFilter != FilterFactory.trueFilter()) {
			anOut.writeAttribute(TREE_FILTER_ATTRIBUTE, _treeFilter.getClass().getName());
		}
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		return new DefaultMutableTLTreeModel(new StructuredElementTreeBuilder(_treeFilter), businessModel);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof StructuredElement;
	}

}
