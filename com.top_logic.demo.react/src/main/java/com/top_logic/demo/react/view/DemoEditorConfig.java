/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.view;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;

/**
 * Self-contained sample configuration edited by the config-editor demo.
 *
 * <p>
 * Exercises a spread of property kinds (text, number, boolean, enumeration, a nested item and a
 * list of items) so the React configuration editor renders each editor variant.
 * </p>
 */
public interface DemoEditorConfig extends ConfigurationItem {

	/** Configuration name for the value of {@link #getTitle()}. */
	String TITLE = "title";

	/** Configuration name for the value of {@link #getCount()}. */
	String COUNT = "count";

	/** Configuration name for the value of {@link #isEnabled()}. */
	String ENABLED = "enabled";

	/** Configuration name for the value of {@link #getPriority()}. */
	String PRIORITY = "priority";

	/** Configuration name for the value of {@link #getAddress()}. */
	String ADDRESS = "address";

	/** Configuration name for the value of {@link #getItems()}. */
	String ITEMS = "items";

	/**
	 * Priority classifier for {@link DemoEditorConfig#getPriority()}.
	 */
	enum Priority {
		/** Lowest priority. */
		LOW,

		/** Default priority. */
		MEDIUM,

		/** Highest priority. */
		HIGH;
	}

	/**
	 * A free-text title.
	 */
	@Name(TITLE)
	String getTitle();

	/**
	 * A bounded item count.
	 */
	@Name(COUNT)
	int getCount();

	/**
	 * Whether the configuration is active.
	 */
	@Name(ENABLED)
	boolean isEnabled();

	/**
	 * The selected priority.
	 */
	@Name(PRIORITY)
	Priority getPriority();

	/**
	 * A nested address rendered inline as a monomorphic item.
	 */
	@Name(ADDRESS)
	@ItemDefault
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	Address getAddress();

	/**
	 * A list of named items with quantities.
	 */
	@Name(ITEMS)
	@Key(Item.NAME)
	List<Item> getItems();

	/**
	 * A postal address.
	 */
	interface Address extends ConfigurationItem {

		/** Configuration name for the value of {@link #getStreet()}. */
		String STREET = "street";

		/** Configuration name for the value of {@link #getCity()}. */
		String CITY = "city";

		/**
		 * The street including house number.
		 */
		@Name(STREET)
		String getStreet();

		/**
		 * The city.
		 */
		@Name(CITY)
		String getCity();
	}

	/**
	 * A named item with a quantity.
	 */
	interface Item extends ConfigurationItem {

		/** Configuration name for the value of {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for the value of {@link #getQuantity()}. */
		String QUANTITY = "quantity";

		/**
		 * The item name.
		 */
		@Name(NAME)
		String getName();

		/**
		 * The item quantity.
		 */
		@Name(QUANTITY)
		int getQuantity();
	}
}
