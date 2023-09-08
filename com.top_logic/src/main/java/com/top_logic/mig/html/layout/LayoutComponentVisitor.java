/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.layout.basic.component.AJAXComponent;

/**
 * Visitor for layout components.
 * 
 * @see LayoutComponent#acceptVisitor(LayoutComponentVisitor) for the pure
 *      visitor pattern implementation.
 * 
 * @see LayoutComponent#acceptVisitorRecursively(LayoutComponentVisitor) for
 *      visiting a component tree recursively.
 * 
 * @see DefaultDescendingLayoutVisitor for a base class for deriving a custom
 *      visitor from.
 *      
 * <p>
 * TODO TSA what about <code>visit(Adorner)</code> nice for CommandHandler.
 * BHU: This would be a very bad idea, because {@link Adorner} is not a member
 * of the {@link LayoutComponent} type hierarchy. Introducing this method would
 * not match the visitor pattern.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public interface LayoutComponentVisitor {

    /**
	 * Declares the operation to be performed on components of type {@link LayoutComponent}.
	 * 
	 * <p>
	 * Perform some operation on the given component, and decide whether the visit should proceed
	 * over all children of the component.
	 * </p>
	 * 
	 * <p>
	 * This method's implementation is also choosen for all sub-types of {@link LayoutComponent}
	 * that do not have their own visit method.
	 * </p>
	 * 
	 * @param aComponent
	 *        The component being visited.
	 * @return Whether the visit should proceed recursively over all children of the component
	 *         passed as argument.
	 */
    public boolean visitLayoutComponent(LayoutComponent aComponent);

    /**
	 * Declares the operation to be performed on components of type {@link LayoutContainer}.
	 * 
	 * @param aComponent
	 *        The component being visited.
	 * @return Whether the visit should proceed recursively over all children of the component
	 *         passed as argument.
	 * 
	 * @see #visitLayoutComponent(LayoutComponent)
	 */
    public boolean visitLayoutContainer(LayoutContainer aComponent);

    /**
	 * Declares the operation to be performed on components of type {@link AJAXComponent}.
	 * 
	 * @param aComponent
	 *        The component being visited.
	 * @return Whether the visit should proceed recursively over all children of the component
	 *         passed as argument.
	 * 
	 * @see #visitLayoutComponent(LayoutComponent)
	 */
    public boolean visitAJAXComponent(AJAXComponent aComponent);
    
    /**
	 * Declares the operation to be performed on components of type {@link MainLayout}.
	 * 
	 * @param aComponent
	 *        The component being visited.
	 * @return Whether the visit should proceed recursively over all children of the component
	 *         passed as argument.
	 * 
	 * @see #visitLayoutComponent(LayoutComponent)
	 */
    public boolean visitMainLayout(MainLayout aComponent);

}
