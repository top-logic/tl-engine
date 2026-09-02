/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.view;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;

/**
 * Self-contained sample configuration edited by the config-editor demo.
 *
 * <p>
 * Exercises a spread of property kinds (text, number, boolean, enumeration, a nested item, a list
 * of items, an array of items, a map of items, a list of plain strings written as one comma
 * separated text, a duration, and a {@link ResKey}) so the React configuration editor renders each
 * editor variant.
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

	/** Configuration name for the value of {@link #getSummary()}. */
	String SUMMARY = "summary";

	/** Configuration name for the value of {@link #getShippingAddresses()}. */
	String SHIPPING_ADDRESSES = "shippingAddresses";

	/** Configuration name for the value of {@link #getCatalog()}. */
	String CATALOG = "catalog";

	/** Configuration name for the value of {@link #getTags()}. */
	String TAGS = "tags";

	/** Configuration name for the value of {@link #getTimeout()}. */
	String TIMEOUT = "timeout";

	/** Configuration name for the value of {@link #getOwner()}. */
	String OWNER = "owner";

	/** Configuration name for the value of {@link #getIcon()}. */
	String ICON = "icon";

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
	 * A localized one-line summary, edited as text through the {@link ResKey} format.
	 */
	@Name(SUMMARY)
	ResKey getSummary();

	/**
	 * The addresses to ship to, an array of {@link Address} rather than a {@link List}.
	 */
	@Name(SHIPPING_ADDRESSES)
	Address[] getShippingAddresses();

	/**
	 * The items on offer, indexed by name, a {@link Map} rather than the {@link List} of
	 * {@link #getItems()}.
	 */
	@Name(CATALOG)
	@Key(Item.NAME)
	Map<String, Item> getCatalog();

	/**
	 * Keywords describing this configuration, written as a single comma separated text.
	 *
	 * <p>
	 * A {@link List} of plain values with a {@link Format} of its own is not a collection of
	 * entries to be edited one by one - the format turns the whole list into one text and back, so
	 * it is a single input, not the collection editor {@link #getItems()} gets. That is what
	 * {@link CommaSeparatedStrings} does here: the property is edited as {@code "red, green,
	 * blue"}.
	 * </p>
	 */
	@Name(TAGS)
	@Format(CommaSeparatedStrings.class)
	List<String> getTags();

	/**
	 * How long to wait before giving up, written as a duration such as {@code "5h 10min"} or
	 * {@code "22ms"}.
	 *
	 * <p>
	 * The value is a plain millisecond count; {@link MillisFormat} is what turns it into something
	 * readable and back. Unlike every other format in this configuration, this one can <em>reject</em>
	 * what is typed - an unknown unit or a text that is no duration at all - which is what makes it
	 * the demo's example of a format error being reported at the field.
	 * </p>
	 */
	@Name(TIMEOUT)
	@Format(MillisFormat.class)
	@LongDefault(0L)
	long getTimeout();

	/**
	 * Who is responsible - mandatory, so applying an empty one is refused and the demo has a
	 * violation to show.
	 */
	@Name(OWNER)
	@Mandatory
	String getOwner();

	/**
	 * The icon standing for this configuration - picked from the theme's icons, the way an icon
	 * model attribute is, rather than typed as its encoded form.
	 */
	@Name(ICON)
	ThemeImage getIcon();

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
