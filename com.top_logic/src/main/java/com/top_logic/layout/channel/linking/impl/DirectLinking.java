/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.impl;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DefaultRefVisitor;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.channel.linking.ref.NamedComponent;
import com.top_logic.layout.channel.linking.ref.RefVisitor;
import com.top_logic.mig.html.layout.ComponentNameFormat;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ChannelLinking} specified by {@link Channel}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class DirectLinking extends AbstractChannelLinking<Channel> implements RefVisitor<Void, StringBuilder> {

	/**
	 * Creates a {@link DirectLinking}.
	 */
	public DirectLinking(InstantiationContext context, Channel config) {
		super(context, config);
	}

	@Override
	public ComponentChannel resolveChannel(Log log, LayoutComponent contextComponent) {
		LayoutComponent sourceComponent =
			DefaultRefVisitor.resolveReference(log, getConfig().getComponentRef(), contextComponent);
		if (sourceComponent == null) {
			return null;
		}

		ComponentChannel result = sourceComponent.getChannelOrNull(getConfig().getName());
		if (result == null) {
			log.error(
				"No such channel '" + getConfig().getName() + "' in component '" + sourceComponent.getName() + " ("
					+ sourceComponent.getClass().getName() + ")'.");
		}
		return result;
	}

	@Override
	public Object eval(LayoutComponent component) {
		LayoutComponent sourceComponent = ComponentRef.resolveComponent(getConfig().getComponentRef(), component);
		if (sourceComponent == null) {
			// Safety against invalid component references.
			Logger.error(
				"Cannot resolve component '" + getConfig().getComponentRef() + "' in the context of component '"
					+ component + "' at " + getConfig().location() + ".",
				DirectLinking.class);
			return null;
		}
		String channelName = getConfig().getName();
		ChannelSPI channel = sourceComponent.channelSPI(channelName);
		if (channel == null) {
			Logger.error(
				"No such channel '" + channelName + "' in component '" + sourceComponent + "' at "
					+ getConfig().location() + ".",
				DirectLinking.class);
			return null;
		}
		return channel.resolveModel(sourceComponent);
	}

	@Override
	public void appendTo(StringBuilder result) {
		result.append(getConfig().getName());
		result.append("(");
		getConfig().getComponentRef().visit(this, result);
		result.append(")");
	}

	@Override
	public Void visitComponentRelation(ComponentRelation ref, StringBuilder arg) {
		arg.append(ref.getKind().name());
		arg.append("()");
		return null;
	}

	@Override
	public Void visitNamedComponent(NamedComponent ref, StringBuilder arg) {
		arg.append(ComponentNameFormat.INSTANCE.getSpecification(ref.getName()));
		return null;
	}

	@Override
	public boolean hasCompactRepresentation() {
		return true;
	}
}
