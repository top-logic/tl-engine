/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.config;

import static com.top_logic.tl.ai.index.config.IndexTestModel.*;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.index.config.IndexedType.PendingChunk;
import com.top_logic.tl.ai.index.text.Chunker;
import com.top_logic.util.model.ModelService;

/**
 * What the configuration of a type makes of an object: texts by attribute and by script, the scope,
 * the keywords, the type names and the label in front of what is embedded.
 */
public class TestIndexedType extends BasicTestCase {

	private static final Chunker CHUNKER;

	static {
		ChunkingConfig chunking = TypedConfiguration.newConfigItem(ChunkingConfig.class);
		chunking.update(chunking.descriptor().getProperty(ChunkingConfig.PAGE_MARKER), "^\\[p\\.(\\d+)\\]$");
		chunking.update(chunking.descriptor().getProperty(ChunkingConfig.MAX_CHARS), 200);
		CHUNKER = ChunkingConfig.chunker(chunking);
	}

	/** A statement: its text parts, its project and related projects as scopes, its kind. */
	public void testStatement() {
		IndexedType type = new IndexedType(type(STATEMENT_CONFIG));
		TLObject[] objects = committed(() -> {
			TLObject project = create(PROJECT, Map.of("name", "Pump"));
			TLObject other = create(PROJECT, Map.of("name", "Valve"));
			TLObject statement = create(STATEMENT, Map.of("title", "Base plate", "text",
				"The base plate is made of anodised aluminium.", "kind", kind("REQUIREMENT"), "project", project,
				"related", List.of(other)));
			return new TLObject[] { project, other, statement };
		});
		TLObject statement = objects[2];

		IndexedType.Content content = type.content(statement, CHUNKER, 10_000);

		assertEquals(List.of(ReadFilter.idOf(objects[0]), ReadFilter.idOf(objects[1])), content.scopes());
		assertEquals(Map.of("kind", List.of("REQUIREMENT")), content.keywords());
		assertTrue(content.types().contains(STATEMENT));
		assertEquals("No chunk for an empty attribute.", 2, content.chunks().size());
		PendingChunk text = content.chunks().get(0);
		assertEquals("text", text.part());
		assertEquals("The base plate is made of anodised aluminium.", text.chunk().text());
		String label = MetaLabelProvider.INSTANCE.getLabel(statement);
		assertEquals("The label is in front of what is embedded.", label + "\n\n" + text.chunk().text(),
			text.embeddingText());
		PendingChunk context = content.chunks().get(1);
		assertEquals("context", context.part());
		assertEquals("Base plate", context.chunk().text());
		assertEquals(List.of("title"), type.part("context").reads());
		assertEquals(List.of("text"), type.part("text").reads());
	}

	/** A material's extracted text is chunked by page, and the scope is its project. */
	public void testMaterialPages() {
		IndexedType type = new IndexedType(type(MATERIAL_CONFIG));
		TLObject material = committed(() -> create(MATERIAL, Map.of("name", "Spec",
			"extractedText", "[p.1]\nFirst page text.\n[p.2]\nSecond page text.",
			"project", create(PROJECT, Map.of("name", "P")))));

		IndexedType.Content content = type.content(material, CHUNKER, 10_000);

		assertEquals(3, content.chunks().size());
		assertEquals("Spec", content.chunks().get(0).chunk().text());
		assertEquals(0, content.chunks().get(0).chunk().locator().page());
		assertEquals("Second page text.", content.chunks().get(2).chunk().text());
		assertEquals(2, content.chunks().get(2).chunk().locator().page());
		assertEquals(1, content.scopes().size());
	}

	/** The configuration of a type also applies to objects whose type has none, but not to others. */
	public void testResolution() {
		IndexedTypes types = new IndexedTypes(List.of(type(STATEMENT_CONFIG), type(MATERIAL_CONFIG)));
		TLObject unindexed = committed(() -> create(UNINDEXED, Map.of("name", "x")));
		assertNull(types.forObject(unindexed));
		assertEquals(2, types.concreteClasses().size());
		assertEquals(List.of(STATEMENT, MATERIAL), types.names());
	}

	/** A configuration that names an attribute the type does not have is refused at start. */
	public void testUnknownAttributeIsRefused() {
		try {
			new IndexedType(type("<type name=\"test.aiindex:Statement\"><text attribute=\"nope\"/></type>"));
			fail("An unknown attribute must be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("'nope'"));
		}
		try {
			new IndexedType(type("<type name=\"test.aiindex:Statement\"><text script=\"s -> 'x'\"/></type>"));
			fail("A script text without a name must be refused.");
		} catch (IllegalArgumentException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("needs a name"));
		}
	}

	/** The model; no other service is needed to compute what an object contributes. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(new TestSuite(TestIndexedType.class), ModelService.Module.INSTANCE));
	}

}
