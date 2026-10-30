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
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigCollectionValue;
import com.top_logic.layout.configedit.ConfigFieldIndex;
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

	/** An element of the edited collection, whose key is mandatory. */
	public interface Item extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		@Mandatory
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

	/** An element of {@link OptionallyKeyedConfig}, whose key is optional. */
	public interface OptionalItem extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		@Name(NAME)
		String getName();

		/** @see #getName() */
		void setName(String value);
	}

	/** A keyed list that accepts anonymous entries. */
	public interface OptionallyKeyedConfig extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(ITEMS)
		@Key(OptionalItem.NAME)
		List<OptionalItem> getItems();
	}

	/** An entry of {@link GroupsConfig} holding a keyed collection of its own. */
	public interface Group extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		/** Property name for {@link #getMembers()}. */
		String MEMBERS = "members";

		@Name(NAME)
		@Mandatory
		String getName();

		/** @see #getName() */
		void setName(String value);

		@Name(MEMBERS)
		@Key(Item.NAME)
		List<Item> getMembers();
	}

	/** A keyed collection whose entries hold keyed collections. */
	public interface GroupsConfig extends ConfigurationItem {

		/** Property name for {@link #getGroups()}. */
		String GROUPS = "groups";

		@Name(GROUPS)
		@Key(Group.NAME)
		List<Group> getGroups();
	}

	/** An item holding a keyed collection, and no {@link com.top_logic.basic.config.container.ConfigPart}. */
	public interface Section extends ConfigurationItem {

		/** Property name for {@link #getItems()}. */
		String ITEMS = "items";

		@Name(ITEMS)
		@Key(Item.NAME)
		List<Item> getItems();
	}

	/** An entry of {@link SectionedGroupsConfig} holding its sections. */
	public interface SectionedGroup extends ConfigurationItem {

		/** Property name for {@link #getName()}. */
		String NAME = "name";

		/** Property name for {@link #getSections()}. */
		String SECTIONS = "sections";

		@Name(NAME)
		@Mandatory
		String getName();

		/** @see #getName() */
		void setName(String value);

		@Name(SECTIONS)
		List<Section> getSections();
	}

	/** A keyed collection whose entries hold entries holding keyed collections. */
	public interface SectionedGroupsConfig extends ConfigurationItem {

		/** Property name for {@link #getGroups()}. */
		String GROUPS = "groups";

		@Name(GROUPS)
		@Key(SectionedGroup.NAME)
		List<SectionedGroup> getGroups();
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

	/**
	 * A key an existing entry already has is complained about while it is being typed, not only when
	 * the entry is confirmed.
	 *
	 * <p>
	 * Waiting for the confirmation makes the user finish an entry before learning that its name was
	 * never available. The complaint costs nothing while typing and is what every other form does.
	 * </p>
	 */
	public void testAKeyTakenByAnExistingEntryIsReportedWhileTyping() {
		Item existing = TypedConfiguration.newConfigItem(Item.class);
		existing.setName("taken");
		_value.add(existing);
		PendingEntry pending = start();

		name(pending, "taken");
		_pending.checkKeys();

		assertEquals(I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE, errorOf(pending).plain());
		assertEquals("Complaining is not committing - the entry stays pending.",
			1, _pending.entries().size());
	}

	/** Two new entries carrying the same key are both told, since neither is more at fault. */
	public void testTwoNewEntriesWithTheSameKeyAreBothReported() {
		PendingEntry first = start();
		PendingEntry second = start();

		name(first, "same");
		name(second, "same");
		_pending.checkKeys();

		assertEquals(I18NConstants.ERROR_DUPLICATE_PENDING_KEY__PROPERTY_VALUE, errorOf(first).plain());
		assertEquals(I18NConstants.ERROR_DUPLICATE_PENDING_KEY__PROPERTY_VALUE, errorOf(second).plain());
	}

	/** Renaming one of them clears the complaint at both. */
	public void testRenamingClearsTheComplaintAtBothEnds() {
		PendingEntry first = start();
		PendingEntry second = start();
		name(first, "same");
		name(second, "same");
		_pending.checkKeys();

		name(second, "other");
		_pending.checkKeys();

		assertNull("The entry that kept its key is no longer in conflict.", errorOf(first));
		assertNull(errorOf(second));
	}

	/** An entry nobody has named yet is not complained about. */
	public void testAnUnnamedEntryIsNotReported() {
		PendingEntry pending = start();

		_pending.checkKeys();

		assertNull("An empty key is unfinished, not wrong.", errorOf(pending));
	}

	/** Confirming one of two clashing entries turns the other's complaint into the taken-key one. */
	public void testConfirmingOneTurnsTheOthersComplaintIntoATakenKey() {
		PendingEntry first = start();
		PendingEntry second = start();
		name(first, "same");
		name(second, "same");
		_pending.checkKeys();

		_pending.confirm(first);

		assertEquals("The key is now held by an entry that really is in the collection.",
			I18NConstants.ERROR_DUPLICATE_KEY__PROPERTY_VALUE, errorOf(second).plain());
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

	/**
	 * Without a key, an entry of a list whose key is optional is confirmed as anonymous entry, any
	 * number of times, and anonymous siblings never make a key clash.
	 */
	public void testConfirmingWithoutAnOptionalKeyAddsAnonymousEntries() {
		OptionallyKeyedConfig config = TypedConfiguration.newConfigItem(OptionallyKeyedConfig.class);
		ConfigCollectionValue value =
			new ConfigCollectionValue(config, config.descriptor().getProperty(OptionallyKeyedConfig.ITEMS));
		ConfigPendingEntries pendingEntries = new ConfigPendingEntries(value, _rendered::add);

		OptionalItem first = TypedConfiguration.newConfigItem(OptionalItem.class);
		OptionalItem second = TypedConfiguration.newConfigItem(OptionalItem.class);
		second.setName("");
		assertNull(pendingEntries.confirm(pendingEntries.start(first)));
		assertNull(pendingEntries.confirm(pendingEntries.start(second)));
		assertEquals(Arrays.asList(first, second), config.getItems());
		assertEquals(0, pendingEntries.entries().size());

		PendingEntry named = pendingEntries.start(TypedConfiguration.newConfigItem(OptionalItem.class));
		ConfigFieldModel keyField = new ConfigFieldModel(named.entry(), value.keyProperty(named.entry()));
		named.setKeyFieldModel(keyField);
		((OptionalItem) named.entry()).setName("named");
		pendingEntries.checkKeys();
		assertNull("Anonymous siblings never clash with a key.", keyField.getError());

		assertNull(pendingEntries.confirm(named));
		assertEquals(3, config.getItems().size());
		assertFalse(value.hasEntryWithKey("other"));
		assertTrue(value.hasEntryWithKey("named"));
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

	/** Confirming all pending entries at once confirms each of them. */
	public void testConfirmPendingConfirmsEveryEntry() {
		ConfigFieldIndex index = new ConfigFieldIndex();
		PendingEntry first = start();
		name(first, "first");
		index.registerPending(first);
		PendingEntry second = start();
		name(second, "second");
		index.registerPending(second);

		assertNull(index.confirmPending());

		assertEquals(Arrays.asList("first", "second"), committedNames());
		assertEquals(0, _pending.entries().size());
	}

	/**
	 * An entry whose confirmation is refused stays pending with its reason, the others are
	 * confirmed anyway.
	 */
	public void testConfirmPendingReportsARefusedEntry() {
		ConfigFieldIndex index = new ConfigFieldIndex();
		PendingEntry unnamed = start();
		index.registerPending(unnamed);
		PendingEntry named = start();
		name(named, "named");
		index.registerPending(named);

		ResKey refusal = index.confirmPending();

		assertEquals(I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY, refusal.plain());
		assertEquals(Arrays.asList("named"), committedNames());
		assertEquals(Arrays.asList(unnamed), _pending.entries());
		assertEquals(I18NConstants.ERROR_VALUE_REQUIRED__PROPERTY, errorOf(unnamed).plain());
	}

	/**
	 * An entry is confirmed only after the pending entries inside it, and stays pending as long as
	 * one of them is refused: confirming it would rebuild the editor around it and drop what is
	 * still pending within.
	 */
	public void testConfirmPendingConfirmsInnerEntriesFirst() {
		GroupsConfig config = TypedConfiguration.newConfigItem(GroupsConfig.class);
		ConfigCollectionValue groups =
			new ConfigCollectionValue(config, config.descriptor().getProperty(GroupsConfig.GROUPS));
		ConfigPendingEntries pendingGroups = new ConfigPendingEntries(groups, any -> {
			// Nothing is displayed.
		});
		Group group = TypedConfiguration.newConfigItem(Group.class);
		group.setName("group");
		PendingEntry pendingGroup = pendingGroups.start(group);

		ConfigCollectionValue members =
			new ConfigCollectionValue(group, group.descriptor().getProperty(Group.MEMBERS));
		ConfigPendingEntries pendingMembers = new ConfigPendingEntries(members, any -> {
			// Nothing is displayed.
		});
		PendingEntry member = pendingMembers.start(TypedConfiguration.newConfigItem(Item.class));

		ConfigFieldIndex index = new ConfigFieldIndex();
		// The outer entry first, as it is rendered before anything inside it.
		index.registerPending(pendingGroup);
		index.registerPending(member);

		assertNotNull("The unnamed member must be refused.", index.confirmPending());
		assertTrue("The group must stay pending while a member inside it is.", config.getGroups().isEmpty());
		assertEquals(Arrays.asList(pendingGroup), pendingGroups.entries());
		assertEquals(Arrays.asList(member), pendingMembers.entries());

		name(member, "member");
		assertNull(index.confirmPending());

		assertEquals(1, config.getGroups().size());
		Group confirmed = config.getGroups().get(0);
		assertSame(group, confirmed);
		assertEquals(1, confirmed.getMembers().size());
		assertEquals("member", confirmed.getMembers().get(0).getName());
	}

	/**
	 * Inner first also where the inner entry is pending below an item already in the outer entry,
	 * which is no {@link com.top_logic.basic.config.container.ConfigPart} and so knows nothing of
	 * what holds it.
	 */
	public void testConfirmPendingConfirmsEntriesBelowAnIntermediateItemFirst() {
		assertFalse("The fixture must not be a ConfigPart.",
			com.top_logic.basic.config.container.ConfigPart.class.isAssignableFrom(Section.class));
		SectionedGroupsConfig config = TypedConfiguration.newConfigItem(SectionedGroupsConfig.class);
		ConfigCollectionValue groups =
			new ConfigCollectionValue(config, config.descriptor().getProperty(SectionedGroupsConfig.GROUPS));
		ConfigPendingEntries pendingGroups = new ConfigPendingEntries(groups, any -> {
			// Nothing is displayed.
		});
		SectionedGroup group = TypedConfiguration.newConfigItem(SectionedGroup.class);
		group.setName("group");
		Section section = TypedConfiguration.newConfigItem(Section.class);
		group.getSections().add(section);
		PendingEntry pendingGroup = pendingGroups.start(group);

		ConfigCollectionValue items =
			new ConfigCollectionValue(section, section.descriptor().getProperty(Section.ITEMS));
		ConfigPendingEntries pendingItems = new ConfigPendingEntries(items, any -> {
			// Nothing is displayed.
		});
		PendingEntry item = pendingItems.start(TypedConfiguration.newConfigItem(Item.class));

		ConfigFieldIndex index = new ConfigFieldIndex();
		// The outer entry first, as it is rendered before anything inside it.
		index.registerPending(pendingGroup);
		index.registerPending(item);

		assertNotNull("The unnamed item must be refused.", index.confirmPending());
		assertTrue("The group must stay pending while an item below it is.", config.getGroups().isEmpty());
		assertEquals(Arrays.asList(pendingGroup), pendingGroups.entries());
		assertEquals(Arrays.asList(item), pendingItems.entries());

		name(item, "item");
		assertNull(index.confirmPending());

		assertEquals(1, config.getGroups().size());
		Section confirmed = config.getGroups().get(0).getSections().get(0);
		assertEquals(1, confirmed.getItems().size());
		assertEquals("item", confirmed.getItems().get(0).getName());
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
