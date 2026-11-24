/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.form.typed.TypedItem;
import test.com.top_logic.layout.view.form.typed.TypedNode;
import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.EditLevel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TypedObjectClasses;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link TLObjectOverlay}s of the view layer implementing the generated Java interfaces
 * of their type, see {@link TypedObjectClasses}.
 *
 * <p>
 * The types of <code>TestViewTypedBuffers.model.xml</code> are bound to the Java interfaces
 * {@link TypedItem} and {@link TypedNode}, the latter below <code>tl.element:StructuredElement</code>.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTypedTLObjectOverlay extends AbstractModelAccessTest {

	private static final String TYPED_MODULE = "TestViewTypedBuffers";

	private static final String TYPED_ITEM = TYPED_MODULE + ":TypedItem";

	private static final String TYPED_NODE = TYPED_MODULE + ":TypedNode";

	private static final String LOCAL_TYPE = "LocalType";

	/**
	 * Types created in the scope of a test object, deleted after the test.
	 */
	private final List<TLClass> _localTypes = new ArrayList<>();

	/**
	 * The tests are about the buffers, not about the access rights.
	 */
	@Override
	protected boolean checksSecurity() {
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		if (!_localTypes.isEmpty()) {
			try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
				for (TLClass localType : _localTypes) {
					localType.tDelete();
				}
				tx.commit();
			}
			_localTypes.clear();
		}
		super.tearDown();
	}

	/**
	 * The buffer of a persistent object implements the Java interface of its type. Its typed
	 * accessors read and write the buffered value, the persistent object is not changed.
	 */
	public void testBufferOfPersistentObject() {
		TLObject object = newObject(TYPED_ITEM, "i1");
		TLObject buffer = new EditLevel().buffer(object);

		assertTrue(buffer instanceof TLObjectOverlay);
		assertTrue(buffer instanceof TypedItem);
		TypedItem item = (TypedItem) buffer;
		assertEquals("i1", item.getName());

		buffer.tUpdateByName(TypedItem.NAME_ATTR, "i2");
		assertEquals("i2", item.getName());
		assertEquals("i1", object.tValueByName(TypedItem.NAME_ATTR));

		item.setName("i3");
		assertEquals("i3", buffer.tValueByName(TypedItem.NAME_ATTR));
		assertEquals("i1", object.tValueByName(TypedItem.NAME_ATTR));

		assertSame(object, ((TLObjectOverlay) buffer).getBase());
		assertEquals(object.tId(), buffer.tId());
		assertSame(object.tHandle(), buffer.tHandle());
	}

	/**
	 * The buffer of a new object implements the Java interface of its type and has no handle.
	 */
	public void testBufferOfNewObject() {
		TLObjectOverlay buffer = new EditLevel().create(typedType(TYPED_ITEM), null);

		assertTrue(buffer instanceof TypedItem);
		TypedItem item = (TypedItem) buffer;
		item.setName("n1");
		assertEquals("n1", buffer.tValueByName(TypedItem.NAME_ATTR));
		assertEquals("n1", item.getName());
		assertNull("The new object is changed through its buffer only.",
			buffer.getBase().tValueByName(TypedItem.NAME_ATTR));

		assertTrue(buffer.tTransient());
		try {
			buffer.tHandle();
			fail("A new object has no handle.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
	}

	/**
	 * The overlay a form edits its object in implements the Java interface of the object's type.
	 */
	public void testFormOverlay() {
		TLObject object = newObject(TYPED_ITEM, "f1");
		FormControl form = new FormControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")), object,
			"no model", NoTokenHandling.INSTANCE);
		form.setModelScope(new RecordingScope());

		assertTrue(form.enterEditMode());
		TLObjectOverlay overlay = form.getOverlay();
		assertTrue(overlay instanceof TypedItem);
		((TypedItem) overlay).setName("f2");
		assertEquals("f2", overlay.tValueByName(TypedItem.NAME_ATTR));
		assertEquals("f1", object.tValueByName(TypedItem.NAME_ATTR));
	}

	/**
	 * The buffer of a persistent object of a type below <code>tl.element:StructuredElement</code>
	 * implements {@link StructuredElement}: The name, the typed accessors and
	 * {@link StructuredElement#getValue(String)}, {@link StructuredElement#setValue(String, Object)}
	 * access the buffer, the handle and the types are those of the persistent object.
	 */
	public void testStructuredElementBuffer() {
		TLObject object = newObject(TYPED_NODE, "n1");
		TLClass localType = localType(object);
		TLObject buffer = new EditLevel().buffer(object);

		assertTrue(buffer instanceof TLObjectOverlay);
		assertTrue(buffer instanceof TypedNode);
		TypedNode node = (TypedNode) buffer;

		assertEquals("n1", node.getName());
		node.setValue(NAME, "n2");
		assertEquals("n2", node.getName());
		assertEquals("n2", node.getValue(NAME));
		assertEquals("n2", buffer.tValueByName(NAME));
		assertEquals("n1", object.tValueByName(NAME));

		node.setLabel("l1");
		assertEquals("l1", buffer.tValueByName(TypedNode.LABEL_ATTR));
		assertNull(object.tValueByName(TypedNode.LABEL_ATTR));

		assertSame(object.tHandle(), node.tHandle());
		assertSame(localType, node.getType(LOCAL_TYPE));
		assertEquals(list(((TLScope) object).getTypes()), list(node.getTypes()));
		assertTrue(node.getTypes().contains(localType));
	}

	/**
	 * The buffer of a new object of a type below <code>tl.element:StructuredElement</code> has
	 * neither handle nor types.
	 */
	public void testStructuredElementNewObject() {
		TLObjectOverlay buffer = new EditLevel().create(typedType(TYPED_NODE), null);

		assertTrue(buffer instanceof TypedNode);
		TypedNode node = (TypedNode) buffer;
		node.setValue(NAME, "c1");
		assertEquals("c1", node.getName());

		try {
			node.tHandle();
			fail("A new object has no handle.");
		} catch (UnsupportedOperationException ex) {
			// Expected.
		}
		assertTrue(node.getTypes().isEmpty());
		assertNull(node.getType(LOCAL_TYPE));
	}

	/**
	 * The {@link StructuredElement} life-cycle accessors of the buffer of a persistent object report
	 * the creation and modification of the persistent object, those of a new object report none.
	 */
	public void testLifecycle() {
		StructuredElement object = (StructuredElement) newObject(TYPED_NODE, "n1");
		StructuredElement buffer = (StructuredElement) new EditLevel().buffer(object);

		assertNotNull(object.getCreated());
		assertEquals(object.getCreated(), buffer.getCreated());
		assertEquals(object.getCreator(), buffer.getCreator());
		assertEquals(object.getModified(), buffer.getModified());
		assertEquals(object.getModifier(), buffer.getModifier());

		StructuredElement newBuffer =
			(StructuredElement) new EditLevel().create(typedType(TYPED_NODE), null);
		assertNull(newBuffer.getCreator());
		assertNull(newBuffer.getCreated());
		assertNull(newBuffer.getModifier());
		assertNull(newBuffer.getModified());
	}

	private static TLClass typedType(String qualifiedName) {
		return (TLClass) TLModelUtil.findType(model(), qualifiedName);
	}

	/**
	 * Creates a type in the scope of the given persistent object.
	 */
	private TLClass localType(TLObject scope) {
		TLModule module = typedType(TYPED_NODE).getModule();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLClass result = TLModelUtil.addClass(module, (TLScope) scope, LOCAL_TYPE);
			tx.commit();
			_localTypes.add(result);
			return result;
		}
	}

	private static List<TLType> list(Iterable<TLType> types) {
		List<TLType> result = new ArrayList<>();
		types.forEach(result::add);
		return result;
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suiteWith(TestTypedTLObjectOverlay.class, TypeIndex.Module.INSTANCE,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
