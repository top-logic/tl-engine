/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;

/**
 * Reads the configuration of one service from application configuration files the way the engine
 * does, so a test checks the XML an application writes rather than a hand-built item.
 */
public final class ServiceConfigFixture {

	private ServiceConfigFixture() {
		// Static utility.
	}

	/** The XML of an application configuration file that configures one service. */
	public static String file(String serviceClass, String instance) {
		return "<application config:interface=\"com.top_logic.basic.config.ApplicationConfig$Config\"\n"
			+ "\txmlns:config=\"http://www.top-logic.com/ns/config/6.0\"\n"
			+ ">\n"
			+ "\t<services>\n"
			+ "\t\t<config service-class=\"" + serviceClass + "\">\n"
			+ instance + "\n"
			+ "\t\t</config>\n"
			+ "\t</services>\n"
			+ "</application>\n";
	}

	/**
	 * The configuration of the given service as the engine reads it from the given layers, the first
	 * one lowest.
	 */
	@SuppressWarnings("unchecked")
	public static <C extends ServiceConfiguration<?>> C read(Class<? extends ManagedClass> service,
			String... layers) throws ConfigurationException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(ApplicationConfig.Config.class);
		ConfigurationReader reader = new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap("application", descriptor));
		List<CharacterContent> sources = new ArrayList<>();
		for (String layer : layers) {
			sources.add(CharacterContents.newContent(layer));
		}
		reader.setSources(sources);
		ApplicationConfig.Config config = (ApplicationConfig.Config) reader.read();
		return (C) config.getServices().get(service).getInstance();
	}
}
