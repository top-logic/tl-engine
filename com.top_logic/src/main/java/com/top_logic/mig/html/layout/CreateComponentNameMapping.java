/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMain;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.map.MultiMaps;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.processor.CompileTimeApplication;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * Creates a mapping file from {@link ComponentName#localName() local name} to
 * {@link ComponentName#qualifiedName() qualified name} of a component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateComponentNameMapping extends XMain {

	private final File _uniqueMapping = new File("uniqueComponentName.txt");

	private final File _nonUniqueMapping = new File("non-uniqueComponentName.txt");

	@Override
	protected void doActualPerformance() throws Exception {
		super.doActualPerformance();
		Map<ComponentName, Collection<ComponentName>> qualifiedNamesByLocalName = createMapping(getProtocol(), new File(getWebapp()));
		dumpQualifiednames(qualifiedNamesByLocalName);

	}

	private void dumpQualifiednames(Map<ComponentName, Collection<ComponentName>> qualifiedNamesByLocalName)
			throws IOException {
		try (PrintWriter uniqueOut =
			new PrintWriter(new OutputStreamWriter(new FileOutputStream(_uniqueMapping), StringServices.UTF8));
				PrintWriter nonuniqueOut = new PrintWriter(
					new OutputStreamWriter(new FileOutputStream(_nonUniqueMapping), StringServices.UTF8))) {
			qualifiedNamesByLocalName.entrySet().forEach(entry -> {
				if (entry.getValue().size() > 1) {
					nonuniqueOut.append(entry.getKey().localName()).append(" = ").append(entry.getValue().toString());
					nonuniqueOut.println();
				} else {
					uniqueOut.append(entry.getKey().localName()).append(" = ")
						.append(entry.getValue().iterator().next().toString());
					uniqueOut.println();
				}
			});
		}
	}

	/**
	 * Creates a map mapping the local component name to the collection of qualified component names
	 * with the same local name.
	 * 
	 * @param protocol
	 *        Log to write messages to.
	 * @param applicationRoot
	 *        The file of the web application for which the mapping should be created.
	 */
	public static Map<ComponentName, Collection<ComponentName>> createMapping(Protocol protocol,
			File applicationRoot) throws ModuleException, IOException {
		Map<ComponentName, Collection<ComponentName>> qualifiedNamesByLocalName;
		try (ModuleContext ctx = ModuleUtil.beginContext()) {
			ModuleUtil.INSTANCE.startUp(ThemeFactory.Module.INSTANCE);
			ModuleUtil.INSTANCE.startUp(CommandGroupRegistry.Module.INSTANCE);
			Theme currentTheme = ThemeFactory.getTheme();
			Map<String, List<BinaryData>> overlayByLayoutKey = LayoutStorage.readLayouts();
			qualifiedNamesByLocalName = new TreeMap<>(new Comparator<ComponentName>() {

				@Override
				public int compare(ComponentName o1, ComponentName o2) {
					return o1.qualifiedName().compareTo(o2.qualifiedName());
				}
			});
			for (String layout : overlayByLayoutKey.keySet()) {
				fetchNameMapping(currentTheme, layout, overlayByLayoutKey, qualifiedNamesByLocalName, applicationRoot,
					protocol);
			}
		}
		return qualifiedNamesByLocalName;
	}

	private static void fetchNameMapping(Theme theme, String layout, Map<String, List<BinaryData>> overlays,
			Map<ComponentName, Collection<ComponentName>> qualifiedNamesByLocalName, File applicationRoot,
			Protocol protocol) throws IOException {
		Protocol log = new BufferingProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		CreateComponentParameter params = CreateComponentParameter.newParameter(layout);
		params.setLayoutResolver(
			CompileTimeApplication.createApplicationModule(applicationRoot).createLayoutResolver(log, theme));
		Config root;
		try {
			root = LayoutStorage
				.createLayout(context, params, overlays.getOrDefault(params.getLayoutName(), Collections.emptyList()))
				.get();
			log.checkErrors();
		} catch (Exception ex) {
			protocol.error("Unable to process '" + layout + "' in theme '" + theme.getName() + "'.", ex);
			return;
		}
		DescendantDFSIterator<Config> it = new DescendantDFSIterator<>(LayoutConfigTreeView.INSTANCE, root, true);
		while (it.hasNext()) {
			Config conf = it.next();
			ComponentName name = conf.getName();
			if (LayoutConstants.isSyntheticName(name)) {
				continue;
			}
			ComponentName qualifiedName;
			ComponentName localName;
			if (!name.isLocalName()) {
				localName = ComponentName.newName(name.localName());
				qualifiedName = name;
			} else {
				localName = name;
				qualifiedName = ComponentName.newName(layout, name.localName());
			}
			MultiMaps.add(qualifiedNamesByLocalName, localName, qualifiedName, HashSet::new);
		}
	}

	@Override
	protected void setUp(String[] args) throws Exception {
		super.setUp(args);
		if (getWebapp() == null) {
			throw new IllegalStateException("Property 'webapp' needed.");
		}
	}

	public static void main(String[] args) throws Exception {
		new CreateComponentNameMapping().runMainCommandLine(args);
	}

}
