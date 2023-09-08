/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Implementation of a {@link ModelSpec}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Options(fun = AllInAppImplementations.class)
public interface ChannelLinking {

	/**
	 * Lookup of a {@link ComponentChannel} instance referenced by a {@link ModelSpec} relative to
	 * the given context component.
	 */
	ComponentChannel resolveChannel(Log log, LayoutComponent contextComponent);

	/**
	 * Computes the {@link ComponentChannel} value of the target channel specified by this
	 * {@link ChannelLinking}.
	 */
	Object eval(LayoutComponent component);

	/**
	 * Writes a compact representation to the given buffer.
	 */
	void appendTo(StringBuilder result);

	/**
	 * Whether the {@link #appendTo(StringBuilder) compact representation} can be used in a
	 * configuration.
	 */
	default boolean hasCompactRepresentation() {
		return false;
	}

	/**
	 * Updates the model specified by the given {@link ModelSpec}.
	 * 
	 * <p>
	 * The given {@link ModelSpec} is resolved and it's value is updated with the given model, if
	 * possible.
	 * </p>
	 * 
	 * @param modelSpec
	 *        The {@link ModelSpec} whose value must be updated. A value of <code>null</code> means
	 *        to update the given components's model, see
	 *        {@link com.top_logic.tool.boundsec.CommandHandler.Config#getTarget()}.
	 * @param baseComponent
	 *        {@link LayoutComponent Base component} to evaluate the given {@link ModelSpec}.
	 * @param model
	 *        The updated model to set.
	 * 
	 * @return Whether the model of the {@link ModelSpec} could be updated.
	 */
	static boolean updateModel(PolymorphicConfiguration<? extends ChannelLinking> modelSpec,
			LayoutComponent baseComponent, Object model) {
		if (modelSpec == null) {
			baseComponent.setModel(model);
			return true;
		}
		if (!(modelSpec instanceof Channel)) {
			// Can not update target model.
			return false;
		}
		Channel channel = (Channel) modelSpec;
		LayoutComponent targetComponent = ComponentRef.resolveComponent(channel.getComponentRef(), baseComponent);
		ComponentChannel targetChannel = targetComponent.getChannel(channel.getName());
		return targetChannel.set(model);
	}

	/**
	 * Instantiates the {@link ChannelLinking} specified by the given {@link ModelSpec} and
	 * evaluates it in the context of the given {@link LayoutComponent}.
	 * 
	 * <p>
	 * Consider using {@link #eval(LayoutComponent, ChannelLinking)} to avoid dynamic instantiation
	 * of the {@link ChannelLinking}.
	 * </p>
	 * 
	 * @see #eval(LayoutComponent, ChannelLinking)
	 */
	static Object eval(LayoutComponent component, ModelSpec modelSpec) {
		return eval(component, linking(modelSpec));
	}

	/**
	 * Null- and exception-safe variant of {@link #eval(LayoutComponent)}.
	 */
	static Object eval(LayoutComponent component, ChannelLinking linking) {
		if (linking == null) {
			return component.getModel();
		}
		Object result;
		try {
			result = linking.eval(component);
		} catch (Exception ex) {
			Logger.error("Unable to resolve '" + linking + "' from '" + component + "'.", ex,
				ChannelLinking.class);
			result = null;
		}
		return result;
	}

	/**
	 * Legacy compatibility for {@link #resolveChannel(Log, LayoutComponent)} with a
	 * {@link ModelSpec}.
	 */
	static ComponentChannel resolveChannel(Protocol log, LayoutComponent component, ModelSpec model) {
		return linking(model).resolveChannel(log, component);
	}

	/**
	 * Instantiates the given {@link ModelSpec}.
	 */
	static ChannelLinking linking(ModelSpec modelSpec) {
		return TypedConfigUtil.createInstance(modelSpec);
	}

}
