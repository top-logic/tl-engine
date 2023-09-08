/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.security.handler;

import java.io.File;
import java.util.Map;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.boundsec.manager.ElementAccessExportHelper;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;

/**
 * {@link CommandHandler} providing an export for all security settings.
 * 
 * <ul>
 * <li>Attribute classifications</li>
 * <li>Permissions for roles on attribute classifications</li>
 * <li>Rules for assigning roles on objects.</li>
 * <li>Access rights for roles on components.</li>
 * <li>Group assignments for persons.</li>
 * </ul>
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class SecurityExportHandler extends AbstractDownloadHandler {

    public static final String COMMAND_ID = "securityExport";

    

    public SecurityExportHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	protected Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo, Map<String, Object> arguments) throws Exception {

        File theDirectory = Settings.getInstance().getTempDir();
        File theOutFile = File.createTempFile("attributeSecurity", ".xml", theDirectory);
		TagWriter theOut = TagWriter.newTagWriter(theOutFile, LayoutConstants.UTF_8);
		theOut.setIndent(true);
        try {
			ElementAccessExportHelper.export(theOut, true, true, true, true, true);
        } finally {
			theOut.close();
        }
        return theOutFile;
    }

    @Override
	public String getDownloadName(LayoutComponent aComponent, Object aPrepareResult) {
        return "security.xml";
    }

    @Override
	public BinaryDataSource getDownloadData(Object aPrepareResult) throws Exception {
		return BinaryDataFactory.createBinaryData((File) aPrepareResult);
    }

    @Override
	public void cleanupDownload(Object model, Object aContext) {
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.EXPORT_SECURITY_PROFILE;
    }

    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
        return InViewModeExecutable.INSTANCE;
    }
}
