/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.func.Function4;
import com.top_logic.layout.DefaultRefVisitor;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.channel.linking.ref.NamedComponent;
import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Option provider for {@link Channel#getName()}.
 * 
 * <p>
 * The possible options depend on
 * </p>
 * 
 * <ul>
 * <li>the context component that must be passed through
 * {@link ComponentConfigurationDialogBuilder#COMPONENT}</li>
 * <li>The selected {@link Channel#getComponentRef()} and the contents thereof:
 * <ul>
 * <li>{@link NamedComponent#getName()} or</li>
 * <li>{@link ComponentRelation#getKind()}</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllChannelNames
		extends Function4<Iterable<String>, Channel, ComponentRef, ComponentName, ComponentRelation.Kind> {

	private LayoutComponent _component;

	/**
	 * Creates a {@link AllChannelNames} function.
	 */
	@CalledByReflection
	public AllChannelNames(DeclarativeFormOptions options) {
		_component = options.get(ComponentConfigurationDialogBuilder.COMPONENT);
	}

	@Override
	public Iterable<String> apply(Channel self, ComponentRef ref, ComponentName name,
			ComponentRelation.Kind relationKind) {
		// Note: The dependency to the component name and relation kind is only declared to enforce
		// updating the options, if they change.
		if (ref != null) {
			LayoutComponent referencedComponent = DefaultRefVisitor.resolveReference(ref, targetComponent(self));
			if (referencedComponent != null) {
				return referencedComponent.getChannelNames();
			}
		}

		return Collections.emptySet();
	}

	private LayoutComponent targetComponent(Channel self) {
		if (channelDefinition(self) instanceof OpenModalDialogCommandHandler.Config) {
			return _component.getDialogParent();
		}
		return _component;
	}

	private ConfigurationItem channelDefinition(Channel self) {
		ConfigurationItem owner = self.container();
		while (owner != null && owner instanceof ModelSpec) {
			owner = ((ModelSpec) owner).container();
		}
		return owner;
	}

}
