/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.Protocol;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.util.TLModelUtil;

/**
 * Default base implementation of {@link TLModelPart}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTLModelPart extends TransientObject implements TLModelPart {

	private final TLModel model;
	private boolean resolved;
	
	private Map<Class<?>, TLAnnotation> _annotations;

	AbstractTLModelPart(TLModel model) {
		this.model = model;
	}

	@Override
	public TLModel getModel() {
		return this.model;
	}
	
	@Override
	public <T extends TLAnnotation> T getAnnotationLocal(Class<T> annotationInterface) {
		if (_annotations == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		T result = (T) _annotations.get(annotationInterface);
		return result;
	}

	@Override
	public void setAnnotation(TLAnnotation annotation) {
		if (_annotations == null) {
			_annotations = new LinkedHashMap<>();
		}
		_annotations.put(annotation.getConfigurationInterface(), annotation);
	}

	@Override
	public void removeAnnotation(Class<? extends TLAnnotation> annotationInterface) {
		if (_annotations == null) {
			return;
		}
		_annotations.remove(annotationInterface);
	}

	@Override
	public Collection<? extends TLAnnotation> getAnnotations() {
		if (_annotations == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableCollection(_annotations.values());
		}
	}

	protected final void localResolve(Protocol log) {
		if (this.resolved) {
			return;
		}
		
		this.internalResolve(log);
		
		this.resolved = true;
	}

	protected void checkResolve() {
		if (! this.resolved) {
			throw new IllegalStateException("Model has not yet been resolved.");
		}
	}
	
	protected void internalResolve(Protocol log) {
		// Nothing to resolve.
	}

	@Override
	public final String toString() {
		return TLModelUtil.qualifiedName(this);
	}

	/**
	 * Appends the names of all {@link TLNamedPart named parts}.
	 * 
	 * <p>
	 * If <code>namedParts</code> is not empty, this methods appends
	 * ',&lt;variableName&gt;[&lt;namedPart1&gt;,&lt;namedPart2&gt;,...,&lt;namedPartn&gt;]' to the
	 * given output.
	 * </p>
	 * 
	 * @param out
	 *        Output to put content to.
	 * @param variableName
	 *        name of the variable containing the named parts.
	 * @param namedParts
	 *        {@link TLNamedPart} to serialise.
	 */
	protected void appendNamedParts(StringBuilder out, String variableName, Collection<? extends TLNamedPart> namedParts) {
		if (!namedParts.isEmpty()) {
			out.append("," + variableName + "[");
			boolean isFirst = true;
			for (TLNamedPart namedPart : namedParts) {
				if (isFirst) {
					isFirst = false;
				} else {
					out.append(',');
				}
				out.append(namedPart.getName());
			}
			out.append("]");
		}
	}

}
