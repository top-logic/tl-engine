/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.model.ModelService;


/**
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@ServiceDependencies({
	ModelService.Module.class,
})
public class ElementBoundHelper extends BoundHelper {

	private static final String SECURITY_STRUCTURE_NAME = "SecurityStructure";

	private volatile BoundObject _securityRoot;

	public ElementBoundHelper(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	protected BoundObject newDefaultObject() {
		return securityRoot();
	}

	@Override
    protected final String getCheckerTypeForType(Object type) {
		// Method declared final to prevent introducing even more type abstractions
		// outside the framework.
        if (type instanceof TLClass) {
			TLClass tlClass = (TLClass) type;
			TLScope scope = tlClass.getScope();
			if (scope instanceof TLModule) {
				return TLModelUtil.qualifiedName(tlClass);
			} else {
				/* Can not use full qualified name for local classes, because it contains id of the
				 * concrete scope element. Better use the checker type of the scope as "scope". */
				return TLModelUtil.qualifiedName(tlClass.getModule().getName(), getCheckerType(scope),
					tlClass.getName());
			}
        } else {
        	return super.getCheckerTypeForType(type);
        }
    }

    @Override
    protected final Object getObjectType(Object anObject) {
		// Method declared final to prevent introducing even more type abstractions
		// outside the framework.
		if (anObject instanceof TLObject) {
			TLStructuredType tType = ((TLObject) anObject).tType();
			if (tType != null) {
				return tType;
			}
			if (((TLObject) anObject).tTransient()) {
				// Transient objects have no table. This actually just occur in tests.
				return super.getObjectType(anObject);
			}
			return ApplicationObjectUtil.tableTypeQName(((TLObject) anObject).tTable());
		}
		return super.getObjectType(anObject);
    }

	@Override
	protected final List<?> getSuperTypes(Object anObject) {
		// Method declared final to prevent introducing even more type abstractions
		// outside the framework.
        if (anObject instanceof TLClass) {
			return ((TLClass) anObject).getGeneralizations();
        } else {
        	return super.getSuperTypes(anObject);
        }
    }
    
    /** 
     * the SecurityStructure root object
     */
    public static BoundObject getSecurityRoot() {
		ElementBoundHelper boundHelper = (ElementBoundHelper) BoundHelper.getInstance();
		return boundHelper.securityRoot();
    }

	/**
	 * The root object of the security structure
	 */
	public final BoundObject securityRoot() {
		if (_securityRoot == null) {
			ModelFactory factory = DynamicModelService.getFactoryFor(SECURITY_STRUCTURE_NAME);
			_securityRoot = (BoundObject) factory.getModule().getSingleton(TLModule.DEFAULT_SINGLETON_NAME);
		}

		return _securityRoot;
	}

	/**
	 * The default security module.
	 */
	public TLModule securityModule() {
		return TLModelUtil.findModule(SECURITY_STRUCTURE_NAME);
	}
}
