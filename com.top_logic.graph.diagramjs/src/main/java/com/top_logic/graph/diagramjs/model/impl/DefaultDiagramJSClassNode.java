/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model.impl;

import java.util.Collection;
import java.util.List;

import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.impl.DefaultNode;
import com.top_logic.graph.diagramjs.model.DiagramJSClassNode;

/**
 * {@link DefaultSharedObject} {@link DiagramJSClassNode} implementation.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultDiagramJSClassNode extends DefaultNode implements DiagramJSClassNode {

	/**
	 * Creates a {@link DefaultDiagramJSClassNode}.
	 * <p>
	 * This method has to be public, as it is called via reflection from the
	 * {@link ReflectionFactory}.
	 * </p>
	 *
	 * @param scope
	 *        See {@link DefaultSharedObject#DefaultSharedObject(ObjectScope)}.
	 */
	public DefaultDiagramJSClassNode(ObjectScope scope) {
		super(scope);
	}

	@Override
	public String getClassName() {
		return get(DiagramJSClassNode.NAME);
	}

	@Override
	public List<String> getClassModifiers() {
		return get(DiagramJSClassNode.MODIFIERS);
	}

	@Override
	public Collection<? extends DefaultDiagramJSLabel> getClassProperties() {
		return getReferrers(DefaultDiagramJSLabel.class, Label.OWNER);
	}

	@Override
	public void setClassName(String name) {
		set(DiagramJSClassNode.NAME, name);
	}

	@Override
	public void setClassModifiers(List<String> modifiers) {
		set(DiagramJSClassNode.MODIFIERS, modifiers);
	}

	@Override
	protected Label createLabelInternal() {
		return new DefaultDiagramJSLabel(scope());
	}

	@Override
	public List<String> getStereotypes() {
		return get(DiagramJSClassNode.STEREOTYPES);
	}

	@Override
	public void setStereotypes(List<String> stereotypes) {
		set(DiagramJSClassNode.STEREOTYPES, stereotypes);
	}

	@Override
	public boolean isImported() {
		return get(DiagramJSClassNode.IMPORTED);
	}

	@Override
	public void setImported(boolean isImported) {
		set(DiagramJSClassNode.IMPORTED, isImported);
	}

}
