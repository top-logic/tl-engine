/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Produces a concrete expression tree from a generic transformation {@link Node}.
 * 
 * @see #materialize(Node)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeMaterializer {

	/**
	 * Factory for creating typed expressions from the result of a transformation of a generic
	 * {@link Expr} tree.
	 */
	public interface Factory {

		/**
		 * Creates a typed expression from the given transformation result.
		 * 
		 * @param type
		 *        The type of expression to create.
		 * @param children
		 *        The typed subexpressions.
		 * @return The typed expression of the given type.
		 */
		Object create(Object type, Object[] children);

	}

	private final FactoryResolver _factoryResolver;

	/**
	 * Creates a {@link TreeMaterializer}.
	 * 
	 * @param factoryType
	 *        The {@link Class} of a static factory for expression nodes.
	 * 
	 * @throws IllegalArgumentException
	 *         iff the factory class contains more than one static method for the same return type.
	 */
	public static TreeMaterializer createTreeMaterializer(Class<?> factoryType) {
		return new TreeMaterializer(FactoryResolver.getInstance(buildFactoryMap(factoryType)));
	}

	/**
	 * Creates a {@link TreeMaterializer}.
	 *
	 * @param factoryMap
	 *        {@link Factory} for node type map.
	 */
	public TreeMaterializer(FactoryResolver factoryMap) {
		_factoryResolver = factoryMap;
	}

	/**
	 * Creates a {@link Factory} map for the given static factory method class.
	 *
	 * @param factoryType
	 *        The class with factory methods.
	 * @return A {@link Factory} map for {@link #TreeMaterializer(FactoryResolver)}.
	 */
	public static Map<Object, Factory> buildFactoryMap(Class<?> factoryType) {
		Map<Object, Factory> factories = new HashMap<>();
		for (Method method : factoryType.getMethods()) {
			if (!Modifier.isStatic(method.getModifiers())) {
				continue;
			}
			Class<?> type = method.getReturnType();
			Factory factory = factory(method);
			Factory clash = factories.put(type, factory);
			if (clash != null) {
				throw new IllegalArgumentException("More than one static methods with same return type '"
					+ type.getName() + "': '" + clash + "' '" + method + "'");
			}
		}
		return factories;
	}

	private static Factory factory(final Method method) {
		final Class<?>[] parameters = method.getParameterTypes();
		if (parameters.length > 0) {
			final int paramCnt = parameters.length;
			final int lastIndex = paramCnt - 1;
			final Class<?> lastParam = parameters[lastIndex];
			final Class<?> varargComponent = lastParam.getComponentType();
			if (lastParam.isArray() && !varargComponent.isPrimitive()) {
				// Assume the last parameter is a varargs one.
				return new Factory() {
					@Override
					public Object create(Object type, Object[] children) {
						try {
							int varargLength = children.length - lastIndex;
							Object varargs = java.lang.reflect.Array.newInstance(varargComponent, varargLength);
							System.arraycopy(children, lastIndex, varargs, 0, varargLength);
							Object[] arguments = new Object[paramCnt];
							System.arraycopy(children, 0, arguments, 0, lastIndex);
							arguments[lastIndex] = varargs;

							return method.invoke(null, arguments);
						} catch (IllegalAccessException ex) {
							throw new IllegalArgumentException("Invalid model.", ex);
						} catch (InvocationTargetException ex) {
							throw new IllegalArgumentException("Invalid model.", ex);
						}
					}

					@Override
					public String toString() {
						return method.toString();
					}
				};
			}
		}
		return new Factory() {
			@Override
			public Object create(Object type, Object[] children) {
				try {
					return method.invoke(null, children);
				} catch (IllegalAccessException ex) {
					throw new IllegalArgumentException("Invalid model.", ex);
				} catch (InvocationTargetException ex) {
					throw new IllegalArgumentException("Invalid model.", ex);
				}
			}

			@Override
			public String toString() {
				return method.toString();
			}
		};
	}

	/**
	 * Instantiates the given generic {@link Node}.
	 * 
	 * @param root
	 *        The {@link Node} to materialize.
	 * @return The concrete expression value.
	 */
	public Object materialize(Node root) {
		return transformNode(root);
	}

	private Object transformNode(Node child) {
		Object value;
		if (child instanceof Expr) {
			value = transformExpr((Expr) child);
		} else if (child == null) {
			value = null;
		} else {
			value = value(child);
		}
		return value;
	}

	private Object transformExpr(Expr root) {
		Object type = root.getType();
		Object[] children = transformChildren(root);
		return create(type, children);
	}

	private Object create(Object type, Object[] children) {
		Factory factory = _factoryResolver.getFactory(type);
		if (factory == null) {
			throw new IllegalArgumentException("There is no factory for type '" + type + "'.");
		}
		try {
			return factory.create(type, children);
		} catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("Wrong parameter in factory  '" + factory + "', arguments: "
				+ types(children), ex);
		}
	}

	private static List<Class<?>> types(Object[] children) {
		Class<?>[] result = new Class<?>[children.length];
		for (int n = 0, cnt = children.length; n < cnt; n++) {
			Object child = children[n];
			if (child != null) {
				result[n] = child.getClass();
			}
		}
		return Arrays.asList(result);
	}

	private Object[] transformChildren(Expr root) {
		int cnt = root.getSize();
		Object[] result = new Object[cnt];
		for (int n = 0; n < cnt; n++) {
			Node child = root.getChild(n);
			Object value = transformNode(child);
			result[n] = value;
		}
		return result;
	}

	private static Object value(Node child) {
		return ((LiteralValue) child).getValue();
	}

}
