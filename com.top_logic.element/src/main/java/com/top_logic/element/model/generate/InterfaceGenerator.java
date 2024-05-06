/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.generate;

import static com.top_logic.basic.generate.CodeUtil.*;
import static com.top_logic.model.util.TLModelNamingConvention.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelNamingConvention;

/**
 * Code generator creating the interface for the data aspect of {@link TLType} instances.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InterfaceGenerator extends BaseClassGenerator {

	/**
	 * Creates a new {@link InterfaceGenerator}.
	 * 
	 * @param type
	 *        Type to generate data interfaces for.
	 */
	public InterfaceGenerator(TLType type) {
		super(packageName(interfaceBaseName(type)), type);
	}

	@Override
	public String className() {
		return simpleClassName(interfaceBaseName(type()));
	}

	@Override
	protected void writeBody() {
		writeImports();
		javadocStart();
		commentLine("Basic interface for {@link " + '#' + typeNameConstant(type()) + "} business objects.");
		line(" * ");
		writeCvsTags();
		javadocStop();
		line("public interface " + className() + getExtends() + " {");
		{
			nl();
			writeTypeNameConstant();
			writePartConstants();
			writeParts();
		}
		line("}");
	}

	private void writeTypeNameConstant() {
		javadocStart();
		commentLine("Name of type <code>" + typeName() + "</code>");
		javadocStop();
		line("String " + typeNameConstant(type()) + " = " + toStringLiteral(typeName()) + ";");
		nl();
	}

	private void writePartConstants() {
		if (!(type() instanceof TLStructuredType)) {
			return;
		}
		Collection<? extends TLStructuredTypePart> localParts = ((TLStructuredType) type()).getLocalParts();
		if (localParts.isEmpty()) {
			return;
		}
		for (TLStructuredTypePart part : sort(localParts)) {
			if (part.isOverride()) {
				continue;
			}
			javadocStart();
			commentLine("Part <code>" + part.getName() + "</code> of <code>" + type().getName() + "</code>");
			commentLine("");
			commentLine("<p>");
			commentLine("Declared as <code>" + part.getType() + "</code> in configuration.");
			commentLine("</p>");
			javadocStop();
			line("String " + partNameConstant(part) + " = " + toStringLiteral(part.getName()) + ";");
			nl();
		}
	}

	private void writeParts() {
		if (!(type() instanceof TLClass)) {
			return;
		}
		Collection<? extends TLStructuredTypePart> localParts = ((TLClass) type()).getLocalParts();
		for (TLStructuredTypePart part : sort(localParts)) {
			// Special handling for the name attribute to prevent clash with implicitly inherited
			// interface TLNamed.
			if (part.getName().equals(TLNamed.NAME_ATTRIBUTE)) {
				continue;
			}
			writePart(part);
		}
	}

	private void writePart(TLStructuredTypePart part) {
		String partNameConstant = partNameConstant(part);
		String accessorSuffix = CodeUtil.toUpperCaseStart(part.getName());

		if (!noGetter()) {
			writeGetter(part);
			if (shouldGenerateModificationGetter(part) && !noElementClasses()) {
				generateModificationGetter(part, partNameConstant, accessorSuffix);
			}
		}
		if (!noSetter()) {
			/* Don't generate a setter for overrides, as they are useless. */
			if (!(isReadOnly(part) || part.isOverride())) {
				writeSetter(part, partNameConstant, accessorSuffix);
			}
		}
	}

	private void writeSetter(TLStructuredTypePart part, String partNameConstant, String accessorSuffix) {
		javadocStart();
		commentLine("Setter for part {@link #" + partNameConstant + "}.");
		javadocStop();
		String setterPrefix = setterPrefix();
		/* The property "name" is always an override, as getName() is already defined in
		 * TLNamed. */
		if ("Name".equals(accessorSuffix) && setterPrefix.equals("set")) {
			line("@Override");
		}
		line("default void " + setterPrefix + accessorSuffix + "(" + getJavaReturnType(part, false) + " newValue) {");
		{
			StringBuilder setterImpl = new StringBuilder();
			setterImpl.append("tUpdateByName(");
			setterImpl.append(partNameConstant(part));
			setterImpl.append(", ");
			setterImpl.append("newValue");
			setterImpl.append(");");
			line(setterImpl.toString());
		}
		line("}");
		nl();

		if (part.isMultiple()) {
			String singularSuffix = CodeUtil.singularName(accessorSuffix);

			javadocStart();
			commentLine("Adds a value to the {@link #" + partNameConstant + "} reference.");
			javadocStop();
			line("default void " + "add" + singularSuffix + "(" + getContentType(part) + " newValue) {");
			{
				StringBuilder setterImpl = new StringBuilder();
				setterImpl.append("tAddByName(");
				setterImpl.append(partNameConstant(part));
				setterImpl.append(", ");
				setterImpl.append("newValue");
				setterImpl.append(");");
				line(setterImpl.toString());
			}
			line("}");
			nl();

			javadocStart();
			commentLine("Removes the given value from the {@link #" + partNameConstant + "} reference.");
			javadocStop();
			line("default void " + "remove" + singularSuffix + "(" + getContentType(part) + " oldValue) {");
			{
				StringBuilder setterImpl = new StringBuilder();
				setterImpl.append("tRemoveByName(");
				setterImpl.append(partNameConstant(part));
				setterImpl.append(", ");
				setterImpl.append("oldValue");
				setterImpl.append(");");
				line(setterImpl.toString());
			}
			line("}");
			nl();
		}
	}

	private void writeGetter(TLStructuredTypePart part) {
		String javaType = getJavaReturnType(part);
		String partNameConstant = partNameConstant(part);
		String accessorSuffix = CodeUtil.toUpperCaseStart(part.getName());

		javadocStart();
		commentLine("Getter for part {@link #" + partNameConstant + "}.");
		javadocStop();
		String getterPrefix = getterPrefix();
		/* The property "name" is always an override, as getName() is already defined in TLNamed. */
		if (("Name".equals(accessorSuffix) && getterPrefix.equals("get")) || part.isOverride()) {
			line("@Override");
		}
		if (suppressUncheckedWarnings(part, javaType)) {
			line("@SuppressWarnings(\"unchecked\")");
		}
		line("default " + javaType + " " + getterPrefix + accessorSuffix + "() {");
		{
			StringBuilder getterImpl = new StringBuilder();
			getterImpl.append("return ");
			getterImpl.append(typeCast(part, javaType));
			getterImpl.append("tValueByName(");
			getterImpl.append(partNameConstant(part));
			getterImpl.append(")");
			getterImpl.append(";");
			line(getterImpl.toString());
		}
		line("}");
		nl();
	}

	private String typeCast(TLStructuredTypePart part, String javaType) {
		String cast;
		if ("Object".equals(javaType)) {
			cast = "";
		} else {
			String javaTypeCast = getJavaTypeCast(part);
			cast = "(" + javaTypeCast + ") ";
		}
		return cast;
	}

	private boolean suppressUncheckedWarnings(TLStructuredTypePart part, String javaType) {
		final boolean suppressUncheckedWarning;
		if ("Object".equals(javaType)) {
			suppressUncheckedWarning = false;
		} else {
			String javaTypeCast = getJavaTypeCast(part);
			suppressUncheckedWarning = containsGeneric(javaTypeCast);
		}
		return suppressUncheckedWarning;
	}

	private void generateModificationGetter(TLStructuredTypePart part, String partNameConstant, String accessorSuffix) {
		String javaType = getJavaReturnType(part);
		String targetType = getJavaReturnType(part, false);

		javadocStart();
		commentLine("Live view of the {@link #" + partNameConstant + "} part.");
		commentLine("<p>");
		commentLine("Changes to this {@link java.util.Collection} change directly the attribute value.");
		commentLine("The caller has to take care of the transaction handling.");
		commentLine("</p>");
		javadocStop();
		line("default " + targetType + " " + getModificationGetter(accessorSuffix) + "() {");
		{
			line("com.top_logic.model.TLStructuredTypePart attribute = tType().getPart(" + partNameConstant(part)
					+ ");");

			if (suppressUncheckedWarnings(part, javaType)) {
				line("@SuppressWarnings(\"unchecked\")");
			}
			line(targetType + " result = (" + targetType + ") "
					+ "com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil"
					+ ".getLiveCollection(this, attribute);");

			line("return result;");
		}
		line("}");
		nl();
	}

	private void writeImports() {
		if (generalizations(type()).isEmpty() && !(noGetter() && noSetter())) {
			if (isStructure(module())) {
				checkElementClassesAllowedInStructures();
				importLine("com.top_logic.element.structured.StructuredElement");
			} else {
				importLine("com.top_logic.knowledge.wrap.Wrapper");
			}
			nl();
		}
	}

	private String getExtends() {
		TLType type = type();
		List<TLClass> generalizations = generalizations(type);
		List<Set<String>> redundantInterf = generalizations
			.stream()
			.map(this::redundantIntfs)
			.collect(Collectors.toList());
		
		List<String> finalSuperInterfaces = new ArrayList<>();
		
		mainLoop:
		for (TLClass superClass : generalizations) {
			String superClassIntf = interfaceName(superClass);
			if (superClassIntf == null) {
				// No interface for super class detected.
				continue;
			}
			
			for (int i = 0; i < redundantInterf.size(); i++) {
				Set<String> redundants = redundantInterf.get(i);
				if (redundants.contains(superClassIntf)) {
					System.out.println("Do not extend interface '" + superClassIntf + "' of generalisation '"
							+ superClass
							+ "', as this interface is a super interface of different generalisation '"
							+ generalizations.get(i)
							+ "'");
					continue mainLoop;
				}
			}
			if (finalSuperInterfaces.contains(superClassIntf)) {
				// Different generalizations of type may have use interface, e.g.
				// tl.element:StructuredElement and tl.element:StructuredElementContainer.
				continue;
			}
			finalSuperInterfaces.add(superClassIntf);
		}

		if (type instanceof TLClass) {
			TLStructuredTypePart namePart = ((TLClass) type).getPart(TLNamed.NAME_ATTRIBUTE);
			if (namePart != null && locallyDefined(type, namePart) && isNotOverridden(namePart)) {
				/* Add implicit TLNamed interface to avoid clash with implicitly inherited methods
				 * from that interface by the implementation class. */
				// TLNamed extends TLObject
				finalSuperInterfaces.remove(TLObject.class.getName());
				if (finalSuperInterfaces.contains(StructuredElement.class.getName())) {
					// StructuredElement extends TLNamed.
				} else if (
					redundantInterf
						.stream()
						.flatMap(Set::stream)
					.anyMatch(Predicate.isEqual(StructuredElement.class.getName()))) {
						// Any super interface is StructuredElements which extends TLNamed.
				} else {
					finalSuperInterfaces.add(TLNamed.class.getName());
				}
			}
		}

		if (finalSuperInterfaces.isEmpty()) {
			return "";
		}

		String allSuperIntf = finalSuperInterfaces.stream().collect(Collectors.joining(", "));

		return " extends " + allSuperIntf;
	}

	/**
	 * Whether the attribute is defined at the given type.
	 */
	private boolean locallyDefined(TLType type, TLStructuredTypePart namePart) {
		return namePart.getOwner() == type;
	}

	/**
	 * Whether the attribute is not overridden from a super type.
	 */
	private boolean isNotOverridden(TLStructuredTypePart namePart) {
		return namePart.getDefinition() == namePart;
	}


	/**
	 * Determines the interfaces that may not be implemented because it is a super interface of the
	 * interface for the given type.
	 */
	private Set<String> redundantIntfs(TLClass type) {
		String interfaceName = interfaceName(type);
		if (interfaceName == null) {
			return Collections.emptySet();
		}
		Set<String> parentIntfs = allParentIntfs(type);
		// It is possible that the type and it's parent use the same implementation interface. In
		// this case the interface is not redundant.
		parentIntfs.remove(interfaceName);
		return parentIntfs;
	}

	/**
	 * Determines all annotated {@link TLModelNamingConvention#interfaceName(TLType) interface}
	 * names for the ancestors of the given type. A potential interface for the given type is
	 * <b>not</b> included.
	 */
	private Set<String> allParentIntfs(TLClass type) {
		HashSet<String> result = new HashSet<>();
		addImplementedIntf(result, type.getGeneralizations());
		return result;
	}

	/**
	 * Adds the annotated {@link TLModelNamingConvention#interfaceName(TLType) interface} names for
	 * all types and its ancestors to the given set.
	 */
	private void addImplementedIntf(HashSet<String> result, Collection<TLClass> types) {
		for (TLClass type : types) {
			String typeIntf = interfaceName(type);
			if (typeIntf != null) {
				result.add(typeIntf);
			}
			addImplementedIntf(result, type.getGeneralizations());
		}
	}

	private static boolean containsGeneric(String javaTypeCast) {
		return javaTypeCast.contains(List.class.getName())
			|| javaTypeCast.contains(Collection.class.getName())
			|| javaTypeCast.contains(Set.class.getName());
	}

}
