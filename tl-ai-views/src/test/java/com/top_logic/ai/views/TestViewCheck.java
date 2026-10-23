/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.views.ViewCheck.Finding;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.util.model.ModelService;

/**
 * The check finds what keeps a view from working - in the engine's words, at its line - and lets a
 * working view pass.
 */
public class TestViewCheck extends BasicTestCase {

	private static final String VIEW = ViewFiles.resource("test-check/page.view.xml");

	/** A view the engine reads, builds and renders passes. */
	public void testAWorkingViewPasses() {
		List<Finding> findings = ViewCheck.check(VIEW, """
			<view>
				<panel>
					<title>
						<en>Fine</en>
					</title>
				</panel>
			</view>
			""");

		assertEquals(List.of(), findings);
	}

	/** A menu, as an app bar holds one, needs the window's context menu - which a page nobody sees has too. */
	public void testAMenuRendersWithoutABrowserWindow() {
		List<Finding> findings = ViewCheck.check(VIEW, """
			<view>
				<menu>
					<text>
						<label>
							<en>More</en>
						</label>
					</text>
					<commands>
						<generic-command>
							<label>
								<en>Nothing</en>
							</label>
							<execute-script function="null"/>
						</generic-command>
					</commands>
				</menu>
			</view>
			""");

		assertEquals(List.of(), findings);
	}

	/** A property the element does not have is reported at its line by the engine's reader. */
	public void testAnUnknownPropertyIsFoundAtItsLine() {
		List<Finding> findings = ViewCheck.check(VIEW, """
			<view>
				<panel>
					<title>
						<en>Fine</en>
					</title>
					<no-such-element/>
				</panel>
			</view>
			""");

		assertEquals(findings.toString(), 1, findings.size());
		assertEquals(6, findings.get(0).line());
		assertTrue(findings.get(0).message(), findings.get(0).message().contains("no-such-element"));
	}

	/** TL-Script naming a type the model does not have fails when the view is built. */
	public void testScriptAgainstAnUnknownTypeIsFound() {
		List<Finding> findings = ViewCheck.check(VIEW, """
			<view>
				<channels>
					<channel name="selected"/>
				</channels>
				<table selection="selected" types="tl.accounts:Person">
					<columns>
						<column attribute="name"/>
					</columns>
					<rows>all(`no.such:Type`)</rows>
				</table>
			</view>
			""");

		assertFalse("The type does not exist.", findings.isEmpty());
		assertTrue(findings.toString(), findings.toString().contains("no.such"));
	}

	/** A channel nobody declared is found when the page is built. */
	public void testAnUndeclaredChannelIsFound() {
		List<Finding> findings = ViewCheck.check(VIEW, """
			<view>
				<form input="nowhere">
					<field attribute="name"/>
				</form>
			</view>
			""");

		assertEquals(findings.toString(), 1, findings.size());
		assertTrue(findings.toString(), findings.get(0).message().contains("nowhere"));
	}

	/** Text that is not XML is reported with its line before the engine is asked. */
	public void testBrokenXmlNamesItsLine() {
		List<Finding> findings = ViewCheck.check(VIEW, "<view>\n\t<panel>\n</view>\n");

		assertEquals(findings.toString(), 1, findings.size());
		assertEquals(3, findings.get(0).line());
	}

	/** Every tag a view can name can be described, with its properties. */
	public void testEveryTagIsDescribed() {
		String summary = ViewElements.summary();
		assertTrue(summary, summary.contains("table"));
		for (String line : summary.split("\n")) {
			for (String tag : line.substring(line.indexOf(':') + 1).split(",")) {
				String described = ViewElements.describe(tag.strip());
				assertNotNull(tag, described);
			}
		}
		assertTrue(ViewElements.describe("table").contains("- rows"));
	}

	/**
	 * The suite: a knowledge base with the model, so that TL-Script compiles against it.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(ServiceTestSetup.createSetup(new TestSuite(TestViewCheck.class),
			ModelService.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}
}
