/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.equal.EqualityByValue;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigItemPath;

/**
 * Tests for {@link ConfigItemPath}, the recorded way from a copy of a configuration's root down to
 * the part that was originally passed to {@link ConfigItemPath#to(ConfigurationItem)}.
 */
public class TestConfigItemPath extends TestCase {

	/** A configuration holding parts by the ITEM, LIST, ARRAY, MAP and a plain value property. */
	public interface Outer extends ConfigurationItem {

		/** Property name for {@link #getPart()}. */
		String PART = "part";

		/** Property name for {@link #getParts()}. */
		String PARTS = "parts";

		/** Property name for {@link #getArrayOfParts()}. */
		String ARRAY_OF_PARTS = "arrayOfParts";

		/** Property name for {@link #getMapOfParts()}. */
		String MAP_OF_PARTS = "mapOfParts";

		/** Property name for {@link #getEqualParts()}. */
		String EQUAL_PARTS = "equalParts";

		/** Property name for {@link #getChoices()}. */
		String CHOICES = "choices";

		@Name(PART)
		Part getPart();

		/** @see #getPart() */
		void setPart(Part value);

		@Name(PARTS)
		List<Part> getParts();

		@Name(ARRAY_OF_PARTS)
		Part[] getArrayOfParts();

		/** @see #getArrayOfParts() */
		void setArrayOfParts(Part[] value);

		@Name(MAP_OF_PARTS)
		@Key(Part.VALUE)
		Map<String, Part> getMapOfParts();

		@Name(EQUAL_PARTS)
		List<EqualPart> getEqualParts();

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

	/**
	 * A {@link ConfigPart} with value equality, unlike {@link Part}: two instances with the same
	 * property values are {@link Object#equals(Object) equal}. This is the fixture that pins that
	 * {@link ConfigItemPath} locates a child in its container by identity ({@code ==}), not
	 * equality - two content-equal {@link Part} instances would already be told apart by the
	 * default identity {@code equals}, so an equals-based search would pass every other test here
	 * too.
	 */
	public interface EqualPart extends ConfigPart, EqualityByValue {

		/** Property name for {@link #getValue()}. */
		String VALUE = "value";

		@Name(VALUE)
		String getValue();

		/** @see #getValue() */
		void setValue(String value);
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

	/** An entry of an array is found again at its own position, not merely the first. */
	public void testAnArrayStepIsReplayedAtItsOwnPosition() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		Part first = TypedConfiguration.newConfigItem(Part.class);
		first.setValue("first");
		Part second = TypedConfiguration.newConfigItem(Part.class);
		second.setValue("second");
		outer.setArrayOfParts(new Part[] { first, second });

		ConfigurationItem resolved = ConfigItemPath.to(second).resolveIn(TypedConfiguration.copy(outer));

		assertEquals("second", ((Part) resolved).getValue());
	}

	/** An entry of a map is found again at its own position, not merely the first. */
	public void testAMapStepIsReplayedAtItsOwnPosition() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		Part first = TypedConfiguration.newConfigItem(Part.class);
		first.setValue("first");
		Part second = TypedConfiguration.newConfigItem(Part.class);
		second.setValue("second");
		outer.getMapOfParts().put("first", first);
		outer.getMapOfParts().put("second", second);

		ConfigurationItem resolved = ConfigItemPath.to(second).resolveIn(TypedConfiguration.copy(outer));

		assertEquals("second", ((Part) resolved).getValue());
	}

	/**
	 * Identity, not equality, must drive the search for a child's position among its container's
	 * elements: two content-equal entries would collapse onto the same (wrong) position if the
	 * search used {@code equals} instead of {@code ==}.
	 */
	public void testIdentityNotEqualityDrivesTheSearch() {
		Outer outer = TypedConfiguration.newConfigItem(Outer.class);
		EqualPart first = TypedConfiguration.newConfigItem(EqualPart.class);
		first.setValue("same");
		EqualPart second = TypedConfiguration.newConfigItem(EqualPart.class);
		second.setValue("same");
		assertEquals("Precondition: the two entries must be value-equal.", first, second);
		outer.getEqualParts().add(first);
		outer.getEqualParts().add(second);

		Outer copy = TypedConfiguration.copy(outer);
		ConfigurationItem resolved = ConfigItemPath.to(second).resolveIn(copy);

		assertSame("The way must resolve to the entry at the original position (index 1), found by identity.",
			copy.getEqualParts().get(1), resolved);
		assertNotSame(copy.getEqualParts().get(0), resolved);
	}

	/** Suite requiring {@link TypeIndex} for {@link TypedConfiguration}. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigItemPath.class, TypeIndex.Module.INSTANCE));
	}
}
