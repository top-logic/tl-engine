/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFAutoShape;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideLayout;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPlaceholder;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShapeNonVisual;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.ElementSpec;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.PlaceholderKey;
import com.top_logic.presentation.model.RunSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.model.SlideSpec;
import com.top_logic.presentation.model.TextSpec;
import com.top_logic.presentation.profile.LayoutProfile;
import com.top_logic.presentation.profile.TemplateProfile;
import com.top_logic.presentation.template.PlaceholderInfo;
import com.top_logic.presentation.template.TemplateAnalysis;
import com.top_logic.util.error.TopLogicException;

/**
 * Creates a PowerPoint presentation from a presentation description on the basis of a template.
 *
 * <p>
 * The slides are created on the slide layouts of the template, so that they show the template's
 * backgrounds, logos and text styles. Of the layout's placeholders, a slide gets only those that
 * receive content: the title, the subtitle, the footer and the placeholders filled explicitly.
 * Placeholder text inherits the style of the layout, unless the description formats it. The
 * content elements are arranged by {@link Arrangement} and created by {@link ElementWriter}; an
 * element that does not fit onto its slide continues on further slides.
 * </p>
 *
 * <p>
 * The slides of the template itself are removed. When writing, all parts of the file only these
 * slides used (e.g. their images and notes) are removed as well, see {@link PackagePruner}, and a
 * template (<code>.potx</code>) becomes a presentation (<code>.pptx</code>).
 * </p>
 *
 * <p>
 * Usage: {@link #createDeck(XMLSlideShow, List, TextSpec, Map, String)}, or step by step:
 * {@link #DeckWriter(XMLSlideShow, Map)}, {@link #removeTemplateSlides()},
 * {@link #addSlide(SlideSpec, TextSpec, String)} for each slide, {@link #write(OutputStream)}.
 * </p>
 */
public final class DeckWriter {

	/**
	 * The content type of a PowerPoint presentation.
	 */
	public static final String PPTX_CONTENT_TYPE =
		"application/vnd.openxmlformats-officedocument.presentationml.presentation";

	/**
	 * The file name of a presentation without explicit name.
	 */
	public static final String DEFAULT_NAME = "presentation.pptx";

	private static final Set<String> TITLE_TYPES = Set.of("title", "ctrTitle");

	private final XMLSlideShow _slideShow;

	private final TemplateAnalysis _analysis;

	private final TemplateProfile _profile;

	private final List<XSLFSlideLayout> _layouts;

	private final DrawingWriter _drawing;

	private final TextWriter _text;

	private TemplateProfile _derived;

	/**
	 * Creates a {@link DeckWriter} for the given template.
	 *
	 * @param template
	 *        The template, which is modified to become the presentation.
	 * @param profile
	 *        Profile data overriding the profile derived from the template, see
	 *        {@link TemplateProfile#create(TemplateAnalysis, Map)}, <code>null</code> for none.
	 */
	public DeckWriter(XMLSlideShow template, Map<?, ?> profile) {
		_slideShow = template;
		_analysis = TemplateAnalysis.analyze(template);
		_profile = TemplateProfile.create(_analysis, profile);
		_layouts = TemplateAnalysis.layouts(template);
		_drawing = new DrawingWriter(_profile.roles());
		_text = new TextWriter(_drawing);
	}

	/**
	 * Creates a presentation.
	 *
	 * @param template
	 *        The template, which is modified to become the presentation.
	 * @param slides
	 *        The slides of the presentation.
	 * @param footer
	 *        The footer of all slides whose layout has a footer, <code>null</code> for none.
	 * @param profile
	 *        Profile data overriding the profile derived from the template, <code>null</code> for
	 *        none.
	 * @param name
	 *        The file name of the result.
	 * @return The presentation file.
	 */
	public static BinaryData createDeck(XMLSlideShow template, List<SlideSpec> slides, TextSpec footer,
			Map<?, ?> profile, String name) {
		DeckWriter writer = new DeckWriter(template, profile);
		writer.removeTemplateSlides();
		for (int n = 0; n < slides.size(); n++) {
			writer.addSlide(slides.get(n), footer, PresentationData.SLIDES + "[" + n + "]");
		}
		return writer.toBinaryData(name);
	}

	/**
	 * The analysis of the template.
	 */
	public TemplateAnalysis getAnalysis() {
		return _analysis;
	}

	/**
	 * The profile of the template the presentation is created with.
	 */
	public TemplateProfile getProfile() {
		return _profile;
	}

	/**
	 * Removes all slides of the template.
	 */
	public void removeTemplateSlides() {
		for (int n = _slideShow.getSlides().size() - 1; n >= 0; n--) {
			_slideShow.removeSlide(n);
		}
	}

	/**
	 * Appends a slide.
	 *
	 * <p>
	 * If an element of the slide does not fit, see {@link ElementWriter#write(ElementSpec, int,
	 * Rectangle2D, String)}, its rest is shown on continuation slides following the slide. A
	 * continuation slide repeats the placeholder texts of the slide, its title extended by the
	 * {@link TemplateProfile#continuationSuffix()}, and shows the continued element in the whole
	 * content area. The other content elements and the notes appear only on the first slide.
	 * </p>
	 *
	 * @param spec
	 *        The slide description.
	 * @param footer
	 *        The footer of the presentation, <code>null</code> for none.
	 * @param path
	 *        The path of the slide in the description, for error messages.
	 * @return The created slide, without its continuation slides.
	 */
	public XSLFSlide addSlide(SlideSpec spec, TextSpec footer, String path) {
		int index = _profile.resolve(_analysis, spec.layout(), path + "." + PresentationData.LAYOUT);
		XSLFSlideLayout layout = _layouts.get(index);
		LayoutProfile layoutProfile = layoutProfile(index);
		Rectangle2D area = layoutProfile.contentArea();

		Map<XSLFTextShape, PlaceholderText> texts = placeholderTexts(spec, footer, layout, layoutProfile, path);

		XSLFSlide result = null;
		Continuation continuation = null;
		do {
			XSLFSlide slide = createSlide(layout, texts, continuation != null, path);
			ElementWriter elements = new ElementWriter(slide, _drawing, _text, _profile);
			List<Arrangement.Placed> placed;
			int start;
			if (continuation == null) {
				placed = new Arrangement(_profile.gap(), elements::naturalHeight).arrange(spec.content(), area, path);
				start = 0;
			} else {
				placed = List.of(new Arrangement.Placed(continuation.element(), area, continuation.path()));
				start = continuation.start();
			}

			Continuation next = null;
			for (Arrangement.Placed element : placed) {
				int rest = elements.write(element.element(), start, element.bounds(), element.path());
				if (rest > 0) {
					if (next != null) {
						throw new TopLogicException(
							I18NConstants.ERROR_SEVERAL_CONTINUED__PATH_OTHER.fill(element.path(), next.path()));
					}
					next = new Continuation(element.element(), rest, element.path());
				}
			}

			if (result == null) {
				result = slide;
				if (spec.notes() != null) {
					writeNotes(slide, spec.notes(), path + "." + PresentationData.NOTES);
				}
			}
			if (spec.hidden()) {
				slide.setHidden(true);
			}
			continuation = next;
		} while (continuation != null);
		return result;
	}

	/**
	 * An element whose rest is shown on a continuation slide.
	 *
	 * @param element
	 *        The element.
	 * @param start
	 *        The first table row or paragraph of the rest.
	 * @param path
	 *        The path of the element in the description.
	 */
	private record Continuation(ElementSpec element, int start, String path) {
		// Pure data.
	}

	/**
	 * Creates a slide with the given placeholder texts.
	 *
	 * @param continued
	 *        Whether the slide continues the previous slide, so that its title gets the
	 *        {@link TemplateProfile#continuationSuffix()}.
	 */
	private XSLFSlide createSlide(XSLFSlideLayout layout, Map<XSLFTextShape, PlaceholderText> texts,
			boolean continued, String path) {
		XSLFSlide slide = _slideShow.createSlide(layout);
		// The slide gets only the placeholders that receive content.
		for (XSLFShape shape : new ArrayList<>(slide.getShapes())) {
			slide.removeShape(shape);
		}
		String titlePath = path + "." + PresentationData.TITLE;
		for (Map.Entry<XSLFTextShape, PlaceholderText> entry : texts.entrySet()) {
			XSLFAutoShape placeholder = createPlaceholder(slide, entry.getKey());
			PlaceholderText text = entry.getValue();
			TextSpec spec = continued && text.path().equals(titlePath)
				? withSuffix(text.text(), _profile.continuationSuffix()) : text.text();
			_text.write(placeholder, spec, text.path());
		}
		return slide;
	}

	/**
	 * The given text with the given suffix appended to its last paragraph, separated by a space.
	 */
	private static TextSpec withSuffix(TextSpec text, String suffix) {
		if (suffix.isEmpty()) {
			return text;
		}
		List<ParagraphSpec> paragraphs = new ArrayList<>(text.paragraphs());
		if (paragraphs.isEmpty()) {
			paragraphs.add(ParagraphSpec.plain(suffix));
		} else {
			ParagraphSpec last = paragraphs.get(paragraphs.size() - 1);
			List<RunSpec> runs = new ArrayList<>(last.runs());
			RunStyle style = runs.isEmpty() ? RunStyle.INHERIT : runs.get(runs.size() - 1).style();
			runs.add(new RunSpec(" " + suffix, style, null));
			paragraphs.set(paragraphs.size() - 1, new ParagraphSpec(runs, last.style(), last.align(), last.level(),
				last.bullet(), last.spaceBefore(), last.spaceAfter(), last.lineSpacing()));
		}
		return text.withParagraphs(paragraphs);
	}

	private LayoutProfile layoutProfile(int index) {
		LayoutProfile result = _profile.slideLayout(index);
		if (result == null) {
			if (_derived == null) {
				_derived = TemplateProfile.derive(_analysis);
			}
			result = _derived.slideLayout(index);
		}
		return result;
	}

	/**
	 * A text for a placeholder.
	 *
	 * @param text
	 *        The text.
	 * @param path
	 *        The path of the text in the description.
	 */
	private record PlaceholderText(TextSpec text, String path) {
		// Pure data.
	}

	private Map<XSLFTextShape, PlaceholderText> placeholderTexts(SlideSpec spec, TextSpec footer,
			XSLFSlideLayout layout, LayoutProfile layoutProfile, String path) {
		Map<XSLFTextShape, PlaceholderText> result = new LinkedHashMap<>();
		if (footer != null && layoutProfile.footer() != null) {
			XSLFTextShape placeholder = placeholderByIdx(layout, layoutProfile.footer().longValue());
			if (placeholder != null) {
				result.put(placeholder, new PlaceholderText(footer, PresentationData.FOOTER));
			}
		}
		if (spec.title() != null) {
			String titlePath = path + "." + PresentationData.TITLE;
			XSLFTextShape title = titlePlaceholder(layout);
			if (title == null) {
				throw noPlaceholder(titlePath, PresentationData.TITLE, layout);
			}
			result.put(title, new PlaceholderText(spec.title(), titlePath));
		}
		if (spec.subtitle() != null) {
			String subtitlePath = path + "." + PresentationData.SUBTITLE;
			XSLFTextShape subtitle = layoutProfile.subtitle() == null
				? null : placeholderByIdx(layout, layoutProfile.subtitle().longValue());
			if (subtitle == null) {
				throw noPlaceholder(subtitlePath, PresentationData.SUBTITLE, layout);
			}
			result.put(subtitle, new PlaceholderText(spec.subtitle(), subtitlePath));
		}
		for (Map.Entry<PlaceholderKey, TextSpec> entry : spec.placeholders().entrySet()) {
			PlaceholderKey key = entry.getKey();
			String name = key instanceof PlaceholderKey.Index index
				? Long.toString(index.idx()) : ((PlaceholderKey.Name) key).name();
			String placeholderPath = path + "." + PresentationData.PLACEHOLDERS + "." + name;
			XSLFTextShape placeholder = key instanceof PlaceholderKey.Index index
				? placeholderByIdx(layout, index.idx()) : placeholderByName(layout, name);
			if (placeholder == null) {
				throw noPlaceholder(placeholderPath, name, layout);
			}
			result.put(placeholder, new PlaceholderText(entry.getValue(), placeholderPath));
		}
		return result;
	}

	private static TopLogicException noPlaceholder(String path, String placeholder, XSLFSlideLayout layout) {
		return new TopLogicException(
			I18NConstants.ERROR_NO_PLACEHOLDER__PATH_PLACEHOLDER_LAYOUT.fill(path, placeholder, layout.getName()));
	}

	private static XSLFTextShape titlePlaceholder(XSLFSlideLayout layout) {
		for (XSLFShape shape : layout.getShapes()) {
			if (shape instanceof XSLFTextShape text && TITLE_TYPES.contains(PlaceholderInfo.typeOf(shape))) {
				return text;
			}
		}
		return null;
	}

	private static XSLFTextShape placeholderByIdx(XSLFSlideLayout layout, long idx) {
		for (XSLFShape shape : layout.getShapes()) {
			CTPlaceholder ph = PlaceholderInfo.placeholderOf(shape);
			if (ph != null && shape instanceof XSLFTextShape text && PlaceholderInfo.idxOf(ph) == idx) {
				return text;
			}
		}
		return null;
	}

	private static XSLFTextShape placeholderByName(XSLFSlideLayout layout, String name) {
		for (XSLFShape shape : layout.getShapes()) {
			if (shape instanceof XSLFTextShape text && PlaceholderInfo.placeholderOf(shape) != null
				&& (name.equals(shape.getShapeName()) || name.equals(PlaceholderInfo.typeOf(shape)))) {
				return text;
			}
		}
		return null;
	}

	/**
	 * Creates an empty placeholder on the slide that fills the given layout placeholder.
	 *
	 * <p>
	 * The placeholder has no shape properties of its own, so that it inherits position, size and
	 * style from the layout.
	 * </p>
	 */
	private static XSLFAutoShape createPlaceholder(XSLFSlide slide, XSLFTextShape layoutPlaceholder) {
		XSLFAutoShape result = slide.createAutoShape();
		CTShape xml = (CTShape) result.getXmlObject();
		CTShapeNonVisual nonVisual = xml.getNvSpPr();
		nonVisual.getCNvPr().setName(layoutPlaceholder.getShapeName());
		nonVisual.getCNvSpPr().addNewSpLocks().setNoGrp(true);
		CTPlaceholder ph = (CTPlaceholder) PlaceholderInfo.placeholderOf(layoutPlaceholder).copy();
		if (ph.isSetHasCustomPrompt()) {
			ph.unsetHasCustomPrompt();
		}
		nonVisual.getNvPr().setPh(ph);
		xml.setSpPr(CTShapeProperties.Factory.newInstance());
		TextWriter.addTextBody(xml);
		return result;
	}

	private void writeNotes(XSLFSlide slide, String notes, String path) {
		XSLFNotes notesSlide = _slideShow.getNotesSlide(slide);
		for (XSLFTextShape shape : notesSlide.getPlaceholders()) {
			if (shape.getTextType() == Placeholder.BODY) {
				List<ParagraphSpec> paragraphs = new ArrayList<>();
				for (String line : notes.split("\\r\\n|\\r|\\n", -1)) {
					paragraphs.add(ParagraphSpec.plain(line));
				}
				_text.write(shape, TextSpec.of(paragraphs), path);
				return;
			}
		}
	}

	/**
	 * Writes the presentation.
	 *
	 * <p>
	 * Parts of the template no longer used are left out, a template becomes a presentation.
	 * </p>
	 */
	public void write(OutputStream out) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		_slideShow.write(buffer);
		OPCPackage pkg;
		try {
			pkg = OPCPackage.open(new ByteArrayInputStream(buffer.toByteArray()));
		} catch (InvalidFormatException ex) {
			throw new IOException(ex);
		}
		try {
			PackagePruner.prune(pkg);
			pkg.replaceContentType(XSLFRelation.PRESENTATIONML_TEMPLATE.getContentType(),
				XSLFRelation.MAIN.getContentType());
			pkg.save(out);
		} catch (InvalidFormatException ex) {
			throw new IOException(ex);
		} finally {
			pkg.revert();
		}
	}

	/**
	 * Writes the presentation into a file with content type {@link #PPTX_CONTENT_TYPE}.
	 *
	 * @param name
	 *        The file name, <code>null</code> for {@link #DEFAULT_NAME}.
	 * @see #write(OutputStream)
	 */
	public BinaryData toBinaryData(String name) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try {
			write(buffer);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return BinaryDataFactory.createBinaryData(buffer.toByteArray(), PPTX_CONTENT_TYPE,
			name == null ? DEFAULT_NAME : name);
	}

}
