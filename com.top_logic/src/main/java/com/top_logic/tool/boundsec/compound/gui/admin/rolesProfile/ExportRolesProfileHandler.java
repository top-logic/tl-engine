/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConfigTree;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.tool.boundsec.AbstractDownloadHandler;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.gui.profile.EditSecurityProfileComponent;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.model.ModelService;

/**
 * Exports the {@link SecurityConfig} for the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ExportRolesProfileHandler extends AbstractDownloadHandler {

	/** Root tag for the {@link SecurityConfig} in configuration files. */
	public static final String XML_TAG_SECURITY = "security";

	/**
	 * Creates a new {@link ExportRolesProfileHandler}.
	 */
	public ExportRolesProfileHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object prepareDownload(LayoutComponent aComponent, DefaultProgressInfo progressInfo,
			Map<String, Object> arguments) throws Exception {
		Collection<LayoutConfigTree> layoutTrees =
			((EditSecurityProfileComponent) aComponent).getAvailableLayoutTrees();

		InMemoryBinaryData inMemoryBinaryData = new InMemoryBinaryData(BinaryData.CONTENT_TYPE_OCTET_STREAM);

		dumpSecurityConfiguration(inMemoryBinaryData, layoutTrees);

		Document node;
		try (InputStream in = inMemoryBinaryData.getStream()) {
			node = DOMUtil.parseStripped(in);
		}
		inMemoryBinaryData.reset();

		XMLPrettyPrinter.dump(inMemoryBinaryData, node);

		return inMemoryBinaryData;
	}

	private void dumpSecurityConfiguration(OutputStream out, Collection<LayoutConfigTree> layoutTrees)
			throws XMLStreamException {
		SecurityConfig securityConfig = exportProfiles(layoutTrees);
		XMLStreamWriter streamWriter =
			XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(out, StringServicesShared.UTF8);
		ConfigurationWriter configWriter = new ConfigurationWriter(streamWriter);
		configWriter.write(XML_TAG_SECURITY, SecurityConfig.class, securityConfig);
	}

	private SecurityConfig exportProfiles(Collection<LayoutConfigTree> layoutTrees) {
		SecurityConfig securityConfig = TypedConfiguration.newConfigItem(SecurityConfig.class);
		for (LayoutConfigTree layout : layoutTrees) {
			addProfiles(securityConfig, layout);
		}
		return securityConfig;
	}

	private void addProfiles(SecurityConfig securityConfig, LayoutConfigTree layout) {
		RolesProfileHandler rph = new RolesProfileHandler();
		TLModel applicationModel = ModelService.getApplicationModel();
		Collection<String> structureNames = AccessManager.getInstance().getStructureNames();
		for (String structureName : sortedStringCopy(structureNames)) {
			TLModule module = applicationModel.getModule(structureName);
			List roles = sortedRolesCopy(BoundHelper.getInstance().getPossibleRoles(module));
			rph.addProfilesFor(securityConfig, roles, layout);
		}
	}

	private List sortedRolesCopy(Collection possibleRoles) {
		List copy = new ArrayList(possibleRoles);
		Collections.sort(copy);
		return copy;
	}

	private String[] sortedStringCopy(Collection<String> names) {
		String[] copy = names.toArray(ArrayUtil.EMPTY_STRING_ARRAY);
		Arrays.sort(copy);
		return copy;
	}

	@Override
	public String getDownloadName(LayoutComponent aComponent, Object aPrepareResult) {
		return exportName();
	}

	private String exportName() {
		return "security.xml";
	}

	@Override
	public BinaryDataSource getDownloadData(Object aPrepareResult) throws Exception {
		return (BinaryDataSource) aPrepareResult;
	}

	@Override
	public void cleanupDownload(Object model, Object aContext) {
		// No cleanup
	}

}

