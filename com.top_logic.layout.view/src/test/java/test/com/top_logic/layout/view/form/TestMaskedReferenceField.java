/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import static com.top_logic.model.search.expr.I18NConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;

/**
 * Tests that a reference field masks the elements of its value the current user may not discover,
 * and that the rows of a composition table are filtered to the discoverable ones.
 *
 * <p>
 * A masked element keeps its place in the value, so saving the form writes it back unchanged, but
 * its identity (label, image, color, link) does not reach the client.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestMaskedReferenceField extends AbstractModelAccessTest {

	/** The text a masked element is shown with. */
	private static final String MASKED = "*****";

	/** A category no user but {@link #_root} may discover, sorting before {@link #_category}. */
	private TLObject _hiddenFirst;

	private FormControl _form;

	private AttributeFieldControl _field;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_hiddenFirst = newObject(qualified(CATEGORY), "aHiddenCategory");
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(CATEGORIES, List.of(_category, _hiddenFirst, _hiddenCategory));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_field = null;
		_form = null;
		_hiddenFirst = null;
		super.tearDown();
	}

	/**
	 * An undiscoverable element is described by the placeholder only, without image, color or link,
	 * and nothing identifying it reaches the client.
	 */
	public void testMaskedDescriptor() {
		becomeUser(_responsible);
		createField(_project, CATEGORIES);

		List<Map<String, Object>> descriptors = valueDescriptors();
		assertEquals(3, descriptors.size());
		assertEquals("category", descriptors.get(0).get(DropdownSelectState.Option.LABEL__PROP));
		for (Map<String, Object> masked : descriptors.subList(1, 3)) {
			assertEquals(MASKED, masked.get(DropdownSelectState.Option.LABEL__PROP));
			assertNull(masked.get(DropdownSelectState.Option.IMAGE__PROP));
			assertNull(masked.get(DropdownSelectState.Option.COLOR_ROLE__PROP));
			assertNull(masked.get(DropdownSelectState.Option.LINK__PROP));
			assertNotNull("The masked element keeps an id to be written back by.",
				masked.get(DropdownSelectState.Option.VALUE__PROP));
		}
		String json = _field.getChromeControl().stateAsJSON();
		assertFalse(json, json.contains("hiddenCategory"));
		assertFalse(json, json.contains("aHiddenCategory"));
		assertEquals(MASKED, Resources.getInstance().getString(
			com.top_logic.layout.react.I18NConstants.MASKED_VALUE));
	}

	/**
	 * Masked elements are displayed after the discoverable ones of an unordered reference, so their
	 * position does not disclose their label.
	 */
	public void testMaskedSortedLast() {
		becomeUser(_responsible);
		createField(_project, CATEGORIES);

		List<Map<String, Object>> descriptors = valueDescriptors();
		assertEquals("category", descriptors.get(0).get(DropdownSelectState.Option.LABEL__PROP));
		assertEquals(MASKED, descriptors.get(1).get(DropdownSelectState.Option.LABEL__PROP));
		assertEquals(MASKED, descriptors.get(2).get(DropdownSelectState.Option.LABEL__PROP));
	}

	/**
	 * Removing the discoverable element writes the masked ones back unchanged.
	 */
	public void testMaskedElementsWrittenBack() {
		becomeUser(_responsible);
		createField(_project, CATEGORIES);
		assertTrue(_form.enterEditMode());

		List<String> maskedIds = new ArrayList<>();
		for (Map<String, Object> descriptor : valueDescriptors()) {
			if (MASKED.equals(descriptor.get(DropdownSelectState.Option.LABEL__PROP))) {
				maskedIds.add((String) descriptor.get(DropdownSelectState.Option.VALUE__PROP));
			}
		}
		assertEquals(2, maskedIds.size());
		input().executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, maskedIds));
		_form.executeSave();

		assertEquals(Set.of(_hiddenFirst, _hiddenCategory), categories());
	}

	/**
	 * Removing a masked element is allowed: the user disposes of the whole value.
	 */
	public void testMaskedElementRemoved() {
		becomeUser(_responsible);
		createField(_project, CATEGORIES);
		assertTrue(_form.enterEditMode());

		List<String> kept = new ArrayList<>();
		for (Map<String, Object> descriptor : valueDescriptors().subList(0, 2)) {
			kept.add((String) descriptor.get(DropdownSelectState.Option.VALUE__PROP));
		}
		input().executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, kept));
		_form.executeSave();

		assertEquals(3 - 1, categories().size());
		assertTrue(categories().contains(_category));
	}

	/**
	 * A save adding an object the user may not discover to a reference is refused.
	 */
	public void testUndiscoverableTargetRefusedOnSave() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(CATEGORIES, List.of(_category));
			tx.commit();
		}
		becomeUser(_responsible);
		createField(_project, CATEGORIES);
		assertTrue(_form.enterEditMode());

		_form.getOverlay().tUpdate(part(PROJECT, CATEGORIES), List.of(_category, _hiddenCategory));
		assertRefused(ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE, _form::executeSave);

		assertEquals(Set.of(_category), categories());
	}

	/**
	 * A user bypassing the model access rights sees every element by its label.
	 */
	public void testAdminSeesUnmasked() {
		becomeUser(_root);
		createField(_project, CATEGORIES);

		List<Object> labels = new ArrayList<>();
		for (Map<String, Object> descriptor : valueDescriptors()) {
			labels.add(descriptor.get(DropdownSelectState.Option.LABEL__PROP));
		}
		assertEquals(List.of("aHiddenCategory", "category", "hiddenCategory"), labels);
	}

	/**
	 * The masked check decides by the current user.
	 */
	public void testIsMasked() {
		becomeUser(_responsible);
		assertFalse(ReactDropdownSelectControl.isMasked(_category));
		assertTrue(ReactDropdownSelectControl.isMasked(_hiddenCategory));
		assertFalse(ReactDropdownSelectControl.isMasked("plain value"));
		assertFalse(ReactDropdownSelectControl.isMasked(null));

		becomeUser(_root);
		assertFalse(ReactDropdownSelectControl.isMasked(_hiddenCategory));
	}

	/**
	 * A table cell shows, sorts and filters a masked element by the placeholder.
	 */
	public void testMaskedCellLabel() {
		becomeUser(_responsible);
		assertEquals("category", ColumnProviderService.label(_category));
		assertEquals(MASKED, ColumnProviderService.label(_hiddenCategory));
		String collectionLabel = ColumnProviderService.label(List.of(_category, _hiddenCategory));
		assertFalse(collectionLabel, collectionLabel.contains("hiddenCategory"));
		assertTrue(collectionLabel, collectionLabel.contains(MASKED));
	}

	/**
	 * The rows of a composition table are the parts the user may discover.
	 */
	public void testCompositionRowsFiltered() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(VIEWER, _roleless);
			tx.commit();
		}
		AttributeRowSetBinding binding = new AttributeRowSetBinding(TASKS);
		assertTrue(binding.resolve(_project));

		becomeUser(_responsible);
		assertEquals(List.of(_task), binding.readRows(_project));

		// The viewer of the project may read it, but holds no role on its tasks.
		becomeUser(_roleless);
		assertEquals(List.of(), binding.readRows(_project));
	}

	/**
	 * Writing the rows of a composition table back keeps the parts the user may not discover.
	 */
	public void testHiddenCompositionRowsKept() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(VIEWER, _roleless);
			tx.commit();
		}
		AttributeRowSetBinding binding = new AttributeRowSetBinding(TASKS);
		assertTrue(binding.resolve(_project));

		becomeUser(_roleless);
		TLObjectOverlay owner = new TLObjectOverlay(_project);
		binding.updateMembership(owner, List.of());
		assertEquals(List.of(_task), owner.tValue(part(PROJECT, TASKS)));
		binding.commit(owner, List.of(), List.of());
		assertEquals(List.of(_task), owner.tValue(part(PROJECT, TASKS)));
		assertTrue(_task.tValid());
	}

	private Set<Object> categories() {
		return new HashSet<>((Collection<?>) _project.tValue(part(PROJECT, CATEGORIES)));
	}

	private void createField(TLObject object, String attribute) {
		_form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), object, "no model", NoTokenHandling.INSTANCE);
		ViewChannel input = new DefaultViewChannel("input");
		input.set(object);
		_form.setInputChannel(input);
		_field = new AttributeFieldControl(_form.getReactContext(), _form, _form, attribute, null, false, null);
		_field.createChromeControl();
		_form.attach();
	}

	private ReactControl input() {
		return _field.getInnerControl();
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> valueDescriptors() {
		assertTrue(input() instanceof ReactDropdownSelectControl);
		return (List<Map<String, Object>>) input().scriptingScalarState().get(FieldState.VALUE__PROP);
	}

	/**
	 * The test suite, additionally starting the {@link FieldControlService} building the inputs of
	 * the fields.
	 */
	public static Test suite() {
		return suiteWith(TestMaskedReferenceField.class, TypeIndex.Module.INSTANCE,
			FieldControlService.Module.INSTANCE);
	}
}
