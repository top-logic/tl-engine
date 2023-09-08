/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.model.util.TLModelNamingConvention.*;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;

/**
 * Code generator for {@link TLClass} constant interfaces.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConstantGenerator extends TLTypeGenerator {

	/**
	 * Creates a new {@link ConstantGenerator}.
	 * 
	 * @param type
	 *        The {@link TLType} to create constants for.
	 */
	public ConstantGenerator(TLType type) {
		super(packageName(constantName(type)), type);
	}

	@Override
	public String className() {
		return simpleClassName(constantName(type()));
	}
	
	@Override
	protected void writeBody() {
		javadocStart();
		commentLine("Constant declarations for <code>" + typeName() + "</code> business objects.");
		commentLine("");
		commentLine("@deprecated Use {link " + interfaceName(type()) + "}.");
		commentLine("");
		writeCvsTags();
		javadocStop();
		line("@Deprecated");
		line("public interface " + className() + " extends " + interfaceName(type()) + " {");
		{
			comment("No longer in use.");
		}
		line("}");
	}

}
