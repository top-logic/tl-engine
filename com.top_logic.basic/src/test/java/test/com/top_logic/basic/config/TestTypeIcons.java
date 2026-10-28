/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypeIcons;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TypeIcon;

/**
 * Tests {@link TypeIcons}: the {@link TypeIcon} of a configuration type is inherited along the type
 * hierarchy, the most specific annotated type wins, then the order the types are extended in.
 */
@SuppressWarnings("javadoc")
public class TestTypeIcons extends TestCase {

	public interface Plain extends ConfigurationItem {
		// No icon.
	}

	@TypeIcon("css:base")
	public interface Base extends ConfigurationItem {
		// Annotated base type.
	}

	public interface Inheriting extends Base {
		// Takes the icon of its base type.
	}

	@TypeIcon("css:specific")
	public interface Specific extends Inheriting {
		// Overrides the icon of its base type.
	}

	@TypeIcon("css:other")
	public interface Other extends ConfigurationItem {
		// Annotated type unrelated to Base.
	}

	public interface BaseFirst extends Inheriting, Other {
		// Two unrelated annotated types, Base comes first.
	}

	public interface OtherFirst extends Other, Inheriting {
		// Two unrelated annotated types, Other comes first.
	}

	public interface MostSpecificWins extends Base, Specific {
		// Specific is more specific than Base, although Base comes first.
	}

	public void testNoIcon() {
		assertNull(iconKey(Plain.class));
	}

	public void testOwnIcon() {
		assertEquals("css:base", iconKey(Base.class));
		assertEquals("css:specific", iconKey(Specific.class));
	}

	public void testInherited() {
		assertEquals("css:base", iconKey(Inheriting.class));
	}

	public void testInterfaceOrder() {
		assertEquals("css:base", iconKey(BaseFirst.class));
		assertEquals("css:other", iconKey(OtherFirst.class));
	}

	public void testMostSpecificWins() {
		assertEquals("css:specific", iconKey(MostSpecificWins.class));
	}

	private static String iconKey(Class<? extends ConfigurationItem> type) {
		return TypeIcons.iconKey(TypedConfiguration.getConfigurationDescriptor(type));
	}

}
