/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.table.AttributedStructureEditComponent;
import com.top_logic.layout.form.treetable.component.TableDeclarationProvider;

/**
 * {@link AttributedStructureEditComponent}, with a enhanced {@link TableDeclarationProvider}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoStructuredEditComponent extends AttributedStructureEditComponent {

	public DemoStructuredEditComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected TableDeclarationProvider getTableDeclarationProvider() {
		return new StructureEditTableDeclarationProvider() {
			@Override
			protected String getContentColumnStyle(String aColumn) {
				if (aColumn.equals("float")) {
					return "text-align: right";
				} else {
					return null;
				}
			}
		};
	}

}
