/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.inspector.model.InspectorTreeBuilder;
import com.top_logic.layout.inspector.resources.JavaFieldResourceProvider;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;

/**
 * {@link InspectorTreeNode} that builds properties by accessing the {@link Field}s of the
 * {@link #getBusinessObject()} using Java reflection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InspectorReflectionNode extends InspectorTreeNode {

	private static final Comparator<Field> COMPARE_BY_NAME = new Comparator<>() {
		@Override
		public int compare(Field o1, Field o2) {
			return FieldProperty.displayName(o1).compareTo(FieldProperty.displayName(o2));
		}
	};

	private static final Object UNACCESSIBLE = new NamedConstant("unaccessible");

	/**
	 * Creates a {@link InspectorReflectionNode}.
	 */
	public InspectorReflectionNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			InspectProperty property, Object userObject) {
		super(model, parent, userObject, property);
	}

	@Override
	public List<InspectorTreeNode> makeChildren() {
		Object object = getBusinessObject();
		if (object == null) {
			return Collections.emptyList();
		}

		Filter<? super InspectorTreeNode> filter = filter();
		List<InspectorTreeNode> children = new ArrayList<>();
		for (Field field : displayedFields(object.getClass())) {
			Inspectable inspectable = field.getAnnotation(Inspectable.class);
			if (inspectable != null && !inspectable.value()) {
				continue;
			}

			Object value = getFieldValue(field, object);
			InspectorTreeNode newNode = makeValueNode(new FieldProperty(field), value);

			if (filter.accept(newNode)) {
				children.add(newNode);
			}
		}

		return children;
	}

	/**
	 * Return a sorted list of fields defined by the given class.
	 * 
	 * @param type
	 *        The class to be inspected, must not be <code>null</code>.
	 * @return The sorted list of fields within the class, never <code>null</code>.
	 */
	protected List<Field> displayedFields(Class<?> type) {
		if (isPrimitive(type)) {
			return Collections.emptyList();
		} else {
			return allDeclaredFieldsSorted(type);
		}
	}

	private List<Field> allDeclaredFieldsSorted(Class<?> type) {
		List<Field> allFields = allDeclaredFields(type);
		Collections.sort(allFields, COMPARE_BY_NAME);

		return allFields;
	}

	private List<Field> allDeclaredFields(Class<?> type) {
		ArrayList<Field> result = new ArrayList<>();
		addAllDeclaredFields(result, type);
		return result;
	}

	private Object getFieldValue(Field field, Object object) {
		try {
			boolean accessible = field.isAccessible();

			Object fieldValue;
			if (!accessible) {
				field.setAccessible(true);
			}
			try {
				fieldValue = field.get(object);

			} finally {
				if (!accessible) {
					field.setAccessible(false);
				}
			}
			return fieldValue;
		} catch (Exception ex) {
			Logger.error("Unable to access field '" + field + "'.", ex, InspectorTreeBuilder.class);
			return UNACCESSIBLE;
		}
	}

	/**
	 * Whether the given type is considered a primitive / atimic type.
	 */
	public static boolean isPrimitive(Class<?> type) {
		return type.isPrimitive()
			|| String.class == type
			|| Integer.class == type
			|| Float.class == type
			|| Double.class == type
			|| Long.class == type
			|| Boolean.class == type
			|| Date.class == type
			|| Character.class == type
			|| type.isEnum()
			|| ResPrefix.class.isAssignableFrom(type)
			|| ResKey.class.isAssignableFrom(type)
			|| NamedConstant.class == type
			|| Class.class == type;
	}

	/**
	 * Whether the inpector considers a field as "private".
	 */
	public static boolean isPrivate(Field field) {
		boolean isPublic = Modifier.isPublic(field.getModifiers());
		if (isPublic) {
			return false;
		}
		return field.getAnnotation(Inspectable.class) == null;
	}

	/**
	 * Return a list of fields defined by the given class.
	 * 
	 * @param result
	 *        The list to be filled, must not be <code>null</code>.
	 * @param type
	 *        The class to be inspected, must not be <code>null</code>.
	 */
	private void addAllDeclaredFields(Collection<Field> result, Class<?> type) {
		result.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type != Object.class) {
			addAllDeclaredFields(result, type.getSuperclass());
		}
	}

	static class FieldProperty extends InspectProperty {

		private final Field _field;

		public FieldProperty(Field field) {
			_field = field;
		}

		@Override
		public boolean isStatic() {
			return Modifier.isStatic(_field.getModifiers());
		}

		/**
		 * Note, result must be consistent with callers of
		 * {@link JavaFieldResourceProvider#getTypeIcon(boolean, Class)}
		 * 
		 * @see com.top_logic.layout.inspector.model.nodes.InspectProperty#isPrivate()
		 */
		@Override
		public boolean isPrivate() {
			return InspectorReflectionNode.isPrivate(_field);
		}

		@Override
		public String name() {
			return displayName(_field);
		}

		public static String displayName(Field o1) {
			String name = o1.getName();
			if (name.startsWith("_")) {
				return name.substring(1);
			}
			return name;
		}

		@Override
		public Field getValue() {
			return _field;
		}

		@Override
		public Object staticType() {
			return _field.getType();
		}

	}

}
