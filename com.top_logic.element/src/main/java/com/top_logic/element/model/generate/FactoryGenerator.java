/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.basic.generate.CodeUtil.*;
import static com.top_logic.model.util.TLModelNamingConvention.*;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.element.config.annotation.TLSingletons;
import com.top_logic.element.meta.kbbased.AbstractElementFactory;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.wrap.StructuredElementWrapperFactory;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.util.model.ModelService;

/**
 * Code generator for factory for a {@link TLModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FactoryGenerator extends TLModelGenerator {

	private final TLModule _module;

	private final String _structureNameConst;

	private String _factoryClassName;

	/**
	 * Creates a new {@link FactoryGenerator}.
	 * 
	 * @param factoryClassName
	 *        Full qualified name of the factory class.
	 * @param module
	 *        The {@link TLModule} to create factory for.
	 */
	public FactoryGenerator(String factoryClassName, TLModule module) {
		super(packageName(factoryClassName));
		_factoryClassName = factoryClassName;
		_module = module;
		_structureNameConst = CodeUtil.toAllUpperCase(_module.getName()) + "_STRUCTURE";
	}
		
	@Override
	public String className() {
		return simpleClassName(_factoryClassName);
	}

	@Override
	protected void writeBody() {
		javadocStart();
		commentLine("Factory for <code>" + _module.getName() + "</code> objects.");
		commentLine("");
		commentLine("<p>");
		commentLine("Note: this is generated code. Do not modify. Instead, create a subclass and register this in the module system.");
		commentLine("</p>");
		commentLine("");
		writeCvsTags();
		javadocStop();
		String fileType = noElementClasses() ? "interface" : "class";
		line("public " + fileType + " " + className() + getExtends() + " {");
		{
			
			nl();
			writeSingletonConstatns();
			writeConstants();
			writeTypeLookup();

			writeTypeNameConstants();

			if (!noElementClasses()) {
				if (isStructure(_module)) {
					writeSingletonLookups();
				}
				writeFactoryMethods();

				writeTechnicalMethods();
			}

		}
		line("}");
	}

	private void writeFactoryMethods() {
		nl();
		for (TLClass type : _module.getClasses()) {
			if (type.isAbstract()) {
				continue;
			}
			String interfaceName = dropPackage(interfaceName(type));

			String typeFactoryName = typeFactoryName(type);
			String typeAccessorName = typeAccessorName(type);

			javadocStart();
			commentLine("Create an instance of {@link " + interfaceName + "} type.");
			javadocStop();
			line(methodModifier() + " final " + interfaceName + " " + typeFactoryName
					+ "(" + type(TLObject.class) + " context) {");
			{
				line("return (" + interfaceName + ") createObject(" + typeAccessorName + "(), context);");
			}
			line("}");
			nl();

			javadocStart();
			commentLine("Create an instance of {@link " + interfaceName + "} type.");
			javadocStop();
			line(methodModifier() + " final " + interfaceName + " " + typeFactoryName + "() {");
			{
				line("return " + typeFactoryName + "(null);");
			}
			line("}");
			nl();
		}
	}

	private void writeSingletonConstatns() {
		TLSingletons singletons = _module.getAnnotation(TLSingletons.class);
		if (singletons != null) {
			for (SingletonConfig config : singletons.getSingletons()) {
				String rootInterface = getSingletonTypeName(config);

				javadocStart();
				commentLine("Name of singleton {@link #" + _structureNameConst + "}.");
				javadocStop();
				line("public static final String " + singletonConstName(config.getName()) + " = " + '"'
						+ config.getName() + '"' + ";");
				nl();
			}
		}
	}

	private void writeSingletonLookups() {
		TLSingletons singletons = _module.getAnnotation(TLSingletons.class);
		if (singletons != null) {
			for (SingletonConfig config : singletons.getSingletons()) {
				String rootInterface = dropPackage(getSingletonTypeName(config));
				String rootAccessorName = singletonLooupMethod(config.getName());
				String singletonConstName = singletonConstName(config.getName());

				javadocStart();
				commentLine("Singleton <code>" + config.getName() + "</code>.");
				javadocStop();
				line("public " + rootInterface + " " + rootAccessorName + "() {");
				{
					line("return (" + rootInterface + ") lookupSingleton(" + singletonConstName + ");");
				}
				line("}");
				nl();

				javadocStart();
				commentLine("Singleton <code>" + config.getName() + "</code> on given branch.");
				javadocStop();
				line("public " + rootInterface + " " + rootAccessorName
						+ "(" + type(Branch.class) + " requestedBranch) {");
				{
					line(
						"return (" + rootInterface + ") lookupSingleton(" + singletonConstName + ", requestedBranch);");
				}
				line("}");
				nl();

				javadocStart();
				commentLine(
					"Singleton <code>" + config.getName() + "</code> on given branch in given revision.");
				javadocStop();
				line("public " + rootInterface + " " + rootAccessorName
						+ "(" + type(Branch.class)
						+ " requestedBranch, " + type(Revision.class) + " historyContext) {");
				{
					line(
						"return (" + rootInterface + ") lookupSingleton(" + singletonConstName
								+ ", requestedBranch, historyContext);");
				}
				line("}");
				nl();
			}
		}
	}

	private String getSingletonTypeName(SingletonConfig config) {
		String typename = config.getTypeSpec();
		TLType rootType = _module.getType(typename);
		if (rootType == null) {
			throw new ConfigurationError("No type with name '" + typename + "' in module '" + _module.getName()
					+ "' but used as type in singleton configuration.");
		}
		String rootInterface = interfaceName(rootType);
		return rootInterface;
	}

	private String singletonLooupMethod(String name) {
		return "get" + CodeUtil.toCamelCaseFromAllUpperCase(name) + "Singleton";
	}

	private String singletonConstName(String name) {
		return "SINGLETON_" + name.toUpperCase();
	}

	private void writeTechnicalMethods() {
		javadocStart();
		commentLine("The singleton instance of {@link " + className() + "}.");
		javadocStop();
		line("public static " + className() + " getInstance() {");
		{
			line("return (" + className() + ") " + type(DynamicModelService.class) + ".getFactoryFor("
					+ _structureNameConst + ");");
		}
		line("}");
	}

	private void writeTypeNameConstants() {
		for (TLClass type : _module.getClasses()) {
			writeTypeNameConstant(type);
			writeTableConstant(type);
		}
	}

	private void writeTypeNameConstant(TLClass type) {
		String interfaceName = dropPackage(interfaceName(type));
		String typeNameConstant = typeNameConstant(type);
		String typeName = type.getName();

		javadocStart();
		commentLine(
			"Name of type <code>" + typeName + "</code> in structure {@link #" + _structureNameConst + "}.");
		commentLine("");
		commentLine("@deprecated Use {@link " + interfaceName + "#" + typeNameConstant + "}.");
		javadocStop();
		line("@Deprecated");
		line("public static final String " + nodeNameConstant(type) + " = " + interfaceName + "." + typeNameConstant
			+ ";");
		nl();
	}

	private void writeTableConstant(TLClass type) {
		if (type.isAbstract()) {
			/* Abstract types are not stored in tables. When they have a table annotation it is just
			 * a template for concrete specialisations. */
			return;
		}
		String tableName = TLAnnotations.getTable(type);
		javadocStart();
		commentLine("Storage table name of {@link #" + nodeNameConstant(type) + "} objects.");
		javadocStop();
		line(
			"public static final String " + tableNameConstant(type) + " = " + toStringLiteral(tableName) + ";");
		nl();
	}

	private String nodeNameConstant(TLClass type) {
		return CodeUtil.toAllUpperCase(type.getName()) + "_NODE";
	}

	private void writeConstants() {
		javadocStart();
		commentLine("Name of the structure <code>" + _module.getName() + "</code> defined by {@link " + className()
			+ "}.");
		javadocStop();
		line("public static final String " + _structureNameConst + " = " + toStringLiteral(_module.getName()) + ";");
		
		nl();

		for (TLEnumeration type : _module.getEnumerations()) {
			writeEnumNameConstant(type);
			for (TLClassifier classifier : sort(type.getClassifiers())) {
				writeClassifierConstant(type, classifier);
			}
		}

		for (TLPrimitive type : _module.getDatatypes()) {
			writeDataTypeNameConstant(type);
		}
	}

	private void writeClassifierConstant(TLEnumeration type, TLClassifier classifier) {
		javadocStart();
		commentLine("Name of the classifier <code>" + classifier.getName() + "</code> in enumeration {@value #"
				+ enumNameConstant(type) + "}.");
		javadocStop();
		line("public static final String " + classifierNameConstant(classifier) + " = "
				+ toStringLiteral(classifier.getName()) + ";");
		nl();
	}

	private void writeEnumNameConstant(TLEnumeration type) {
		javadocStart();
		commentLine("Name of the enumeration <code>" + type.getName() + "</code> in module {@value #"
				+ _structureNameConst + "}.");
		javadocStop();
		line("public static final String " + enumNameConstant(type) + " = " + toStringLiteral(type.getName()) + ";");
		nl();
	}

	private void writeDataTypeNameConstant(TLPrimitive type) {
		javadocStart();
		commentLine("Name of the data type <code>" + type.getName() + "</code> in module {@value #"
				+ _structureNameConst + "}.");
		javadocStop();
		line("public static final String " + typeNameConstant(type) + " = " + toStringLiteral(type.getName()) + ";");
		nl();
	}

	private void writeTypeLookup() {
		for (TLClass type : _module.getClasses()) {
			writeTypeLookup(type);
		}
		for (TLEnumeration type : _module.getEnumerations()) {
			writeEnumLookup(type);
		}
		for (TLPrimitive type : _module.getDatatypes()) {
			writeDataTypeLookup(type);
		}
	}

	private void writeEnumLookup(TLEnumeration type) {
		String enumAccessorName = enumAccessorName(type);
		String typeNameConstant = enumNameConstant(type);

		javadocStart();
		commentLine("Lookup {@value #" + typeNameConstant + "} enumeration.");
		javadocStop();
		line("public static " + type(TLEnumeration.class) + " " + enumAccessorName + "() {");
		{
			line(
				"return (" + type(TLEnumeration.class) + ") " + type(ModelService.class)
						+ ".getApplicationModel().getModule("
						+ _structureNameConst + ").getType("
						+ typeNameConstant + ");");
		}
		line("}");
		nl();

		for (TLClassifier part : sort(type.getClassifiers())) {
			writeClassifier(enumAccessorName, type, part);
		}

	}

	private void writeClassifier(String enumAccessorName, TLEnumeration enumeration, TLClassifier classifier) {
		javadocStart();
		commentLine("Lookup classifier {@value #" + classifierNameConstant(classifier) + "} of enumeration {@value #"
				+ enumNameConstant(enumeration) + "}.");
		javadocStop();
		line("public static " + type(TLClassifier.class) + " " + classifierAccessorName(classifier) + "() {");
		{
			line("return " + enumAccessorName + "().getClassifier(" + classifierNameConstant(classifier) + ");");
		}
		line("}");
		nl();
	}

	private void writeDataTypeLookup(TLPrimitive type) {
		String typeAccessorName = typeAccessorName(type);
		String typeNameConstant = typeNameConstant(type);

		javadocStart();
		commentLine("Lookup {@value #" + typeNameConstant + "} data type.");
		javadocStop();
		line("public static " + type(TLPrimitive.class) + " " + typeAccessorName + "() {");
		{
			line(
				"return (" + type(TLPrimitive.class) + ") " + type(ModelService.class)
						+ ".getApplicationModel().getModule("
						+ _structureNameConst + ").getType("
						+ typeNameConstant + ");");
		}
		line("}");
		nl();
	}

	private void writeTypeLookup(TLClass type) {
		// interface extends constants.
		String interfaceName = dropPackage(interfaceName(type));
		String typeNameConstant = typeNameConstant(type);
		String typeAccessorName = typeAccessorName(type);

		javadocStart();
		commentLine("Lookup {@link " + interfaceName + "} type.");
		javadocStop();
		line("public static " + type(TLClass.class) + " " + typeAccessorName + "() {");
		{
			line(
				"return (" + type(TLClass.class) + ") " + type(ModelService.class) + ".getApplicationModel().getModule("
					+ _structureNameConst + ").getType("
				+ interfaceName + "." + typeNameConstant + ");");
		}
		line("}");
		nl();

		for (TLStructuredTypePart part : sort(type.getLocalParts())) {
			writeTypePart(interfaceName, typeAccessorName, part);
		}

	}

	private String type(Class<?> type) {
		return type.getName();
	}

	private String methodModifier() {
		return noElementClasses() ? "default" : "public";
	}

	private void writeTypePart(String interfaceName, String typeAccessorName, TLStructuredTypePart part) {
		String partNameConstant = partNameConstant(part);

		javadocStart();
		commentLine("Lookup {@link " + interfaceName + "#" + partNameConstant + "} of {@link " + interfaceName + "}.");
		javadocStop();
		String partType;
		String cast;
		if (part instanceof TLReference) {
			partType = type(TLReference.class);
			cast = "(" + partType + ") ";
		} else if (part instanceof TLProperty) {
			partType = type(TLProperty.class);
			cast = "(" + partType + ") ";
		} else {
			partType = type(TLStructuredTypePart.class);
			cast = "";
		}
		line("public static " + partType + " " + typePartAccessorName(part) + "() {");
		{
			line("return " + cast + typeAccessorName + "().getPart(" + interfaceName + "." + partNameConstant + ");");
		}
		line("}");
		nl();
	}

	private String getExtends() {
		if (noElementClasses()) {
			return "";
		}
		String superTypeSpec;
		if (isStructure(_module)) {
			superTypeSpec = type(StructuredElementWrapperFactory.class);
		} else {
			superTypeSpec = type(AbstractElementFactory.class);
		}
		return " extends " + superTypeSpec;
	}

}
