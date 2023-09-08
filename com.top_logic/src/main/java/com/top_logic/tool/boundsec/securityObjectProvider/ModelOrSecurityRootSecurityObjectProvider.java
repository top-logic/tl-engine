/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * The ModelOrSecurityRootSecurityObjectProvider uses the model of the component as security object.
 * If no model is available it will fall back to the ({@link BoundHelper#getDefaultObject()}) object
 *
 * @author <a href="mailto:kbu@top-logic.com">KBU</a>
 */
public class ModelOrSecurityRootSecurityObjectProvider implements SecurityObjectProvider {

	/** Alias for this provider */
    public static String ALIAS_NAME = "modelOrSecRoot";

    /** Saves an instance of this class. */
    public static final ModelOrSecurityRootSecurityObjectProvider INSTANCE = new ModelOrSecurityRootSecurityObjectProvider();

    /**
	 * Return model or SecurityRoot
	 * 
	 * @see PathSecurityObjectProvider#getSecurityObject(BoundChecker, Object, BoundCommandGroup)
	 */
	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		BoundObject theModel =
			PathSecurityObjectProvider.MODEL_INSTANCE.getSecurityObject(aChecker, model, aCommandGroup);
		if (theModel == null) {
			return SecurityRootObjectProvider.INSTANCE.getSecurityObject(aChecker, model, aCommandGroup);
		} else {
			return theModel;
		}
    }
    
}
