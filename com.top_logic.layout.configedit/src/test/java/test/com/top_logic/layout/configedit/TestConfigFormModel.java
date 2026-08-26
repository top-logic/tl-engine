/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigFormModel;

/**
 * Tests for {@link ConfigFormModel}, the state behind an edit mode: the original, a working copy,
 * and the transitions between them.
 */
public class TestConfigFormModel extends TestCase {

	/** A simple configuration with a single value property. */
	public interface TestConfig extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/** A configuration holding a {@link Part} and a value property the part can navigate to. */
	public interface Outer extends ConfigurationItem {

		/** Property name for {@link #getPart()}. */
		String PART = "part";

		/** Property name for {@link #getChoices()}. */
		String CHOICES = "choices";

		@Name(PART)
		Part getPart();

		/** @see #getPart() */
		void setPart(Part value);

		@Name(CHOICES)
		String getChoices();

		/** @see #getChoices() */
		void setChoices(String value);
	}

	/** A {@link ConfigPart} that can be nested inside an {@link Outer}. */
	public interface Part extends ConfigPart {

		/** Property name for {@link #getValue()}. */
		String VALUE = "value";

		@Name(VALUE)
		String getValue();

		/** @see #getValue() */
		void setValue(String value);
	}

	/** In view mode the editor works on the item itself, so a change is immediate. */
	public void testViewModeEditsTheOriginal() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigFormModel model = new ConfigFormModel(config);

		assertFalse(model.isEditMode());
		assertSame(config, model.edited());
	}

	/** Entering edit mode puts a copy in front of the original. */
	public void testEditModeWorksOnACopy() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		ConfigFormModel model = new ConfigFormModel(config);

		model.startEditing();

		assertTrue(model.isEditMode());
		assertNotSame("The copy must not be the original.", config, model.edited());
		assertEquals("The copy starts out as what the original holds.",
			"before", ((TestConfig) model.edited()).getName());
	}

	/** What is typed into the copy does not reach the original until it is applied. */
	public void testChangesStayInTheCopyUntilApplied() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		ConfigFormModel model = new ConfigFormModel(config);
		model.startEditing();

		((TestConfig) model.edited()).setName("after");

		assertEquals("The original must be untouched while editing.", "before", config.getName());

		model.apply();

		assertEquals("Applying carries the copy's content over.", "after", config.getName());
		assertFalse("Applying leaves edit mode.", model.isEditMode());
		assertSame("Back to the original once applied.", config, model.edited());
	}

	/** Cancelling drops the copy without touching the original. */
	public void testCancellingDiscardsTheCopy() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		ConfigFormModel model = new ConfigFormModel(config);
		model.startEditing();
		((TestConfig) model.edited()).setName("after");

		model.cancelEditing();

		assertEquals("before", config.getName());
		assertFalse(model.isEditMode());
		assertSame(config, model.edited());
	}

	/** A second edit starts from what the original holds now, not from the abandoned copy. */
	public void testASecondEditStartsFresh() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		config.setName("before");
		ConfigFormModel model = new ConfigFormModel(config);
		model.startEditing();
		((TestConfig) model.edited()).setName("abandoned");
		model.cancelEditing();

		model.startEditing();

		assertEquals("before", ((TestConfig) model.edited()).getName());
	}

	/**
	 * What is copied is the root, so the edited part can still navigate out of itself - the reason
	 * this is not simply a copy of the item the caller named.
	 */
	public void testEditingAPartKeepsWhatIsAroundIt() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		outer.setChoices("a,b,c");
		Part part = TypedConfiguration.newConfigItem(Part.class);
		outer.setPart(part);
		ConfigFormModel model = new ConfigFormModel(part);

		model.startEditing();

		Part edited = (Part) model.edited();
		assertNotSame("A copy is edited, not the part itself.", part, edited);
		assertNotNull("The copied part must still be contained.", edited.container());
		assertEquals("Navigating out of the edited part must still find the surrounding values.",
			"a,b,c", ((Outer) edited.container()).getChoices());
	}

	/** Applying writes back into the part that was named, leaving the rest of the tree alone. */
	public void testApplyingAPartWritesBackIntoThatPart() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		Part part = TypedConfiguration.newConfigItem(Part.class);
		part.setValue("before");
		outer.setPart(part);
		ConfigFormModel model = new ConfigFormModel(part);
		model.startEditing();
		((Part) model.edited()).setValue("after");

		model.apply();

		assertEquals("after", part.getValue());
		assertSame("The tree must still hold the very same part object.", part, outer.getPart());
	}

	/** Every mode change notifies, so the control knows to rebuild. */
	public void testEveryModeChangeNotifies() {
		TestConfig config = TypedConfiguration.newConfigItem(TestConfig.class);
		ConfigFormModel model = new ConfigFormModel(config);
		int[] calls = new int[1];
		model.addListener(() -> calls[0]++);

		model.startEditing();
		assertEquals(1, calls[0]);

		model.cancelEditing();
		assertEquals(2, calls[0]);

		model.startEditing();
		model.apply();
		assertEquals(4, calls[0]);
	}

	/** Suite requiring {@link TypeIndex} for {@link TypedConfiguration}. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigFormModel.class, TypeIndex.Module.INSTANCE));
	}
}
