/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test case for {@link ArtifactRef} and {@link Sections}.
 */
public class TestArtifactRef extends TestCase {

	public void testReferencesRoundTripWithSlashesInTheIdAndAVersion() {
		ArtifactRef ref = ArtifactRef.parse(" tl-artifact://gamma/task/4f2a/frame SCR-01.html ");
		assertEquals(new ArtifactRef("gamma", "task", "4f2a/frame SCR-01.html", null), ref);
		ArtifactRef versioned = ArtifactRef.parse("tl-artifact://beta/spec/42@7");
		assertEquals("7", versioned.version());
		assertEquals("tl-artifact://beta/spec/42@7", versioned.toString());
		assertEquals("tl-artifact://a/task/t/answer", ArtifactRef.ofTask("a", "t", "answer").toString());
		for (String bad : new String[] { "http://x/y/z", "tl-artifact://beta", "tl-artifact://beta/spec/", "" }) {
			assertNull(bad, ArtifactRef.parse(bad));
		}
	}

	public void testSectionsOfMarkdownAndJson() {
		String spec = "# Spec\nIntro\n## REQ-001 Prüfmittel\nA\n### Details\nB\n## REQ-002 Kalibrierplan\nC";
		assertEquals(List.of("Spec", "REQ-001 Prüfmittel", "Details", "REQ-002 Kalibrierplan"), Sections.list(spec));
		assertEquals("A heading by part of its title, with its subsections.",
			"## REQ-001 Prüfmittel\nA\n### Details\nB", Sections.extract(spec, "req-001"));
		assertNull(Sections.extract(spec, "REQ-009"));

		String listed = "- ## Prüfmittelnummer\n  Eindeutig.\n- ## Kalibrierstatus\n  Gültig oder gesperrt.";
		assertEquals("Headings a model wrote as list items count too.", List.of("Prüfmittelnummer", "Kalibrierstatus"),
			Sections.list(listed));
		assertEquals("- ## Kalibrierstatus\n  Gültig oder gesperrt.", Sections.extract(listed, "Kalibrierstatus"));

		Map<String, Object> tokens = Json.map("color", Json.map("primary", "#0057b8"), "spacing", List.of(4L, 8L));
		assertEquals(List.of("color", "spacing"), Sections.list(tokens));
		assertEquals("#0057b8", Sections.extract(tokens, "color/primary"));
		assertEquals(8L, Sections.extract(tokens, "spacing/1"));
		assertSame(tokens, Sections.extract(tokens, null));
	}
}
