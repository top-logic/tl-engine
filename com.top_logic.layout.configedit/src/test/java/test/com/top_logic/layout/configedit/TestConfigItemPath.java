/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigItemPath;

/**
 * Tests for {@link ConfigItemPath}, the recorded way from a copy of a configuration's root down to
 * the part that was originally passed to {@link ConfigItemPath#to(ConfigurationItem)}.
 */
public class TestConfigItemPath extends TestCase {

	/** A configuration holding parts by the ITEM, LIST and a plain value property. */
	public interface Outer extends ConfigurationItem {

		/** Property name for {@link #getPart()}. */
		String PART = "part";

		/** Property name for {@link #getParts()}. */
		String PARTS = "parts";

		/** Property name for {@link #getChoices()}. */
		String CHOICES = "choices";

		@Name(PART)
		Part getPart();

		/** @see #getPart() */
		void setPart(Part value);

		@Name(PARTS)
		List<Part> getParts();

		@Name(CHOICES)
		String getChoices();

		/** @see #getChoices() */
		void setChoices(String value);
	}

	/** A {@link ConfigPart} that can be nested inside another {@link Part}. */
	public interface Part extends ConfigPart {

		/** Property name for {@link #getValue()}. */
		String VALUE = "value";

		/** Property name for {@link #getNested()}. */
		String NESTED = "nested";

		@Name(VALUE)
		String getValue();

		/** @see #getValue() */
		void setValue(String value);

		@Name(NESTED)
		Part getNested();

		/** @see #getNested() */
		void setNested(Part value);
	}

	/** An item nothing contains is its own root, reached by an empty way. */
	public void testAnUnattachedItemIsItsOwnRoot() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);

		ConfigItemPath path = ConfigItemPath.to(outer);

		assertSame(outer, path.root());
		assertSame("An empty way leads back to the copy's own root.",
			outer, path.resolveIn(outer));
	}

	/** A part reached through an ITEM property is found again in a copy of the root. */
	public void testAnItemPropertyStepIsReplayed() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		Part part = TypedConfiguration.newConfigItem(Part.class);
		part.setValue("v");
		outer.setPart(part);

		ConfigItemPath path = ConfigItemPath.to(part);
		assertSame(outer, path.root());

		Outer copy = TypedConfiguration.copy(outer);
		ConfigurationItem resolved = path.resolveIn(copy);

		assertNotSame("The way must lead into the copy, not back to the original.", part, resolved);
		assertSame(copy.getPart(), resolved);
		assertEquals("v", ((Part) resolved).getValue());
	}

	/** An entry of a list is found again at its own position. */
	public void testAListStepIsReplayed() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		Part first = TypedConfiguration.newConfigItem(Part.class);
		first.setValue("first");
		Part second = TypedConfiguration.newConfigItem(Part.class);
		second.setValue("second");
		outer.getParts().add(first);
		outer.getParts().add(second);

		ConfigurationItem resolved = ConfigItemPath.to(second).resolveIn(TypedConfiguration.copy(outer));

		assertEquals("second", ((Part) resolved).getValue());
	}

	/** Two levels down works the same way - the steps are replayed in order. */
	public void testANestedPartIsReplayed() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		Part part = TypedConfiguration.newConfigItem(Part.class);
		Part inner = TypedConfiguration.newConfigItem(Part.class);
		inner.setValue("inner");
		part.setNested(inner);
		outer.setPart(part);

		ConfigItemPath path = ConfigItemPath.to(inner);
		assertSame(outer, path.root());

		ConfigurationItem resolved = path.resolveIn(TypedConfiguration.copy(outer));

		assertEquals("inner", ((Part) resolved).getValue());
	}

	/**
	 * The copy of the root really does keep what the edited part can navigate to - the point of
	 * copying the root rather than the part.
	 */
	public void testTheCopiedPartCanStillNavigateUpwards() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		outer.setChoices("a,b,c");
		Part part = TypedConfiguration.newConfigItem(Part.class);
		outer.setPart(part);

		ConfigItemPath path = ConfigItemPath.to(part);
		Outer copy = TypedConfiguration.copy(outer);
		Part copiedPart = (Part) path.resolveIn(copy);

		assertSame("The copied part must be contained by the copied root.", copy, copiedPart.container());
		assertEquals("Navigating upwards out of the edited part must still reach a value.",
			"a,b,c", ((Outer) copiedPart.container()).getChoices());
	}

	/** Suite requiring {@link TypeIndex} for {@link TypedConfiguration}. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigItemPath.class, TypeIndex.Module.INSTANCE));
	}
}
