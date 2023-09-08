/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.layout.channel.linking.ref.ComponentRef;
import com.top_logic.layout.channel.linking.ref.ComponentRelation;
import com.top_logic.layout.channel.linking.ref.NamedComponent;
import com.top_logic.layout.channel.linking.ref.RefVisitor;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link RefVisitor} resolving a {@link ComponentRef} in context of a given component.
 * 
 * @see DefaultRefVisitor#resolveReference(Log, ComponentRef, LayoutComponent)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultRefVisitor implements RefVisitor<LayoutComponent, LayoutComponent> {

	/** Singleton {@link DefaultRefVisitor} instance. */
	public static final DefaultRefVisitor INSTANCE = new DefaultRefVisitor();

	private DefaultRefVisitor() {
		// singleton instance
	}

	/**
	 * Resolves the given {@link ComponentRef} in context of the given component.
	 * 
	 * @param log
	 *        Log to write potential messages to.
	 * @param ref
	 *        The {@link ComponentRef} to resolve.
	 * @param contextComponent
	 *        The context component. This component is used to resolve relative paths.
	 * 
	 * @return The resolved component. May be <code>null</code> in strange of errors.
	 */
	public static LayoutComponent resolveReference(Log log, ComponentRef ref, LayoutComponent contextComponent) {
		try {
			return ref.visit(DefaultRefVisitor.INSTANCE, contextComponent);
		} catch (RuntimeException ex) {
			log.error("Unable to resolve reference " + ref, ex);
			return null;
		}
	}

	@Override
	public LayoutComponent visitNamedComponent(NamedComponent ref, LayoutComponent arg) {
		ComponentName sourceComponentName = ref.getName();
		if (sourceComponentName == null) {
			return null;
		}
		LayoutComponent target = arg.getMainLayout().getComponentByName(sourceComponentName);
		if (target == null) {
			Logger.error(Resources.getSystemInstance()
				.getString(I18NConstants.UNKNOWN_COMPONENT__NAME__LOCATION.fill(sourceComponentName,
					arg.getLocation() + "': " + sourceComponentName)),
				DefaultRefVisitor.class);
		}
		return target;
	}

	@Override
	public LayoutComponent visitComponentRelation(ComponentRelation ref, LayoutComponent arg) {
		switch (ref.getKind()) {
			case dialogParent: {
				LayoutComponent dialogParent = arg.getDialogParent();
				if (dialogParent != null) {
					return dialogParent;
				} else {
					// Ignore, may be a default, but the layout is not used in a dialog.
					return null;
				}
			}
			case self: {
				return arg;
			}
			default:
				throw new UnsupportedOperationException("Kind is not supported: " + ref.getKind());
		}
	}

}

