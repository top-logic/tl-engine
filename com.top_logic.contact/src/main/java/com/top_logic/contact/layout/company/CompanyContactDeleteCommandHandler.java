/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.company;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.contact.business.CompanyContact;
import com.top_logic.contact.layout.ContactDeleteHandler;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class CompanyContactDeleteCommandHandler extends ContactDeleteHandler {

    public static final String COMMAND_ID = "companyContactDelete";
    
    /** 
     * Creates a {@link CompanyContactDeleteCommandHandler}.
     */
    public CompanyContactDeleteCommandHandler(InstantiationContext context, Config config) {
        super(context, config);
    }
    
    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), ExecutabilityRuleManager.getRule(COMMAND_ID));
    }
    
    public static class CompanyContactDeleteRule implements ExecutabilityRule {
        
        public static final CompanyContactDeleteRule INSTANCE = new CompanyContactDeleteRule();
        
        public ExecutableState isExecutable(CompanyContact aCompany) {
        	try {
				if (aCompany != null && WrapperMetaAttributeUtil.hasWrappersWithValue(aCompany)) {
        			return ExecutableState.createDisabledState(I18NConstants.ERROR_COMPANY_STILL_REFERENCED);
        		}
        	}
        	catch (Exception ex) {
        		Logger.error("Unable to check, if delete is allowed for " + aCompany, ex, this);
        	}
        	return ExecutableState.EXECUTABLE;
        }

        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
			return this.isExecutable((CompanyContact) model);
        }
    }
}

