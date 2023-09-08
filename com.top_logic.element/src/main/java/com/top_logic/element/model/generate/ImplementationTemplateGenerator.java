/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.model.util.TLModelNamingConvention.*;

import java.util.List;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;

/**
 * Code generator that creates {@link Wrapper} templates for {@link TLType}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ImplementationTemplateGenerator extends TemplateGenerator {

	/**
	 * Creates a new {@link ImplementationTemplateGenerator}.
	 * 
	 * @param type
	 *        Type to generate implementation templates for.
	 */
	public ImplementationTemplateGenerator(TLType type) {
		super(packageName(implementationName(type)), type);
	}

	@Override
	public String className() {
		return simpleClassName(implementationName(type()));
	}

	@Override
	protected void writeBody() {
		writeImports();
		
		String interfaceSpec = dropPackage(interfaceName(type()));

		javadocStart();
		commentLine("Wrapper implementation class for <code>" + type() + "</code> business objects.");
		commentLine("");
		writeCvsTags();
		javadocStop();

		/* Generate non abstract java classes, because it may also possible for abstract TLClass
		 * that dynamically concrete subclasses may be defined which use this wrapper as
		 * implementation class. */
		line("public class " + className() + " extends " + getExtends() + " implements "
			+ interfaceSpec + " {");
		{
			nl();
			javadocStart();
			commentLine("Default constructor.");
			javadocStop();
			line("public " + className() + "(KnowledgeObject ko) {");
			{
				line("super(ko);");
			}
			line("}");
			nl();
		}
		line("}");
	}

	private String getExtends() {
		return superTypeSpec(type());

	}

	private String superTypeSpec(TLType type) {
		List<TLClass> superTypes = generalizations(type);
		if (!superTypes.isEmpty()) {
			TLClass primary = superTypes.get(0);
			if (primary.isAbstract()) {
				return superTypeSpec(primary);
			} else {
				return dropPackage(implementationName(primary));
			}
		} else {
			if (isStructure(module())) {
				return "com.top_logic.element.structured.wrap.AttributedStructuredElementWrapper";
			} else {
				return "com.top_logic.element.meta.kbbased.AttributedWrapper";
			}
		}
	}

	private void writeImports() {
		importLine("com.top_logic.knowledge.objects.KnowledgeObject");
		nl();
	}

}
