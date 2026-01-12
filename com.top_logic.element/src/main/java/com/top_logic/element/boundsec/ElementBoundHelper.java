/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.model.TLModule;
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
