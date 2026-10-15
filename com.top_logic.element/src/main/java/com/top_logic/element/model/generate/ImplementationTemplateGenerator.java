/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.model.util.TLModelNamingConvention.*;

import java.util.List;

import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.binding.DynamicBinding;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.config.JavaClass;
import com.top_logic.model.util.TLModelNamingConvention;

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

		/* The implementation class is non-abstract, even for an abstract type (whose class is
		 * generated only when the type carries an implementation binding): An object of a concrete
		 * subtype that is defined dynamically, without a generated class of its own, is implemented
		 * by it, see DynamicBinding. */
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

	/**
	 * The name of the class the implementation class of the given type extends: the
	 * implementation class of its primary (first) generalization, or {@link AttributedWrapper} for
	 * a type without generalizations.
	 *
	 * <p>
	 * A generalization without an implementation class of its own (see
	 * {@link #hasImplementationClass(TLClass)}) contributes the implementation class of its own
	 * primary generalization. This is the rule {@link DynamicBinding} applies when it chooses the
	 * class implementing an object at runtime.
	 * </p>
	 */
	private String superTypeSpec(TLType type) {
		List<TLClass> superTypes = generalizations(type);
		if (superTypes.isEmpty()) {
			return AttributedWrapper.class.getName();
		}
		TLClass primary = superTypes.get(0);
		if (hasImplementationClass(primary)) {
			return dropPackage(implementationName(primary));
		}
		return superTypeSpec(primary);
	}

	/**
	 * Whether the given type has an implementation class: the class of its {@link JavaClass}
	 * annotation, or the class generated for it by naming convention (see
	 * {@link TLModelNamingConvention#implementationName(TLType)}).
	 *
	 * <p>
	 * A class is generated for a concrete type and for an abstract type with a {@link JavaClass}
	 * annotation, but not for an abstract type without one.
	 * </p>
	 */
	private static boolean hasImplementationClass(TLClass type) {
		return implementationName(type) != null
			&& (!type.isAbstract() || type.getAnnotation(JavaClass.class) != null);
	}

	private void writeImports() {
		importLine("com.top_logic.knowledge.objects.KnowledgeObject");
		nl();
	}

}
