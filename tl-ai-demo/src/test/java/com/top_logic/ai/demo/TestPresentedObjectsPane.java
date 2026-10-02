/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatRailElement;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.model.util.TLModelPartRef;

/**
 * What the chat page shows for the objects an agent <code>present</code>s (Issue #493).
 *
 * <p>
 * The agent names objects and the page decides how they look, so what the page decided is a
 * statement this application makes, not an implementation detail: several presented objects are a
 * <b>table</b> of their attributes - the name, start date, free seats and balance of each event - and
 * not a list of labels, which is what the rail's pane used to hold and what made the pane say less about the
 * objects than the answer beside it did.
 * </p>
 *
 * <p>
 * The test reads the shipped view file rather than a fixture, so it also fails when the
 * configuration stops parsing.
 * </p>
 */
public class TestPresentedObjectsPane extends TestCase {

	/** The chat page of this demo. */
	private static final String VIEW = "/WEB-INF/views/tl-ai-demo/chat-grouped.view.xml";

	/** The type the pane is about. */
	private static final String EVENT = "demo.events:Event";

	/**
	 * Several presented objects are shown as a table of their attributes.
	 */
	public void testCollectionIsATable() throws Exception {
		TableElement.Config table = presentedTable();

		assertEquals("The columns say how each event is doing.",
			List.of("name", "startDate", "remainingCapacity", "balance"), attributes(table));
		assertEquals("Naming the row type gives the columns their model labels - and is what lets"
			+ " this table be built at all, while the pane channel is still empty.",
			List.of(EVENT), qualifiedNames(table.getTypes()));
		assertEquals("The rows are what stands on the channel the <present> tool writes.",
			List.of("paneItems"), channelNames(table));
		assertNotNull("The rows are computed from that channel.", table.getRows());
	}

	/**
	 * A row click opens that object: the table writes it to the channel the pane's
	 * {@code <form>} reads, which is what turns the table into the record beside it.
	 */
	public void testRowClickOpensTheObject() throws Exception {
		TableElement.Config table = presentedTable();

		assertNotNull("A row click has to lead somewhere.", table.getSelection());
		assertEquals("paneObject", table.getSelection().getChannelName());

		ChatRailElement.Config rail = rail();
		assertEquals("One object is a form.", 1, rail.getPane().size());
		assertTrue("... and it is the <form> the clicked row lands in.",
			rail.getPane().get(0) instanceof FormElement.Config);
	}

	/** The one table the rail's {@code <list-pane>} holds. */
	private TableElement.Config presentedTable() throws Exception {
		List<PolymorphicConfiguration<? extends UIElement>> listPane = rail().getListPane();

		assertEquals("The pane for a collection holds exactly one element.", 1, listPane.size());
		PolymorphicConfiguration<? extends UIElement> content = listPane.get(0);
		assertTrue("Several presented objects are a <table>, not a list of labels, but were: "
			+ content.descriptor().getConfigurationInterface().getName(),
			content instanceof TableElement.Config);
		return (TableElement.Config) content;
	}

	/** The {@code <chat-rail>} of the demo's chat page. */
	private ChatRailElement.Config rail() throws Exception {
		ChatRailElement.Config rail = search(view(), ChatRailElement.Config.class);
		assertNotNull("The chat page has a rail.", rail);
		return rail;
	}

	/** The shipped view file, parsed. */
	private static ViewElement.Config view() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPresentedObjectsPane.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = FileManager.getInstance().getData(VIEW);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		return config;
	}

	private static List<String> attributes(TableElement.Config table) {
		assertNotNull("The table names its columns.", table.getColumns());
		List<String> result = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ColumnDeclaration> column : table.getColumns().getColumns()) {
			assertTrue("An attribute column: " + column, column instanceof AttributeColumn.Config);
			result.add(((AttributeColumn.Config) column).getAttribute());
		}
		return result;
	}

	private static List<String> qualifiedNames(List<TLModelPartRef> refs) {
		List<String> result = new ArrayList<>();
		for (TLModelPartRef ref : refs) {
			result.add(ref.qualifiedName());
		}
		return result;
	}

	private static List<String> channelNames(TableElement.Config table) {
		List<String> result = new ArrayList<>();
		for (ChannelRef ref : table.getInputs()) {
			result.add(ref.getChannelName());
		}
		return result;
	}

	/**
	 * The first configuration of the given type in the tree below the given one.
	 *
	 * <p>
	 * Only the properties that hold configurations are followed ({@link PropertyKind#ITEM},
	 * {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY}, {@link PropertyKind#MAP}); a
	 * {@link PropertyKind#REF reference} would lead out of the tree and a
	 * {@link PropertyKind#DERIVED derived} value is computed rather than written.
	 * </p>
	 */
	private static <T extends ConfigurationItem> T search(ConfigurationItem root, Class<T> type) {
		if (type.isInstance(root)) {
			return type.cast(root);
		}
		for (PropertyDescriptor property : root.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM:
				case LIST:
				case ARRAY:
				case MAP: {
					T found = searchIn(root.value(property), type);
					if (found != null) {
						return found;
					}
					break;
				}
				default:
					break;
			}
		}
		return null;
	}

	private static <T extends ConfigurationItem> T searchIn(Object value, Class<T> type) {
		if (value instanceof ConfigurationItem item) {
			return search(item, type);
		}
		if (value instanceof Collection<?> collection) {
			for (Object entry : collection) {
				T found = searchIn(entry, type);
				if (found != null) {
					return found;
				}
			}
		}
		if (value instanceof Map<?, ?> map) {
			for (Object entry : map.values()) {
				T found = searchIn(entry, type);
				if (found != null) {
					return found;
				}
			}
		}
		if (value instanceof Object[] array) {
			for (Object entry : array) {
				T found = searchIn(entry, type);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * All this test needs is the {@link FileManager} over the web application and the
	 * {@code TypeIndex} that typed configuration resolves the nested element tags through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestPresentedObjectsPane.class);
	}

}
