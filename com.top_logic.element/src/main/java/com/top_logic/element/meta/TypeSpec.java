/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.util.model.TL5Types;

/**
 * Preliminary representation of attribute types.
 * 
 * <p>
 * Should be removed, if each type has its persistent model element.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypeSpec {

	/** Protocol prefix of a type specification referencing a model class by name. */
	public static final String INTERFACE_PROTOCOL = "interface";

	/** Protocol prefix of a type specification referencing an enumeration by name. */
	public static final String ENUM_PROTOCOL = TL5Types.ENUM_PROTOCOL;

	/** Qualified name for the core boolean type. */
	public static final String BOOLEAN_TYPE = "tl.core:Boolean";

	/** Qualified name for the core tristate type. */
	public static final String TRISTATE_TYPE = "tl.core:Tristate";

	/** Qualified name for the core float type. */
	public static final String FLOAT_TYPE = "tl.core:Float";

	/** Qualified name for the core double type. */
	public static final String DOUBLE_TYPE = "tl.core:Double";

	/** Qualified name for the core string type. */
	public static final String STRING_TYPE = "tl.core:String";

	/** Qualified name for the core long type. */
	public static final String LONG_TYPE = "tl.core:Long";

	/** Qualified name for the core int type. */
	public static final String INTEGER_TYPE = "tl.core:Integer";

	/** Qualified name for the core date type. */
	public static final String DATE_TYPE = "tl.core:Date";

	/** Qualified name for the core date with date time type. */
	public static final String DATE_TIME_TYPE = "tl.core:DateTime";

	/** Qualified name for the core date with time type. */
	public static final String TIME_TYPE = "tl.core:Time";

	/** Qualified name for the core binary type. */
	public static final String BINARY_TYPE = "tl.core:Binary";

	/** Qualified name for the core any type. */
	public static final String OBJECT_TYPE = "tl.util:Any";

	/** Qualified name for the core type holding external contacts. */
	public static final String EXTERNAL_CONTACT_TYPE = "tl.contact.external:ExternalContact";

	/** Qualified name for the core type holding external contacts as set. */
	public static final String EXTERNAL_CONTACT_SET_TYPE = "tl.contact.external:ExternalContactSet";

	/** Qualified name for the core country type. */
	public static final String COUNTRY_TYPE = "tl.util:Country";

	/** Qualified name for the core language type. */
	public static final String LANGUAGE_TYPE = "tl.util:Language";

	/** Qualified name for the core icon type. */
	public static final String ICON_TYPE = "tl.core:Icon";

	/** Qualified name for the color type. */
	public static final String COLOR_TYPE = "tl.util:Color";

	/** Qualified name for the internationalized string type. */
	public static final String I18N_STRING_TYPE = "tl.model.i18n:I18NString";

	/** Qualified name for the core JSON type. */
	public static final String JSON_TYPE = "tl.util:JSON";

	/** Qualified name for the core resource type. */
	public static final String RESOURCE_TYPE = "tl.util:Resource";

	/**
	 * Parses a type specification of the form {@code protocol:name}.
	 *
	 * @param typeSpec
	 *        The type specification. The protocol {@value #ENUM_PROTOCOL} references an
	 *        enumeration, {@value #INTERFACE_PROTOCOL} a class. Any other protocol is the name of
	 *        a structure followed by a comma separated list of node names.
	 * @return The parsed {@link TypeSpec}.
	 * @throws ConfigurationException
	 *         If the given specification is empty or has no protocol.
	 */
	public static TypeSpec lookup(String typeSpec) throws ConfigurationException {
		if (typeSpec.isEmpty()) {
			throw new ConfigurationException("Empty type.");
		}
	
		int protocolIndex = typeSpec.indexOf(':');
		if (protocolIndex > 0) {
			String protocol = typeSpec.substring(0, protocolIndex);
			String typeName = typeSpec.substring(protocolIndex + 1);
			if (ENUM_PROTOCOL.equals(protocol)) {
				return new EnumType(typeName);
			}
			else if (INTERFACE_PROTOCOL.equals(protocol)) {
				return new ElementType(typeName);
			}
			else {
				String structureName = protocol;
				String nodeNamesSpec = typeName;
				List<String> names = split(nodeNamesSpec);
				return new NodeType(structureName, names);
			}
		} else {
			throw new ConfigurationException(
				"No type protocol in type '" + typeSpec + "'.");
		}
	}

	private static List<String> split(String commaSeparatedNames) {
		List<String> names = null;
		int startIndex = 0;
		while (startIndex < commaSeparatedNames.length()) {
			int commaIndex = commaSeparatedNames.indexOf(',', startIndex);
			if (commaIndex < 0) {
				break;
			} else {
				if (names == null) {
					names = new ArrayList<>();
				}
				names.add(commaSeparatedNames.substring(startIndex, commaIndex));
				startIndex = commaIndex + 1;
			}
		}
		if (names == null) {
			names = Collections.singletonList(commaSeparatedNames);
		} else {
			names.add(commaSeparatedNames.substring(startIndex));
		}
		return names;
	}

	/**
	 * {@link TypeSpec} of a primitive data type.
	 */
	public static class DataType extends TypeSpec {

		private final String _name;

		protected DataType(String name) {
			_name = name;
		}

		/**
		 * The name of the data type.
		 */
		public String getName() {
			return _name;
		}

	}

	/**
	 * Base class for {@link TypeSpec}s referencing a model type.
	 */
	protected static abstract class ReferenceTypeSpec extends TypeSpec {

	}

	/**
	 * {@link TypeSpec} referencing an enumeration.
	 */
	public static final class EnumType extends ReferenceTypeSpec {

		private final String _enumName;

		public EnumType(String enumName) {
			_enumName = enumName;
		}

		/**
		 * The name of the referenced enumeration.
		 */
		public String getEnumName() {
			return _enumName;
		}
	}

	/**
	 * {@link TypeSpec} referencing a model class.
	 */
	public static final class ElementType extends ReferenceTypeSpec {

		private final String _metaElementName;

		public ElementType(String metaElementName) {
			_metaElementName = metaElementName;
		}

		/**
		 * The name of the referenced class.
		 */
		public String getMetaElementName() {
			return _metaElementName;
		}

	}

	/**
	 * {@link TypeSpec} referencing nodes of a structure.
	 */
	public static final class NodeType extends ReferenceTypeSpec {

		private final String _structureName;

		private final List<String> _nodeNames;

		public NodeType(String structureName, List<String> nodeNames) {
			_structureName = structureName;
			_nodeNames = nodeNames;
		}

		/**
		 * The name of the structure.
		 */
		public String getStructureName() {
			return _structureName;
		}

		/**
		 * The names of the referenced node types of the structure.
		 */
		public List<String> getNodeNames() {
			return _nodeNames;
		}

	}

}
