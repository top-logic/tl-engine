/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Objects of the test model {@code test.aiindex} and configurations read from XML, for the tests of
 * this module.
 */
public final class IndexTestModel {

	/** The project type. */
	public static final String PROJECT = "test.aiindex:Project";

	/** The statement type. */
	public static final String STATEMENT = "test.aiindex:Statement";

	/** The material type. */
	public static final String MATERIAL = "test.aiindex:Material";

	/** A type nobody indexes. */
	public static final String UNINDEXED = "test.aiindex:Unindexed";

	/**
	 * The configuration of the statement type: its text and a title script, scoped by its project
	 * and the projects it is related to, with its kind as keyword.
	 */
	public static final String STATEMENT_CONFIG = """
		<type name="test.aiindex:Statement"
			scope="s -> $s.get(`test.aiindex:Statement#project`).singleElement().union($s.get(`test.aiindex:Statement#related`))"
		>
			<text attribute="text"/>
			<text attribute="secret"/>
			<text name="context" script="s -> $s.get(`test.aiindex:Statement#title`)" reads="title"/>
			<keywords>
				<keyword name="kind" script="s -> $s.get(`test.aiindex:Statement#kind`)"/>
			</keywords>
		</type>
		""";

	/** The configuration of the material type: name and extracted text, scoped by its project. */
	public static final String MATERIAL_CONFIG = """
		<type name="test.aiindex:Material" scope="m -> $m.get(`test.aiindex:Material#project`)">
			<text attribute="name"/>
			<text attribute="extractedText"/>
		</type>
		""";

	private IndexTestModel() {
		// Static utility.
	}

	/**
	 * Reads a configuration item of the given type from XML whose root element has the given tag.
	 */
	@SuppressWarnings("unchecked")
	public static <C extends ConfigurationItem> C read(Class<C> type, String tag, String xml) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(type);
		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(tag, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		try {
			return (C) reader.read();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Cannot read: " + xml, ex);
		}
	}

	/**
	 * An indexed type read from XML.
	 */
	public static IndexedTypeConfig type(String xml) {
		return read(IndexedTypeConfig.class, "type", xml);
	}

	/**
	 * A new object of the given type with the given attribute values; call inside a transaction.
	 */
	public static TLObject create(String type, Map<String, Object> values) {
		TLObject object =
			ModelService.getInstance().getFactory().createObject((TLClass) TLModelUtil.findType(type), null);
		for (Map.Entry<String, Object> value : values.entrySet()) {
			object.tUpdate(object.tType().getPart(value.getKey()), value.getValue());
		}
		return object;
	}

	/**
	 * Sets attribute values of an object; call inside a transaction.
	 */
	public static void set(TLObject object, Map<String, Object> values) {
		for (Map.Entry<String, Object> value : values.entrySet()) {
			object.tUpdate(object.tType().getPart(value.getKey()), value.getValue());
		}
	}

	/**
	 * The classifier of the {@code Kind} enumeration with the given name.
	 */
	public static Object kind(String name) {
		return TLModelUtil.findPart("test.aiindex:Kind#" + name);
	}

	/**
	 * Runs the given change in a transaction of its own and commits it.
	 */
	public static <T> T committed(java.util.function.Supplier<T> change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * Deletes the given objects in one transaction.
	 */
	public static void delete(Collection<? extends TLObject> objects) {
		committed(() -> {
			for (TLObject object : objects) {
				object.tDelete();
			}
			return null;
		});
	}

}
