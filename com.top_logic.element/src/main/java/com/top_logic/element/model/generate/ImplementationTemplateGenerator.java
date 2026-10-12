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

		line("public " + classModifier() + "class " + className() + " extends " + getExtends() + " implements "
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

	/**
	 * The modifier of the generated class declaration: {@code abstract} for an abstract type, empty
	 * otherwise.
	 *
	 * <p>
	 * An abstract type (an abstract class or an interface of the model) gets an abstract
	 * implementation class, so that abstract methods of its business interface need an
	 * implementation only in the classes of its concrete subtypes. An object of a subtype that is
	 * defined dynamically, without a generated class of its own, is implemented by the nearest
	 * non-abstract implementation class along its primary generalizations, see
	 * {@link DynamicBinding}.
	 * </p>
	 */
	private String classModifier() {
		return type() instanceof TLClass clazz && clazz.isAbstract() ? "abstract " : "";
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
	 * The implementation class of a type is the class of its implementation binding, or the class
	 * generated for it by naming convention (see
	 * {@link TLModelNamingConvention#implementationName(TLType)}). A type without such a class
	 * uses the implementation class of its own primary generalization. This is the rule
	 * {@link DynamicBinding} applies when it chooses the class implementing an object at runtime,
	 * except that an abstract class is a valid base class here, whereas {@link DynamicBinding}
	 * skips it, because it cannot implement an object.
	 * </p>
	 */
	private String superTypeSpec(TLType type) {
		List<TLClass> superTypes = generalizations(type);
		if (superTypes.isEmpty()) {
			return AttributedWrapper.class.getName();
		}
		TLClass primary = superTypes.get(0);
		String name = implementationName(primary);
		if (name != null) {
			return dropPackage(name);
		}
		return superTypeSpec(primary);
	}

	private void writeImports() {
		importLine("com.top_logic.knowledge.objects.KnowledgeObject");
		nl();
	}

}
