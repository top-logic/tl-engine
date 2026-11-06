/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Configuration of an element embedding a view as an isolated component: the channels of the
 * enclosing view shared with the embedded one.
 *
 * <p>
 * The embedded view gets a channel namespace of its own, see {@link #embeddedContext(ViewContext,
 * List)}. Only the channels named in the {@link #getBindings() bindings} cross the boundary.
 * </p>
 *
 * @see ReferenceElement
 * @see DynamicViewElement
 */
public interface ViewBindings extends ConfigurationItem {

	/** Configuration name for {@link #getBindings()}. */
	String BINDINGS = "bindings";

	/**
	 * Channel bindings from the parent scope to the embedded view's channels.
	 *
	 * <p>
	 * Each binding maps a channel declared in the embedded view to a channel in the parent's scope.
	 * At runtime, the parent's channel instance is registered directly in the child's context - both
	 * sides share the same object.
	 * </p>
	 */
	@Name(BINDINGS)
	@DefaultContainer
	List<ChannelBindingConfig> getBindings();

	/**
	 * The context an embedded view is created in.
	 *
	 * <p>
	 * An isolated child context: a fresh channel namespace, all ambient scopes inherited. The command
	 * scope is shared so that commands contributed by the embedded view (e.g. a dashboard's edit
	 * command) bubble up to the enclosing app bar; the security scope so that command security rules
	 * in the embedded view default to the enclosing removable unit; the object list scope so that an
	 * embedded view used as an {@code <object-list>} row keeps {@code <link-element>} /
	 * {@code <remove-element>} working. The form is shared as well: an embedded view inside a
	 * {@code <form>} fills a part of that form, its fields edit the form's object and its commands
	 * store the form's state.
	 * </p>
	 *
	 * @param parentContext
	 *        The context of the embedding element.
	 * @param bindings
	 *        The parent channels to share with the embedded view, see {@link #getBindings()}.
	 * @return The context with the bound parent channels registered under the names the embedded
	 *         view declares.
	 */
	static ViewContext embeddedContext(ViewContext parentContext, List<ChannelBindingConfig> bindings) {
		ViewContext childContext = parentContext.withIsolatedChannels();
		childContext.setFormModel(parentContext.getFormModel());
		for (ChannelBindingConfig binding : bindings) {
			ViewChannel parentChannel = parentContext.resolveChannel(binding.getTo());
			childContext.registerChannel(binding.getChannel(), parentChannel);
		}
		return childContext;
	}
}
