/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.form.FormGroupAccessor;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Default mutable {@link TableDeclaration} implementation for programatic use.
 * 
 * @see TreeTableRenderer
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultTableDeclaration extends TableDeclarationBase {

    public static final String DEFAULT_COLUMN_NAME = "default";

    private List        columnNames   = new ArrayList();
	private Map         columnsByName = new HashMap();

    /** Please {@link #addColumnDeclaration(String, ColumnDeclaration)}s when using this CTor */ 
	public DefaultTableDeclaration(ResPrefix resPrefix, ResourceProvider aResProvider) {
	    resourcePrefix   = resPrefix;
	    resourceProvider = aResProvider;
	}

    /** Please {@link #addColumnDeclaration(String, ColumnDeclaration)}s when using this CTor */ 
    public DefaultTableDeclaration() {
		this(ResPrefix.GLOBAL, MetaResourceProvider.INSTANCE); // not very useful except without
																// headers, well
    }

    /** Create a Table declaration assuming a Default column an the given FormFieldNames  */
	public DefaultTableDeclaration(ResPrefix resPrefix, String formFieldNames[]) { // TODO JDK 1.5
																					// use ...
        this(resPrefix, MetaResourceProvider.INSTANCE);
        this.addColumnDeclaration(DEFAULT_COLUMN_NAME, new DefaultColumnDeclaration(ColumnDeclaration.DEFAULT_COLUMN));
        ControlProvider          cp     = DefaultFormFieldControlProvider.INSTANCE;
        for (int i = 0; i < formFieldNames.length; i++) {
            this.addColumnDeclaration(formFieldNames[i], new DefaultColumnDeclaration(cp)); 
        }
        // As we just added ColumnDeclaration.CONTROL_COLUMN this access-method should be used. 
        accessor = FormGroupAccessor.INSTANCE;
 
    }

    public void addColumnDeclaration(String name, ColumnDeclaration column) {
		if (columnsByName.put(name, column) != null) {
		    throw new IllegalArgumentException("Column '" + name + "' was already added");
		}
		columnNames.add(name);
	}

	@Override
	public List getColumnNames() {
		return columnNames;
	}

	@Override
	public ColumnDeclaration getColumnDeclaration(String columnName) {
		return (ColumnDeclaration) columnsByName.get(columnName);
	}
	
}
