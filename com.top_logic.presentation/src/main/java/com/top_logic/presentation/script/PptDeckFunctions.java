/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.xslf.usermodel.XMLSlideShow;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.export.DeckWriter;
import com.top_logic.presentation.model.DescriptionReader;
import com.top_logic.presentation.model.SlideSpec;
import com.top_logic.presentation.model.TextSpec;
import com.top_logic.presentation.model.Value;
import com.top_logic.presentation.profile.TemplateProfile;
import com.top_logic.presentation.template.TemplateAnalysis;

/**
 * TL-Script functions creating PowerPoint presentations in the design of a template.
 *
 * <p>
 * A presentation is described by slides created with <code>pptSlide()</code> and the other
 * <code>ppt</code> builder functions. It is created on the basis of a template, whose slide
 * layouts, backgrounds, logos, fonts and colors it takes over. What the creation needs to know
 * about the template beyond its layouts is its profile, see <code>pptProfile()</code>.
 * </p>
 */
@ScriptPrefix("ppt")
public class PptDeckFunctions extends TLScriptFunctions {

	/**
	 * Creates a PowerPoint presentation in the design of a template.
	 *
	 * <p>
	 * Each slide is created on a slide layout of the template: the layout given in the slide
	 * description, or the layout of the role <code>content</code>. Of the layout's placeholders, the
	 * slide shows only those that get a text: the title, the subtitle, the footer and the
	 * placeholders filled explicitly. Placeholder texts take the style of the template, unless the
	 * description formats them. The content elements are placed at their position; the elements
	 * without position are arranged one below the other in the content area of the layout. Texts
	 * that do not fit into their element are shrunk or cut off as requested by their
	 * <code>overflow</code>. A table that does not fit onto its slide, or a text marked with
	 * <code>split</code>, continues on further slides. The sample slides of the template are not
	 * part of the result.
	 * </p>
	 *
	 * <p>
	 * Example:
	 * </p>
	 *
	 * <pre>
	 * pptDeck($template, [
	 *     pptSlide(layout: "title", title: "Quarterly report", subtitle: "Q3 2026"),
	 *     pptSlide(title: "Figures", content: pptTable([["Region", "Sales"], ["North", 12]], headerRows: 1))
	 *   ],
	 *   footer: "Confidential",
	 *   profile: {"roles": {"risk": "#C00000"}})
	 * </pre>
	 *
	 * @param template
	 *        The PowerPoint template (<code>.pptx</code> or <code>.potx</code>).
	 * @param slides
	 *        The slides, each described by <code>pptSlide()</code> or an equivalent dictionary.
	 * @param footer
	 *        The footer text shown on each slide whose layout has a footer placeholder. A slide
	 *        sets its own footer by filling the footer placeholder in its <code>placeholders</code>.
	 * @param profile
	 *        Entries overriding the template profile as reported by <code>pptProfile()</code>,
	 *        e.g. <code>{"layouts": {"content": 10}}</code> to create content slides on the layout
	 *        with index 10, or <code>{"roles": {"risk": "#C00000"}}</code> to define the color of
	 *        the color role <code>risk</code>. Dictionaries are merged entry by entry.
	 * @param name
	 *        The file name of the result.
	 * @return The presentation as <code>.pptx</code> file.
	 */
	@Label("Create presentation")
	@SideEffectFree
	public static BinaryData deck(
			@Mandatory BinaryDataSource template,
			@Mandatory Object slides,
			Object footer,
			Map<?, ?> profile,
			@StringDefault(DeckWriter.DEFAULT_NAME) String name) {
		List<SlideSpec> slideSpecs = DescriptionReader.readSlides(slides);
		TextSpec footerSpec = DescriptionReader.readText(new Value(footer, PresentationData.FOOTER));
		try (XMLSlideShow slideShow = PptTemplateFunctions.open(template)) {
			return DeckWriter.createDeck(slideShow, slideSpecs, footerSpec, profile, name);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	/**
	 * Describes what the creation of a presentation needs to know about a template beyond its
	 * slide layouts.
	 *
	 * <p>
	 * The profile is derived from the slide layouts of the template and can be adjusted for each
	 * presentation by the <code>profile</code> argument of <code>pptDeck()</code>. It is a
	 * dictionary with the following entries:
	 * </p>
	 *
	 * <ul>
	 * <li><code>layouts</code>: The index of the slide layout for each role: <code>title</code>
	 * (the title slide), <code>content</code> (a slide with title and content), <code>section</code>
	 * (the first slide of a section), <code>closing</code> (the last slide). A role without a
	 * suitable layout has no entry. A slide refers to these layouts by the role name.</li>
	 * <li><code>slideLayouts</code>: For each slide layout by its index: its <code>name</code>, the
	 * index of the placeholder taking the <code>subtitle</code>, the index of the placeholder
	 * taking the <code>footer</code>, the indexes of the placeholders taking the
	 * <code>entries</code> of an agenda slide in their order, and the <code>contentArea</code>
	 * (<code>x</code>, <code>y</code>, <code>width</code>, <code>height</code> in points), in
	 * which the content elements without position are arranged.</li>
	 * <li><code>agendaLayouts</code>: The index or name of the slide layout of an agenda slide by
	 * its number of entries, e.g. <code>{"3": 3, "4": 4}</code>. <code>none</code> removes a derived
	 * entry. An agenda slide with another number of entries lists them on the content layout.</li>
	 * <li><code>roles</code>: The color for each color role name, e.g. <code>topicA</code>,
	 * <code>risk</code> or <code>muted</code>.</li>
	 * <li><code>styles</code>: The shape styles by their names, e.g. <code>card</code>, used by
	 * the building blocks.</li>
	 * <li><code>ratings</code>: The styles of rated table cells by the rating words.</li>
	 * <li><code>gap</code>: The distance in points between the elements of a row or column that
	 * gives none, and between the content elements of a slide arranged one below the other
	 * (default 17).</li>
	 * <li><code>minFontSize</code>: The smallest font size in points to which a text is shrunk to
	 * fit into its element (default 10).</li>
	 * <li><code>continuationSuffix</code>: The text appended to the title of a slide continuing a
	 * table or text of the previous slide, separated by a space. The default is "(cont.)" in the
	 * language of the current user.</li>
	 * </ul>
	 *
	 * <p>
	 * The content layout is a layout with a title and a single large text placeholder, whose area
	 * becomes the content area. A layout without such a placeholder offers the area between title
	 * (or subtitle) and footer. The subtitle is a placeholder directly below the title, the footer
	 * a placeholder at the bottom of the slide. The entry placeholders are the other small text
	 * placeholders, read column by column. A layout with a title, no large placeholder and at least
	 * two entry placeholders of the same size is the agenda layout for their number.
	 * </p>
	 *
	 * @param template
	 *        The PowerPoint template (<code>.pptx</code> or <code>.potx</code>).
	 * @return The dictionary describing the profile of the template.
	 */
	@Label("Template profile")
	@SideEffectFree
	public static Map<String, Object> profile(@Mandatory BinaryDataSource template) {
		try (XMLSlideShow slideShow = PptTemplateFunctions.open(template)) {
			return TemplateProfile.derive(TemplateAnalysis.analyze(slideShow)).toData();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
