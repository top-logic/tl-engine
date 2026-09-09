/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.form.ConfigFieldControlProvider;

/**
 * Tests for {@link ConfigFieldControlProvider} - a model attribute whose values are configurations,
 * rendered by the configuration editor.
 *
 * <p>
 * Exercises the two decisions the provider makes: that the editor works on a copy, and that the copy
 * reaches the attribute's field when - and only when - something is actually changed.
 * </p>
 */
public class TestConfigFieldControlProvider extends TestCase {

	/** The configuration an attribute's value has. */
	public interface Edited extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue());
	}

	private static Edited item(String name) {
		Edited result = TypedConfiguration.newConfigItem(Edited.class);
		result.setName(name);
		return result;
	}

	/** The name field the editor built for the (single) item it renders. */
	private FieldModel nameField(ReactControl editor) {
		for (ReactControl child : editor.scriptingChildren()) {
			if (child.getModel() instanceof FieldModel field) {
				return field;
			}
			FieldModel found = nameField(child);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * The editor works on a copy: what the model holds is left alone until the form saves.
	 *
	 * <p>
	 * An attribute's value in edit mode is still the base object's own object - editing it in place
	 * would change the model before anyone pressed Save, and Cancel could not take it back.
	 * </p>
	 */
	public void testTheOriginalIsNotEdited() {
		Edited original = item("original");
		FieldModel model = new AbstractFieldModel(original);

		ReactControl editor = ConfigFieldControlProvider.createItemControl(_context, model, Edited.class);
		nameField(editor).setValue("changed");

		assertEquals("The model's own object must not have been touched.", "original", original.getName());
	}

	/** Nothing is handed to the field while the editor is merely being built. */
	public void testBuildingAloneChangesNothing() {
		Edited original = item("original");
		FieldModel model = new AbstractFieldModel(original);

		ConfigFieldControlProvider.createItemControl(_context, model, Edited.class);

		assertSame("An untouched form must not look edited.", original, model.getValue());
		assertFalse(model.isDirty());
	}

	/** A change reaches the field, so the surrounding form has something to save. */
	public void testAChangeReachesTheField() {
		Edited original = item("original");
		FieldModel model = new AbstractFieldModel(original);

		ReactControl editor = ConfigFieldControlProvider.createItemControl(_context, model, Edited.class);
		nameField(editor).setValue("changed");

		assertNotSame("The field must hold the edited copy now.", original, model.getValue());
		assertEquals("changed", ((Edited) model.getValue()).getName());
	}

	/** An attribute with no value yet is edited as a fresh item of its type. */
	public void testAnEmptyAttributeIsEditedAsAFreshItem() {
		FieldModel model = new AbstractFieldModel(null);

		ReactControl editor = ConfigFieldControlProvider.createItemControl(_context, model, Edited.class);
		nameField(editor).setValue("first");

		assertEquals("first", ((Edited) model.getValue()).getName());
	}

	/** A multi-valued attribute is rendered by the list editor, over copies of its elements. */
	public void testTheElementsOfAListAreCopied() {
		Edited original = item("original");
		List<ConfigurationItem> values = new ArrayList<>(List.of(original));
		FieldModel model = new AbstractFieldModel(values);

		ReactControl editor = ConfigFieldControlProvider.createListControl(_context, model, Edited.class, "Items");
		nameField(editor).setValue("changed");

		assertEquals("The model's own element must not have been touched.", "original", original.getName());
		assertEquals("changed", ((Edited) currentValues(model).get(0)).getName());
	}

	/** Building a list editor hands the field nothing either. */
	public void testBuildingAListAloneChangesNothing() {
		List<ConfigurationItem> values = new ArrayList<>(List.of(item("original")));
		FieldModel model = new AbstractFieldModel(values);

		ConfigFieldControlProvider.createListControl(_context, model, Edited.class, "Items");

		assertSame("An untouched form must not look edited.", values, model.getValue());
	}

	private static List<?> currentValues(FieldModel model) {
		return (List<?>) model.getValue();
	}

	/** Suite requiring the services the configuration editor builds its fields with. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFieldControlProvider.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}
}
