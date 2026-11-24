/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.typed;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.DefaultFullTextBuffer;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.impl.TypedObjectClasses;

/**
 * Test for {@link TypedObjectClasses} with transient objects created by the
 * {@link TransientObjectFactory}.
 *
 * <p>
 * The test module binds its types to the Java interfaces of this package: the interfaces
 * <code>Named</code> and <code>Coded</code> have the Java interfaces {@link Named} and
 * {@link Coded}, the classes <code>NamedItem</code> and <code>Item</code> inherit from them
 * without a Java interface of their own, <code>Plain</code> has no Java interface at all, and
 * <code>Node</code> below <code>tl.element:StructuredElement</code> has the Java interface
 * {@link Node} extending the framework interface {@link StructuredElement}, and
 * <code>ModelClass</code> is bound to the framework interface {@link TLClass} that a transient
 * object cannot implement.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTypedObjectClasses extends AbstractTypedObjectTest {

	private static final String T_HANDLE = "tHandle";

	/**
	 * A transient object of a type with a Java interface implements it, and its getter and setter
	 * access the value of the object.
	 */
	public void testInterfaceOfGeneralization() {
		TLObject object = newTransient("NamedItem");

		assertTrue(object instanceof TransientTLObjectImpl);
		assertTrue(object instanceof Named);
		Named named = (Named) object;

		named.setName("n1");
		assertEquals("n1", object.tValueByName(Named.NAME_ATTR));

		object.tUpdateByName(Named.NAME_ATTR, "n2");
		assertEquals("n2", named.getName());
	}

	/**
	 * A type with several generalizations implements the Java interfaces of all of them.
	 */
	public void testInterfacesOfAllGeneralizations() {
		TLObject object = newTransient("Item");

		assertTrue(object instanceof Named);
		assertTrue(object instanceof Coded);

		((Coded) object).setCode("c1");
		((Named) object).setName("n1");
		assertEquals("c1", object.tValueByName(Coded.CODE_ATTR));
		assertEquals("n1", object.tValueByName(Named.NAME_ATTR));
	}

	/**
	 * A type without any Java interface uses the base class itself.
	 */
	public void testNoInterface() {
		TLClass plain = type(MODULE + ":Plain");

		assertSame(TransientTLObjectImpl.class, TypedObjectClasses.classFor(TransientTLObjectImpl.class, plain));
		assertSame(TransientTLObjectImpl.class, newTransient("Plain").getClass());
	}

	/**
	 * A transient type without module (e.g. the result type of the script console) has no Java
	 * interface: its objects use the base class itself.
	 */
	public void testTypeWithoutModule() {
		TLClass resultType = TransientModelFactory.createTransientClass(getModel(), "Result");
		assertNull(resultType.getModule());

		assertSame(TransientTLObjectImpl.class,
			TypedObjectClasses.classFor(TransientTLObjectImpl.class, resultType));
		assertSame(TransientTLObjectImpl.class, TransientModelFactory.createTransientObject(resultType).getClass());
	}

	/**
	 * The generated class is created once for the same type.
	 */
	public void testClassCached() {
		TLClass item = type(MODULE + ":Item");

		Class<?> first = TypedObjectClasses.classFor(TransientTLObjectImpl.class, item);
		assertNotSame(TransientTLObjectImpl.class, first);
		assertSame(first, TypedObjectClasses.classFor(TransientTLObjectImpl.class, item));
		assertSame(first, newTransient("Item").getClass());
	}

	/**
	 * Two types with the same Java interfaces share the generated class.
	 */
	public void testClassSharedByInterfaces() {
		assertSame(
			TypedObjectClasses.classFor(TransientTLObjectImpl.class, type(MODULE + ":NamedItem")),
			TypedObjectClasses.classFor(TransientTLObjectImpl.class, type(MODULE + ":Named")));
	}

	/**
	 * A transient object of a type below <code>tl.element:StructuredElement</code> implements
	 * the Java interface of its type and {@link StructuredElement}. Typed accessors and
	 * {@link Wrapper#getValue(String)}, {@link Wrapper#setValue(String, Object)} access the values
	 * of the object.
	 */
	public void testStructuredElement() {
		TLObject object = newTransient("Node");
		assertTrue(object instanceof TransientTLObjectImpl);
		assertTrue(object instanceof Node);
		assertTrue(object instanceof StructuredElement);
		Node node = (Node) object;

		node.setLabel("l1");
		assertEquals("l1", object.tValueByName(Node.LABEL_ATTR));
		assertEquals("l1", node.getValue(Node.LABEL_ATTR));

		node.setValue(Node.LABEL_ATTR, "l2");
		assertEquals("l2", object.tValueByName(Node.LABEL_ATTR));
		assertEquals("l2", node.getLabel());

		node.setElementName("n1");
		assertEquals("n1", object.tValueByName(TLNamed.NAME_ATTRIBUTE));
		assertEquals("n1", node.getName());
	}

	/**
	 * {@link Wrapper#tHandle()} of a transient object fails as {@link TLObject#tHandle()} does,
	 * and the generated class implements it with the narrowed return type.
	 */
	public void testHandle() throws NoSuchMethodException {
		TLObject object = newTransient("Node");
		try {
			((Wrapper) object).tHandle();
			fail("A transient object has no handle.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
		try {
			object.tHandle();
			fail("A transient object has no handle.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}

		assertEquals(KnowledgeObject.class, object.getClass().getMethod(T_HANDLE).getReturnType());
		Method declared = declaredMethods(object.getClass()).get(T_HANDLE + "()");
		assertNotNull(declared);
		assertEquals(KnowledgeObject.class, declared.getReturnType());
	}

	/**
	 * The full text of a transient object contains its name.
	 */
	public void testFullText() {
		Node node = (Node) newTransient("Node");
		node.setElementName("fullTextName");

		DefaultFullTextBuffer buffer = new DefaultFullTextBuffer();
		node.generateFullText(buffer);
		assertEquals("fullTextName", buffer.toString());
	}

	/**
	 * A transient object without persistent counterpart defines no types.
	 */
	public void testScopeOfTransient() {
		Node node = (Node) newTransient("Node");

		assertTrue(node.getTypes().isEmpty());
		assertTrue(node.getClasses().isEmpty());
		assertTrue(node.getAssociations().isEmpty());
		assertTrue(node.getEnumerations().isEmpty());
		assertTrue(node.getDatatypes().isEmpty());
		assertNull(node.getType("x"));
	}

	/**
	 * The single-valued backwards reference <code>parent</code> of a transient child delivers the
	 * transient object referring to it.
	 */
	public void testParentOfTransient() {
		TLObject parent = newTransient("Node");
		TLObject child = newTransient("Node");
		TLObject orphan = newTransient("Node");

		parent.tUpdateByName(StructuredElement.CHILDREN_ATTR, List.of(child));

		assertSame(parent, child.tValueByName(StructuredElement.PARENT_ATTR));
		assertSame(parent, ((StructuredElement) child).getParent());
		assertEquals(List.of(child), ((StructuredElement) parent).getChildren());

		assertNull(orphan.tValueByName(StructuredElement.PARENT_ATTR));
		assertNull(((StructuredElement) orphan).getParent());
	}

	/**
	 * A Java interface whose abstract methods the generated class cannot implement is not added.
	 */
	public void testInterfaceNotImplementable() {
		TLClass modelClass = type(MODULE + ":ModelClass");

		assertSame(TransientTLObjectImpl.class,
			TypedObjectClasses.classFor(TransientTLObjectImpl.class, modelClass));
		TLObject object = newTransient("ModelClass");
		assertFalse(object instanceof TLClass);
		assertSame(modelClass, object.tType());
	}

	/**
	 * The methods of {@link Wrapper}, {@link StructuredElement} and their super interfaces that a
	 * transient object implements by the rules of {@link TypedObjectClasses}, and the default
	 * methods it deliberately inherits.
	 *
	 * <p>
	 * Fails, if a method of these interfaces is added or removed that requires a rule, so that the
	 * rules are adjusted along with the interfaces.
	 * </p>
	 */
	public void testRules() throws NoSuchMethodException {
		Set<String> abstractMethods = new TreeSet<>();
		Set<String> concrete = concreteSignatures(TransientTLObjectImpl.class.getMethods());
		concrete.addAll(concreteSignatures(StructuredElement.class.getMethods()));
		for (Method method : StructuredElement.class.getMethods()) {
			if (Modifier.isAbstract(method.getModifiers()) && !concrete.contains(signature(method))) {
				abstractMethods.add(signature(method));
			}
		}
		assertEquals(new TreeSet<>(List.of(
			"tHandle():KnowledgeObject",
			"getClasses():Collection",
			"getAssociations():Collection",
			"getEnumerations():Collection",
			"getDatatypes():Collection",
			"getTypes():Collection",
			"getType(String):TLType")), abstractMethods);

		Class<?> generated = newTransient("Node").getClass();
		assertEquals(new TreeSet<>(List.of(
			"tHandle()",
			"getValue(String)",
			"setValue(String,Object)",
			"getClasses()",
			"getAssociations()",
			"getEnumerations()",
			"getDatatypes()",
			"getTypes()",
			"getType(String)")), declaredMethods(generated).keySet());

		Method getValue = Wrapper.class.getMethod("getValue", String.class);
		Method setValue = Wrapper.class.getMethod("setValue", String.class, Object.class);
		assertTrue(getValue.isDefault());
		assertTrue(setValue.isDefault());

		Method compareTo = generated.getMethod("compareTo", Wrapper.class);
		Method generateFullText = generated.getMethod("generateFullText", FullTextBuBuffer.class);
		assertTrue(compareTo.isDefault());
		assertTrue(generateFullText.isDefault());
		assertEquals(Wrapper.class, compareTo.getDeclaringClass());
		assertEquals(Wrapper.class, generateFullText.getDeclaringClass());
	}

	private static Set<String> concreteSignatures(Method[] methods) {
		Set<String> result = new HashSet<>();
		for (Method method : methods) {
			int modifiers = method.getModifiers();
			if (!Modifier.isAbstract(modifiers) && !Modifier.isStatic(modifiers)) {
				result.add(signature(method));
			}
		}
		return result;
	}

	private static String signature(Method method) {
		return method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName)
			.collect(Collectors.joining(",")) + "):" + method.getReturnType().getSimpleName();
	}

	/**
	 * The methods declared in the given class that are neither bridges nor synthetic, by name and
	 * parameter types.
	 */
	private static Map<String, Method> declaredMethods(Class<?> type) {
		Map<String, Method> result = new TreeMap<>();
		for (Method method : type.getDeclaredMethods()) {
			if (method.isBridge() || method.isSynthetic()) {
				continue;
			}
			result.put(method.getName() + "(" + Arrays.stream(method.getParameterTypes()).map(Class::getSimpleName)
				.collect(Collectors.joining(",")) + ")", method);
		}
		return result;
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suite(TestTypedObjectClasses.class);
	}

}
