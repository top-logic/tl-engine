/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.CssTagAttributeTemplate;
import com.top_logic.html.template.EmptyTemplate;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.StartTagTemplate;
import com.top_logic.html.template.TagAttributeTemplate;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.html.template.TemplateSequence;
import com.top_logic.html.template.expr.LiteralText;
import com.top_logic.html.template.expr.StringLiteral;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.layout.BoxLayout;
import com.top_logic.layout.form.boxes.layout.HorizontalLayout;
import com.top_logic.layout.form.boxes.layout.VerticalLayout;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.internal.TemplateAnnotation;
import com.top_logic.layout.form.template.model.internal.TemplateControlProvider;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * Factory for {@link HTMLTemplateFragment}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Templates {

	/**
	 * Creates a {@link HTMLConstants#DIV} template.
	 */
	public static TagTemplate div(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.DIV, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#DIV} template.
	 */
	public static TagTemplate div(List<? extends HTMLTemplateFragment> contents) {
		return div(toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#TABLE} template.
	 */
	public static TagTemplate table(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.TABLE, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#TABLE} template.
	 */
	public static TagTemplate table(List<? extends HTMLTemplateFragment> contents) {
		return table(toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#THEAD} template.
	 */
	public static TagTemplate thead(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.THEAD, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#TBODY} template.
	 */
	public static TagTemplate tbody(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.TBODY, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#TR} template.
	 */
	public static TagTemplate tr(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.TR, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#TR} template.
	 */
	public static TagTemplate tr(List<? extends HTMLTemplateFragment> contents) {
		return tr(toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#TD} template.
	 */
	public static TagTemplate th(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.TH, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#TH} template.
	 */
	public static TagTemplate th(List<? extends HTMLTemplateFragment> contents) {
		return th(toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#TD} template.
	 */
	public static TagTemplate td(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.TD, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#TD} template.
	 */
	public static TagTemplate td(List<? extends HTMLTemplateFragment> contents) {
		return td(toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#SPAN} template.
	 */
	public static TagTemplate span(HTMLTemplateFragment... contents) {
		return tag(HTMLConstants.SPAN, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#SPAN} template.
	 */
	public static TagTemplate span(List<? extends HTMLTemplateFragment> contents) {
		return span(toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#BR} template.
	 */
	public static TagTemplate br(AttributeTemplate... contents) {
		return tag(HTMLConstants.BR, contents);
	}

	/**
	 * Creates a {@link HTMLConstants#BR} template.
	 */
	public static TagTemplate br(List<? extends AttributeTemplate> contents) {
		return br(toAttributeArray(contents));
	}

	/**
	 * Creates an {@link Image} template.
	 */
	public static Image img(ThemeImage key, AttributeTemplate... contents) {
		return new Image(key, fragment(contents));
	}

	/**
	 * Creates an {@link Image} template.
	 */
	public static Image img(ThemeImage key, List<? extends AttributeTemplate> contents) {
		return img(key, toAttributeArray(contents));
	}

	/**
	 * Creates generic {@link TagTemplate} template.
	 */
	public static TagTemplate tag(final String name, final HTMLTemplateFragment... contents) {
		StartTagTemplate start = new StartTagTemplate(0, 0, name);
		HTMLTemplateFragment singleContent = null;
		List<HTMLTemplateFragment> concatenatedContent = null;
		for (HTMLTemplateFragment element : contents) {
			if (element instanceof TagAttributeTemplate) {
				start.addAttribute((TagAttributeTemplate) element);
			} else {
				if (concatenatedContent != null) {
					concatenatedContent.add(element);
				} else if (singleContent != null) {
					concatenatedContent = new ArrayList<>();
					concatenatedContent.add(singleContent);
					concatenatedContent.add(element);
					singleContent = null;
				} else {
					singleContent = element;
				}
			}
		}
		HTMLTemplateFragment content =
			concatenatedContent != null ? new TemplateSequence(concatenatedContent)
			: (singleContent != null ? singleContent : EmptyTemplate.INSTANCE);
		TagTemplate result = new TagTemplate(start, content);
		return result;
	}

	/**
	 * Creates generic {@link TagTemplate} template.
	 */
	public static TagTemplate tag(final String name, final List<? extends HTMLTemplateFragment> contents) {
		return tag(name, toArray(contents));
	}

	/**
	 * Creates a {@link HTMLConstants#CLASS_ATTR} {@link AttributeTemplate} template.
	 */
	public static AttributeTemplate css(String cssClass) {
		return new CssTagAttributeTemplate(0, 0, HTMLConstants.CLASS_ATTR, text(cssClass));
	}

	/**
	 * Creates a {@link HTMLConstants#STYLE_ATTR} {@link AttributeTemplate} template.
	 */
	public static AttributeTemplate style(String style) {
		return attr(HTMLConstants.STYLE_ATTR, style);
	}

	/**
	 * Creates a {@link HTMLConstants#COLSPAN_ATTR} {@link AttributeTemplate} template.
	 */
	public static AttributeTemplate colspan(int value) {
		return attr(HTMLConstants.COLSPAN_ATTR, Integer.toString(value));
	}

	/**
	 * Creates a generic {@link AttributeTemplate} template.
	 */
	public static AttributeTemplate attr(final String name, final String value) {
		return new TagAttributeTemplate(0, 0, name, text(value));
	}

	/**
	 * Creates a generic {@link AttributeTemplate} template.
	 */
	public static AttributeTemplate resourceAttr(final String name, final ResKey key) {
		return new TagAttributeTemplate(0, 0, name, resource(key));
	}

	/**
	 * Creates a {@link TemplateSequence} template, if more than one template is given.
	 */
	public static HTMLTemplateFragment fragment(final HTMLTemplateFragment... sequence) {
		switch (sequence.length) {
			case 0:
				return EmptyTemplate.INSTANCE;
			case 1:
				return sequence[0];
			default:
				return new TemplateSequence(Arrays.asList(sequence));
		}
	}

	/**
	 * Creates a {@link TemplateSequence} template.
	 */
	public static TemplateSequence fragment(final List<? extends HTMLTemplateFragment> contents) {
		return new TemplateSequence(contents);
	}

	private static HTMLTemplateFragment[] toArray(final List<? extends HTMLTemplateFragment> contents) {
		return contents.toArray(new HTMLTemplateFragment[contents.size()]);
	}

	private static AttributeTemplate[] toAttributeArray(final List<? extends AttributeTemplate> contents) {
		return contents.toArray(new AttributeTemplate[contents.size()]);
	}

	private static HTMLTemplateFragment[] toBoxArray(final List<? extends HTMLTemplateFragment> contents) {
		return contents.toArray(new HTMLTemplateFragment[contents.size()]);
	}

	/**
	 * Creates a {@link LiteralText} template.
	 */
	public static LiteralText text(final String value) {
		return new LiteralText(0, 0, value);
	}

	/**
	 * Creates a {@link StringLiteral} template.
	 */
	public static StringLiteral string(final String value) {
		return new StringLiteral(0, 0, value);
	}

	/**
	 * Creates a {@link HTMLSource} template.
	 */
	public static HTMLSource htmlSource(final String value) {
		return new HTMLSource(value);
	}

	/**
	 * {@link HTMLFragmentInclude} rendering the given fragment.
	 */
	public static HTMLFragmentInclude htmlTemplate(HTMLFragment fragment) {
		return new HTMLFragmentInclude(fragment);
	}

	/**
	 * Creates a {@link Resource} template.
	 */
	public static Resource resource(final ResKey key) {
		return new Resource(key);
	}

	/**
	 * Creates a {@link ResourceExpr} template.
	 */
	public static ResourceExpr resourceExpr(final ResKey key) {
		return new ResourceExpr(key);
	}

	/**
	 * Creates an {@link Embedd} template.
	 */
	public static Embedd embedd(HTMLTemplateFragment... contents) {
		return new Embedd(fragment(contents));
	}

	/**
	 * Creates an {@link Embedd} template.
	 */
	public static Embedd embedd(List<? extends HTMLTemplateFragment> contents) {
		return embedd(toArray(contents));
	}

	/**
	 * Creates an {@link EmptyTemplate} template.
	 */
	public static EmptyTemplate empty() {
		return EmptyTemplate.INSTANCE;
	}

	/**
	 * Creates a {@link Items} template.
	 */
	public static Items items(HTMLTemplateFragment template) {
		return new Items(template);
	}

	/**
	 * Creates a {@link Self} template with label style.
	 */
	public static Self label() {
		return self(MemberStyle.LABEL);
	}

	/**
	 * Creates a {@link Self} template with label and colon style.
	 */
	public static Self labelWithColon() {
		return self(MemberStyle.LABEL_WITH_COLON);
	}

	/**
	 * Creates a {@link Self} template with error style.
	 */
	public static Self error() {
		return self(MemberStyle.ERROR);
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#LABEL} style.
	 */
	public static Member label(FormMember member) {
		return label(member.getName());
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#ERROR} style.
	 */
	public static Member error(FormMember member) {
		return error(member.getName());
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#LABEL} style.
	 */
	public static Member label(String name) {
		return member(name, MemberStyle.LABEL);
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#LABEL_WITH_COLON} style.
	 */
	public static Member labelWithColon(String name) {
		return member(name, MemberStyle.LABEL_WITH_COLON);
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#ERROR} style.
	 */
	public static Member error(String name) {
		return member(name, MemberStyle.ERROR);
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(FormMember member) {
		return member(member.getName());
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(FormMember member, HTMLTemplateFragment template) {
		return member(member.getName(), template);
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(final String name, HTMLTemplateFragment template) {
		return member(name, MemberStyle.NONE, template);
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(final String name) {
		return member(name, MemberStyle.NONE);
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(final String name, MemberStyle style) {
		return member(name, style, (HTMLTemplateFragment) null);
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(final String name, MemberStyle style, HTMLTemplateFragment template) {
		return new Member(name, style, template, null);
	}

	/**
	 * Creates a {@link Member} template.
	 */
	public static Member member(final String name, MemberStyle style, ControlProvider cp) {
		return new Member(name, style, null, cp);
	}

	/**
	 * Creates the name of an inner member.
	 * 
	 * @param memberNames
	 *        Path navigating through the {@link FormMember} hierarchy to identify an (deep) inner
	 *        member.
	 * @return Name of an inner member, e.g. to use in {@link #member(String)}.
	 */
	public static String path(String... memberNames) {
		switch (memberNames.length) {
			case 0:
				return ".";
			case 1:
				return memberNames[0];
			default:
				StringBuilder path = new StringBuilder(memberNames[0]);
				for (int i = 1; i < memberNames.length; i++) {
					path.append('.');
					path.append(memberNames[i]);
				}
				return path.toString();
		}

	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#DIRECT} style.
	 */
	public static Self direct() {
		return self(MemberStyle.DIRECT);
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#DIRECT} style.
	 */
	public static Self direct(ControlProvider cp) {
		return self(MemberStyle.DIRECT, cp);
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#DIRECT} style.
	 */
	public static Member direct(FormMember member) {
		return direct(member.getName());
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#DIRECT} style.
	 */
	public static Member direct(String name) {
		return member(name, MemberStyle.DIRECT);
	}

	/**
	 * Creates a {@link Member} template with {@link MemberStyle#DIRECT} style.
	 */
	public static Member direct(String name, ControlProvider cp) {
		return new Member(name, MemberStyle.DIRECT, null, cp);
	}

	/**
	 * Creates a {@link Self} template.
	 */
	public static Self self() {
		return self((HTMLTemplateFragment) null);
	}

	/**
	 * Creates a {@link Self} template.
	 */
	public static Self self(HTMLTemplateFragment template) {
		return self(MemberStyle.NONE, template);
	}

	/**
	 * Creates a {@link Self} template.
	 */
	public static Self self(MemberStyle style) {
		return self(style, (HTMLTemplateFragment) null);
	}

	/**
	 * Creates a {@link Self} template.
	 */
	public static Self self(MemberStyle style, HTMLTemplateFragment template) {
		return new Self(style, template, null);
	}

	/**
	 * Creates a {@link Self} template.
	 */
	public static Self self(MemberStyle style, ControlProvider cp) {
		return new Self(style, null, cp);
	}

	/**
	 * Applies the given {@link HTMLTemplateFragment} to the given {@link FormMember}.
	 */
	public static <M extends FormMember> M template(M field, HTMLTemplateFragment template) {
		ControlProvider cp;
		if (template instanceof Embedd) {
			cp = new TemplateAnnotation((Embedd) template);
		} else {
			cp = new TemplateControlProvider(template, DefaultFormFieldControlProvider.INSTANCE);
		}
		field.setControlProvider(cp);
		return field;
	}

	/**
	 * Creates a vertically layouted {@link HTMLTemplateFragment}.
	 * 
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to expand vertically aligned.
	 */
	public static HTMLTemplateFragment verticalBox(HTMLTemplateFragment... contents) {
		return collectionBox(VerticalLayout.INSTANCE, contents);
	}

	/**
	 * Creates a vertically layouted {@link HTMLTemplateFragment}.
	 * 
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to expand vertically aligned.
	 */
	public static HTMLTemplateFragment verticalBox(List<HTMLTemplateFragment> contents) {
		return verticalBox(toBoxArray(contents));
	}

	/**
	 * Creates a horizontally layouted {@link HTMLTemplateFragment}.
	 * 
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to expand vertically aligned.
	 */
	public static HTMLTemplateFragment horizontalBox(HTMLTemplateFragment... contents) {
		return collectionBox(HorizontalLayout.INSTANCE, contents);
	}

	/**
	 * Creates a horizontally layouted {@link HTMLTemplateFragment}.
	 * 
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to expand vertically aligned.
	 */
	public static HTMLTemplateFragment horizontalBox(List<? extends HTMLTemplateFragment> contents) {
		return horizontalBox(toBoxArray(contents));
	}

	/**
	 * Creates a custom layouted {@link HTMLTemplateFragment}.
	 * 
	 * @param layout
	 *        The {@link BoxLayout} to apply.
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to expand vertically aligned.
	 */
	public static HTMLTemplateFragment collectionBox(BoxLayout layout, HTMLTemplateFragment... contents) {
		return new CollectionBoxTemplate(layout, contents);
	}

	/**
	 * Creates a custom layouted {@link HTMLTemplateFragment}.
	 * 
	 * @param layout
	 *        The {@link BoxLayout} to apply.
	 * @param contents
	 *        The {@link HTMLTemplateFragment}s to expand vertically aligned.
	 */
	public static HTMLTemplateFragment collectionBox(BoxLayout layout, List<? extends HTMLTemplateFragment> contents) {
		return collectionBox(layout, toBoxArray(contents));
	}

	/**
	 * Creates a fieldset {@link HTMLTemplateFragment}.
	 * 
	 * @param legend
	 *        The {@link HTMLTemplateFragment} to expand in the legend area.
	 * @param content
	 *        The {@link HTMLTemplateFragment} to expand in the content area.
	 * @param personalizationKey
	 *        The personalization key to store the state to the {@link PersonalConfiguration}.
	 */
	public static FieldSetBoxTemplate fieldsetBoxDirect(HTMLTemplateFragment legend, HTMLTemplateFragment content,
			ConfigKey personalizationKey) {
		return new FieldSetBoxTemplate(legend, content).setPersonalizationKey(personalizationKey);
	}

	/**
	 * Creates a fieldset {@link HTMLTemplateFragment} with {@link #contentBox(HTMLTemplateFragment)
	 * wrapped} content.
	 * 
	 * @param legend
	 *        The {@link HTMLTemplateFragment} to expand in the legend area.
	 * @param content
	 *        The {@link HTMLTemplateFragment} to expand in the content area.
	 * @param personalizationKey
	 *        The personalization key to store the state to the {@link PersonalConfiguration}.
	 */
	public static FieldSetBoxTemplate fieldsetBoxWrap(HTMLTemplateFragment legend, HTMLTemplateFragment content,
			ConfigKey personalizationKey) {
		return fieldsetBoxDirect(legend, contentBox(content), personalizationKey);
	}

	/**
	 * Creates a fieldset {@link HTMLTemplateFragment}.
	 * 
	 * @param legend
	 *        The {@link HTMLTemplateFragment} to expand in the legend area.
	 * @param content
	 *        The {@link HTMLTemplateFragment} to expand in the content area.
	 * @param personalizationKey
	 *        The personalization key to store the state to the {@link PersonalConfiguration}.
	 */
	public static FieldSetBoxTemplate fieldsetBox(HTMLTemplateFragment legend, HTMLTemplateFragment content,
			ConfigKey personalizationKey) {
		return fieldsetBoxWrap(legend, content, personalizationKey);
	}

	/**
	 * Adapts the content template to a {@link HTMLTemplateFragment} to be used as box content.
	 * 
	 * @param content
	 *        The rendered content.
	 * @return A {@link HTMLTemplateFragment} that renders the given content.
	 */
	public static HTMLTemplateFragment contentBox(HTMLTemplateFragment content) {
		return contentBox(content, true);
	}

	/**
	 * Adapts the content template to a {@link HTMLTemplateFragment} to be used as box content.
	 * 
	 * @param content
	 *        The rendered content.
	 * @param wholeLine
	 *        Whether the content must be rendered using the whole line.
	 * @return A {@link HTMLTemplateFragment} that renders the given content.
	 */
	public static HTMLTemplateFragment contentBox(HTMLTemplateFragment content, boolean wholeLine) {
		TagTemplate contentFragment = div(css("rf_keepInline"), content);

		if (wholeLine) {
			return div(css(ReactiveFormCSS.RF_LINE), contentFragment);
		} else {
			return contentFragment;
		}
	}

	/**
	 * Creates a {@link HTMLTemplateFragment} that consists of a label and content area.
	 * 
	 * @param label
	 *        The label content.
	 * @param content
	 *        The main content.
	 * @return A {@link HTMLTemplateFragment} that renders label and content.
	 */
	public static HTMLTemplateFragment descriptionBox(HTMLTemplateFragment label, HTMLTemplateFragment content) {
		return descriptionBox(label, content, LabelPosition.DEFAULT);
	}

	/**
	 * Creates a {@link HTMLTemplateFragment} that consists of a label and content area.
	 * 
	 * @param label
	 *        The label content.
	 * @param content
	 *        The main content.
	 * @param labelPosition
	 *        The position of the given label content.
	 * @return A {@link HTMLTemplateFragment} that renders label and content.
	 */
	public static HTMLTemplateFragment descriptionBox(HTMLTemplateFragment label, HTMLTemplateFragment content,
			LabelPosition labelPosition) {
		return descriptionBox(label, content, labelPosition, LabelPlacement.DEFAULT);
	}

	/**
	 * Creates a {@link HTMLTemplateFragment} that consists of a label and content area.
	 * 
	 * @param label
	 *        The label content.
	 * @param content
	 *        The main content.
	 * @param labelPosition
	 *        The position of the given label content.
	 * @param labelPlacement
	 *        Whether the label is rendered above the value.
	 * @return A {@link HTMLTemplateFragment} that renders label and content.
	 */
	public static HTMLTemplateFragment descriptionBox(HTMLTemplateFragment label, HTMLTemplateFragment content,
			LabelPosition labelPosition, LabelPlacement labelPlacement) {
		return new DescriptionBoxTemplate(label, content, labelPosition, labelPlacement);
	}

	/**
	 * Creates a box template for a primitive field consisting of a label box and a content box with
	 * the input element and an error display.
	 * 
	 * @param name
	 *        The field name.
	 * @return A {@link HTMLTemplateFragment} that renders all aspects of a primitive field.
	 */
	public static HTMLTemplateFragment fieldBox(String name) {
		return fieldBox(name, LabelPlacement.DEFAULT);
	}

	/**
	 * Creates a box template for a primitive field consisting of a label box with an error display
	 * and a content box with the input element.
	 * 
	 * @param name
	 *        The field name.
	 * @param labelPlacement
	 *        Whether the label is rendered above the value.
	 * @return A {@link HTMLTemplateFragment} that renders all aspects of a primitive field.
	 */
	public static HTMLTemplateFragment fieldBox(String name, LabelPlacement labelPlacement) {
		LabelPosition labelPosition = LabelPosition.DEFAULT;
		return member(name, descriptionBox(fragment(labelWithColon(), error()), self(), labelPosition, labelPlacement));
	}

	/**
	 * Creates a box template for a primitive field consisting of a label box with an an error
	 * display but without a colon and a content box with the input element.
	 * 
	 * @param name
	 *        The field name.
	 * @return A {@link HTMLTemplateFragment} that renders all aspects of a primitive field.
	 */
	public static HTMLTemplateFragment fieldBoxInputFirst(String name) {
		LabelPosition labelPosition = LabelPosition.AFTER_VALUE;
		return member(name, descriptionBox(fragment(label(), error()), self(), labelPosition, LabelPlacement.INLINE));
	}

	/**
	 * Creates a box template for a primitive field consisting of an empty label box and a content
	 * box with the input element and an error display.
	 * 
	 * @param name
	 *        The field name.
	 * @return A {@link HTMLTemplateFragment} that renders all aspects of a primitive field.
	 */
	public static HTMLTemplateFragment fieldBoxNoLabel(String name) {
		LabelPosition labelPosition = LabelPosition.HIDE_LABEL;
		return member(name,
			descriptionBox(Templates.empty(), fragment(self(), error()), labelPosition, LabelPlacement.DEFAULT));
	}

	/**
	 * {@link HTMLTemplateFragment} creating a tuple of an icon-label-pair and an element (e.g. a
	 * descriptionBox or a fieldSet for the form editor.
	 * 
	 * @param template
	 *        The {@link HTMLTemplateFragment} to render the element.
	 * @param arguments
	 *        A mapping of arguments to adjust the {@link FormEditorElementTemplate}.
	 * @return A new {@link FormEditorElementTemplate}.
	 */
	public static HTMLTemplateFragment formEditorElement(HTMLTemplateFragment template, TypedAnnotatable arguments) {
		return new FormEditorElementTemplate(template, arguments);
	}

}
