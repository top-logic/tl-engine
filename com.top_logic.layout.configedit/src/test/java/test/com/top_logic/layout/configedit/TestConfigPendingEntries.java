/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigCollectionValue;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigPendingEntries;
import com.top_logic.layout.configedit.ConfigPendingEntries.PendingEntry;
import com.top_logic.layout.configedit.I18NConstants;

/**
 * Tests for {@link ConfigPendingEntries} - the entries the editor has created but the edited
 * collection cannot hold yet.
 *
 * <p>
 * Nothing here renders anything: the key field each entry reports its refusals at is built directly
 * as a {@link ConfigFieldModel}, the way the control would hand it over.
 * </p>
 */
public class TestConfigPendingEntries extends TestCase {

	/** An element of the edited collection. */
	public interface Item extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/** A keyed collection, the only kind that has pending entries at all. */
	public interface KeyedConfig extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(ITEMS)
		@Key(Item.NAME)
		List<Item> getItems();
	}

	private KeyedConfig _config;

	private ConfigCollectionValue _value;

	private ConfigPendingEntries _pending;

	/** The items the re-render callback was asked to expand, in call order. */
	private List<ConfigurationItem> _rendered;

	/** The key field model most recently handed to each entry, as a render cycle would. */
	private Map<PendingEntry, ConfigFieldModel> _keyFields;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(KeyedConfig.class);
		_value = new ConfigCollectionValue(_config, _config.descriptor().getProperty(KeyedConfig.ITEMS));
		_rendered = new ArrayList<>();
		_keyFields = new HashMap<>();
		_pending = new ConfigPendingEntries(_value, _rendered::add);
	}

	/** Starts a pending entry and gives it the key field the control would build for it. */
	private PendingEntry start() {
		PendingEntry pending = _pending.start(TypedConfiguration.newConfigItem(Item.class));
		return withKeyField(pending);
	}

	/**
	 * Hands the entry the key field model, as a render cycle does, and remembers it so a test can
	 * read back what a refused confirmation reported there.
	 */
	private PendingEntry withKeyField(PendingEntry pending) {
		ConfigurationItem entry = pending.entry();
		ConfigFieldModel keyField = new ConfigFieldModel(entry, _value.keyProperty(entry));
		pending.setKeyFieldModel(keyField);
		_keyFields.put(pending, keyField);
		return pending;
	}

	private static void name(PendingEntry pending, String key) {
		((Item) pending.entry()).setName(key);
	}

	private List<String> committedNames() {
		List<String> result = new ArrayList<>();
		for (Item item : _config.getItems()) {
			result.add(item.getName());
		}
		return result;
	}

	/** A started entry is held here, not in the collection. */
	public void testAStartedEntryIsNotInTheCollection() {
		start();

		assertEquals(1, _pending.entries().size());
		assertEquals(0, _config.getItems().size());
	}

	/** Several entries may be pending at once - none of them has claimed anything. */
	public void testSeveralEntriesMayBePendingAtOnce() {
		start();
		start();

		assertEquals(2, _pending.entries().size());
		assertEquals(0, _config.getItems().size());
	}

	/** Confirming a named entry moves it into the collection. */
	public void testConfirmingANamedEntryCommitsIt() {
		PendingEntry pending = start();
		name(pending, "first");

		_pending.confirm(pending);

		assertEquals(Arrays.asList("first"), committedNames());
		assertEquals("The entry is no longer pending once it is in the collection.",
			0, _pending.entries().size());
	}

	/** Confirming without a key reports that at the key field and keeps the entry pending. */
	public void testConfirmingWithoutAKeyIsRefusedAtTheField() {
		PendingEntry pending = start();

		_pending.confirm(pending);

		assertEquals(0, _config.getItems().size());
		assertEquals(1, _pending.entries().size());
		assertEquals(I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY, errorOf(pending).plain());
	}

	/** A key another entry already holds is refused the same way. */
	public void testConfirmingATakenKeyIsRefusedAtTheField() {
		PendingEntry first = start();
		name(first, "taken");
		_pending.confirm(first);

		PendingEntry second = start();
		name(second, "taken");
		_pending.confirm(second);

		assertEquals("The clashing entry must stay out of the collection.",
			Arrays.asList("taken"), committedNames());
		assertEquals(1, _pending.entries().size());
		assertEquals(I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE, errorOf(second).plain());
	}

	/**
	 * Two pending entries may hold the same key while neither is confirmed - a key still being
	 * written has been claimed by nobody.
	 */
	public void testTwoPendingEntriesMayHoldTheSameKey() {
		PendingEntry first = start();
		PendingEntry second = start();
		name(first, "same");
		name(second, "same");

		assertEquals("Neither entry is in the collection, so neither key is taken.",
			0, _config.getItems().size());
		assertNull("Holding a key that is not yet claimed is no error.", errorOf(first));
		assertNull(errorOf(second));

		_pending.confirm(first);

		assertEquals(Arrays.asList("same"), committedNames());
		assertNull("The entry that took the key first is not the one at fault.", errorOf(first));
	}

	/** Discarding removes exactly the given entry and never touches the collection. */
	public void testDiscardingRemovesOnlyThatEntry() {
		PendingEntry first = start();
		PendingEntry second = start();

		_pending.discard(first);

		assertEquals(1, _pending.entries().size());
		assertSame(second, _pending.entries().get(0));
		assertEquals("Discarding never touches the collection - the entry was never in it.",
			0, _config.getItems().size());
	}

	/**
	 * A pending entry's item can be replaced, which the collection itself cannot do for it: a
	 * pending entry has no position there to replace.
	 */
	public void testReplacingAPendingEntrysItem() {
		PendingEntry pending = start();
		Item replacement = TypedConfiguration.newConfigItem(Item.class);
		replacement.setName("replaced");

		_pending.replaceEntry(pending, replacement);

		assertSame("The holder keeps its identity, so the closures of the render survive.",
			pending, _pending.entries().get(0));
		assertSame(replacement, pending.entry());

		withKeyField(pending);
		_pending.confirm(pending);

		assertEquals("Confirming takes the replacement, not what the entry started out as.",
			Arrays.asList("replaced"), committedNames());
	}

	/** Every operation that changes what is displayed asks for a re-render. */
	public void testEveryOperationRequestsARerender() {
		PendingEntry pending = start();
		assertEquals("Starting an entry renders it, expanded.",
			Arrays.asList(pending.entry()), _rendered);

		_rendered.clear();
		name(pending, "first");
		_pending.confirm(pending);
		assertEquals("Confirming renders the entry in its new place.",
			Arrays.asList(pending.entry()), _rendered);

		_rendered.clear();
		PendingEntry other = start();
		_rendered.clear();
		_pending.discard(other);
		assertEquals("Discarding renders with nothing to expand.",
			Arrays.asList((ConfigurationItem) null), _rendered);
	}

	/** A refused confirmation reports at the key field the latest render cycle handed over. */
	public void testTheErrorGoesToTheCurrentKeyField() {
		PendingEntry pending = start();
		ConfigFieldModel stale = _keyFields.get(pending);
		ConfigFieldModel current = new ConfigFieldModel(pending.entry(), keyProperty(pending));
		pending.setKeyFieldModel(current);

		_pending.confirm(pending);

		assertNotNull("The field of the current render cycle carries the error.", current.getError());
		assertNull("The field of an earlier render cycle is no longer displayed.", stale.getError());
	}

	private PropertyDescriptor keyProperty(PendingEntry pending) {
		return _value.keyProperty(pending.entry());
	}

	/** The error the entry's current key field carries, or {@code null} if it carries none. */
	private ResKey errorOf(PendingEntry pending) {
		return _keyFields.get(pending).getError();
	}

	/** Suite requiring {@link TypeIndex} and {@link ThreadContextManager} for label resolution. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestConfigPendingEntries.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}
}
