/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.DefaultExpansionModel;
import com.top_logic.layout.form.boxes.reactive_tag.AbstractGroupSettings;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;
import com.top_logic.layout.form.boxes.tag.PersonalizedExpansionModel;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.template.WithProperties;

/**
 * {@link HTMLTemplateFragment} creating a fieldset box with legend and content.
 * 
 * @see #getLegend()
 * @see #getContent()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FieldSetBoxTemplate extends AbstractGroupSettings<FieldSetBoxTemplate> implements Template {

	private final HTMLTemplateFragment _legend;

	private final HTMLTemplateFragment _content;

	private ConfigKey _personalizationKey = ConfigKey.none();

	private boolean _initiallyCollapsed;

	private Consumer<GroupCellControl> _initializer;

	/**
	 * Creates a {@link FieldSetBoxTemplate}.
	 * 
	 * @param legend
	 *        See {@link #getLegend()}.
	 * @param content
	 *        See {@link #getContent()}.
	 * @see Templates#fieldsetBoxDirect(HTMLTemplateFragment, HTMLTemplateFragment, ConfigKey)
	 */
	FieldSetBoxTemplate(HTMLTemplateFragment legend, HTMLTemplateFragment content) {
		_legend = legend;
		_content = content;
	}

	/**
	 * The content to render into the legend.
	 */
	public HTMLTemplateFragment getLegend() {
		return _legend;
	}

	/**
	 * The box that represents the content of this fieldset.
	 */
	public HTMLTemplateFragment getContent() {
		return _content;
	}

	/**
	 * The personalization key for the collapsed state.
	 */
	public ConfigKey getPersonalizationKey() {
		return _personalizationKey;
	}

	/**
	 * @see #getPersonalizationKey()
	 */
	public FieldSetBoxTemplate setPersonalizationKey(ConfigKey personalizationKey) {
		_personalizationKey = Objects.requireNonNull(personalizationKey);
		return this;
	}

	/**
	 * Whether the fieldset box is collapsed when the user displays it the first time.
	 */
	public boolean isInitiallyCollapsed() {
		return _initiallyCollapsed;
	}

	/**
	 * @see #isInitiallyCollapsed()
	 */
	public FieldSetBoxTemplate setInitiallyCollapsed(boolean initiallyCollapsed) {
		_initiallyCollapsed = initiallyCollapsed;
		return this;
	}

	/**
	 * An optional initializer that is applied when the displaying control is created before it is
	 * rendered the first time.
	 */
	public Consumer<GroupCellControl> getInitializer() {
		return _initializer;
	}

	/**
	 * @see #getInitializer()
	 */
	public FieldSetBoxTemplate setInitializer(Consumer<GroupCellControl> initializer) {
		_initializer = initializer;
		return this;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		Collapsible collapsible = createExpansionModel();
		HTMLFragment legend = TemplateRenderer.toFragmentInline(properties, getLegend());
		HTMLFragment content = TemplateRenderer.toFragment(properties, getContent());
		GroupCellControl groupCell = new GroupCellControl(content, collapsible, this).setTitle(legend);
		if (_initializer != null) {
			_initializer.accept(groupCell);
		}
		TemplateRenderer.renderControl(displayContext, out, groupCell);
	}

	private Collapsible createExpansionModel() {
		ConfigKey personalizationKey = getPersonalizationKey();
		boolean initiallyCollapsed = isInitiallyCollapsed();

		Collapsible collapsible;
		if (personalizationKey != ConfigKey.none()) {
			collapsible = new PersonalizedExpansionModel(initiallyCollapsed, personalizationKey);
		} else {
			collapsible = new DefaultExpansionModel(initiallyCollapsed);
		}
		return collapsible;
	}

	@Override
	protected FieldSetBoxTemplate self() {
		return this;
	}
}
