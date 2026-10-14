/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.basic.generate.CodeUtil.*;
import static com.top_logic.model.util.TLModelNamingConvention.*;

import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.util.model.ModelService;

/**
 * Code generator for the class holding the model metadata of a {@link TLEnumeration}.
 *
 * <p>
 * The generated class holds the names of the enumeration and its classifiers as constants and
 * static methods looking up the enumeration and its classifiers in the application model.
 * </p>
 *
 * @see com.top_logic.model.util.TLModelNamingConvention#enumClassName(TLEnumeration)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class EnumerationGenerator extends TLTypeGenerator {

	/**
	 * Creates a {@link EnumerationGenerator}.
	 *
	 * @param type
	 *        The {@link TLEnumeration} to create the class for.
	 */
	public EnumerationGenerator(TLEnumeration type) {
		super(packageName(enumClassName(type)), type);
	}

	@Override
	protected TLEnumeration type() {
		return (TLEnumeration) super.type();
	}

	@Override
	public String className() {
		return simpleClassName(enumClassName(type()));
	}

	@Override
	protected void writeBody() {
		javadocStart();
		commentLine("Model metadata of the enumeration <code>" + typeName() + "</code> in module <code>"
			+ module().getName() + "</code>.");
		commentLine("");
		commentLine("<p>");
		commentLine("Note: this is generated code. Do not modify.");
		commentLine("</p>");
		commentLine("");
		writeCvsTags();
		javadocStop();
		line("public final class " + className() + " {");
		{
			nl();
			writeTypeNameConstant();
			for (TLClassifier classifier : sort(type().getClassifiers())) {
				writeClassifierConstant(classifier);
			}
			writeTypeLookup();
			for (TLClassifier classifier : sort(type().getClassifiers())) {
				writeClassifierLookup(classifier);
			}
			writeConstructor();
		}
		line("}");
	}

	private void writeTypeNameConstant() {
		javadocStart();
		commentLine("Name of the enumeration <code>" + typeName() + "</code> in module <code>"
			+ module().getName() + "</code>.");
		javadocStop();
		line("public static final String " + ENUM_TYPE_NAME_CONSTANT + " = " + toStringLiteral(typeName()) + ";");
		nl();
	}

	private void writeClassifierConstant(TLClassifier classifier) {
		javadocStart();
		commentLine("Name of the classifier <code>" + classifier.getName() + "</code> in enumeration {@value #"
			+ ENUM_TYPE_NAME_CONSTANT + "}.");
		javadocStop();
		line("public static final String " + classifierConstant(classifier) + " = "
			+ toStringLiteral(classifier.getName()) + ";");
		nl();
	}

	private void writeTypeLookup() {
		String enumType = TLEnumeration.class.getName();

		javadocStart();
		commentLine("Lookup the enumeration {@value #" + ENUM_TYPE_NAME_CONSTANT + "}.");
		javadocStop();
		line("public static " + enumType + " " + ENUM_TYPE_ACCESSOR + "() {");
		{
			line("return (" + enumType + ") " + ModelService.class.getName()
				+ ".getApplicationModel().getModule(" + toStringLiteral(module().getName()) + ").getType("
				+ ENUM_TYPE_NAME_CONSTANT + ");");
		}
		line("}");
		nl();
	}

	private void writeClassifierLookup(TLClassifier classifier) {
		javadocStart();
		commentLine("Lookup the classifier {@value #" + classifierConstant(classifier) + "} of enumeration {@value #"
			+ ENUM_TYPE_NAME_CONSTANT + "}.");
		javadocStop();
		line("public static " + TLClassifier.class.getName() + " " + classifierAccessor(classifier) + "() {");
		{
			line("return " + ENUM_TYPE_ACCESSOR + "().getClassifier(" + classifierConstant(classifier) + ");");
		}
		line("}");
		nl();
	}

	private void writeConstructor() {
		javadocStart();
		commentLine("No instances, only static members.");
		javadocStop();
		line("private " + className() + "() {");
		{
			comment("No instances.");
		}
		line("}");
	}

}
