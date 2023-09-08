/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.progress;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.progress.AbstractProgressComponent;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.tool.export.progress.ExportDownloadComponent.DownloadData;

/**
 * Progress dialog for executing the export of a grid.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExportProgressComponent extends AbstractProgressComponent {

    public interface Config extends AbstractProgressComponent.Config {
		@Name(XML_DOWNLOAD_COMPONENT)
		@FormattedDefault("export/gridExportDialog.layout.xml#GridExportDownload")
		ComponentName getDownloadComp();
	}

	public static final String XML_DOWNLOAD_COMPONENT = "downloadComp";

	private ComponentName downloadComponentName;

    public ExportProgressComponent(InstantiationContext context, Config someAtts) throws ConfigurationException {
        super(context, someAtts);

        this.downloadComponentName = someAtts.getDownloadComp();
    }

	@Override
    public void finishParent() {
		DefaultProgressInfo progressInfo = (DefaultProgressInfo) getInfo();
		LayoutComponent downloadComponent = this.getMainLayout().getComponentByName(downloadComponentName);
		assert downloadComponent != null : "Component for export download not found: " + downloadComponentName;
		
		DownloadData data = new DownloadData(ExportProgressHandler.getExportData(progressInfo));
		if (progressInfo.shouldStop()) {
			return;
		}
		downloadComponent.setModel(data);
		LayoutComponent downloadDialog = downloadComponent.getParent();
		
		OpenModalDialogCommandHandler.openDialog(downloadDialog,
			new ResourceText(downloadComponent.getResPrefix().key("title")));
    }
}

