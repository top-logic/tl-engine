/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl.GroupBorder;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} sectioning the fields of a form.
 *
 * <p>
 * A group gathers the fields that belong together under a heading of its own - Name, Contact,
 * Address - and can be framed and folded away. It takes part in the grid of the enclosing
 * {@link FormElement} or {@link FieldsElement} rather than opening a grid of its own: the fields
 * inside a section line up with the fields outside it, column for column.
 * </p>
 *
 * <p>
 * A section spans the whole width of that grid. One that spans a single column instead confines
 * its content to that column, where it stacks one item below the other.
 * </p>
 */
@InApp
public class GroupElement extends ContainerElement {

	/**
	 * Configuration for {@link GroupElement}.
	 */
	@TagName("group")
	public interface Config extends ContainerElement.Config {

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getBorder()}. */
		String BORDER = "border";

		/** Configuration name for {@link #getCollapsible()}. */
		String COLLAPSIBLE = "collapsible";

		/** Configuration name for {@link #getCollapsed()}. */
		String COLLAPSED = "collapsed";

		/** Configuration name for {@link #getFullLine()}. */
		String FULL_LINE = "full-line";

		@Override
		@ClassDefault(GroupElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * The heading the section is displayed under.
		 *
		 * <p>
		 * A section without a label shows no heading, and is set apart by its frame alone.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * The frame drawn around the section.
		 */
		@Name(BORDER)
		GroupBorder getBorder();

		/**
		 * Whether the user can fold the section away.
		 */
		@Name(COLLAPSIBLE)
		boolean getCollapsible();

		/**
		 * Whether the section starts folded away.
		 *
		 * <p>
		 * The user unfolds it from the heading, so this applies to a section that can be folded.
		 * </p>
		 */
		@Name(COLLAPSED)
		boolean getCollapsed();

		/**
		 * Whether the section spans the whole width of the form it stands in.
		 *
		 * <p>
		 * A section over the whole width distributes its fields over the columns of the form. One
		 * confined to a single column stacks its content in that column, one item below the other.
		 * </p>
		 */
		@Name(FULL_LINE)
		@BooleanDefault(true)
		boolean getFullLine();
	}

	private final ResKey _label;

	private final GroupBorder _border;

	private final boolean _collapsible;

	private final boolean _collapsed;

	private final boolean _fullLine;

	/**
	 * Creates a new {@link GroupElement} from configuration.
	 */
	@CalledByReflection
	public GroupElement(InstantiationContext context, Config config) {
		super(context, config);

		_label = config.getLabel();
		_border = config.getBorder();
		_collapsible = config.getCollapsible();
		_collapsed = config.getCollapsed();
		_fullLine = config.getFullLine();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// The children are created in the very context the section stands in, so that a field
		// inside it reaches the same form model as a field beside it.
		List<ReactControl> children = createChildControls(context).stream()
			.map(child -> (ReactControl) child)
			.collect(Collectors.toList());

		String header = _label != null ? Resources.getInstance().getString(_label) : null;

		return new ReactFormGroupControl(context, header, _collapsible, _collapsed, _border, _fullLine,
			List.of(), children);
	}
}
