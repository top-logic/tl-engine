/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.model.util.TLModelNamingConvention.*;

import com.top_logic.basic.StringServices;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;

/**
 * Code generator creating the interface template for the business aspect of {@link TLClass}
 * instances.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class InterfaceTemplateGenerator extends TemplateGenerator {

	/**
	 * Creates a new {@link InterfaceTemplateGenerator}.
	 * 
	 * @param type
	 *        {@link TLType} to generate interface templates for.
	 */
	public InterfaceTemplateGenerator(TLType type) {
		super(packageName(interfaceName(type)), type);
	}

	@Override
    public String className() {
		return simpleClassName(interfaceName(type()));
    }

    @Override
    protected void writeBody() {
		javadocStart();
		commentLine(
			"Interface for {@link " + '#' + typeNameConstant(type()) + "} business objects.");
		commentLine(StringServices.EMPTY_STRING);
        writeCvsTags();
		javadocStop();
		line("public interface " + className() + " extends " + getExtends() + " {");
        {
            nl();
			comment("Wrapper functionality.");
            nl();
        }
        line("}");
    }

	private String getExtends() {
		return dropPackage(interfaceBaseName(type()));
	}
}
