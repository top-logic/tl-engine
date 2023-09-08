/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import static com.top_logic.layout.scripting.runtime.action.ApplicationAssertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.layout.scripting.check.ZipFileCheck.ZipFileCheckConfig.EntryConfig;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ValueCheck} that inspects the entries of a ZIP file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ZipFileCheck extends ValueCheck<ZipFileCheck.ZipFileCheckConfig> {

	/**
	 * Configuration of {@link ZipFileCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface ZipFileCheckConfig extends ValueCheck.ValueCheckConfig {

		/**
		 * The checks for entries.
		 */
		@EntryTag("path-constraints")
		@Key(EntryConfig.PATH_PROPERTY_NAME)
		Map<String, EntryConfig> getEntryConstraints();

		/**
		 * Entry names that must not occur in the tested ZIP file.
		 */
		@ListBinding(format = StringValueProvider.class, tag = "entry", attribute = "name")
		List<String> getForbiddenEntries();

		/**
		 * Description of a single ZIP file entry to check.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface EntryConfig extends ConfigurationItem {

			String PATH_PROPERTY_NAME = "path";
			/**
			 * The path of the ZIP entry to check.
			 */
			@Name(PATH_PROPERTY_NAME)
			String getPath();

			/**
			 * The {@link ValueCheck}s to apply to the given entry.
			 */
			@EntryTag("constraint")
			List<ValueCheckConfig> getContentConstraints();

		}

	}

	/**
	 * Create a {@link ZipFileCheck} from configuration.
	 */
	public ZipFileCheck(InstantiationContext context, ZipFileCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object value) {
		Collection<String> foundEntries = new ArrayList<>();
		Set<String> missingEntries = new HashSet<>(_config.getEntryConstraints().keySet());
		Set<String> forbiddenEntries = new HashSet<>(_config.getForbiddenEntries());

		try {
			try (ZipInputStream zipStream = new ZipInputStream(BinaryData.cast(value).getStream())) {
				while (true) {
					ZipEntry entry = zipStream.getNextEntry();
					if (entry == null) {
						break;
					}

					String entryName = entry.getName();
					if (forbiddenEntries.contains(entryName)) {
						throw ApplicationAssertions.fail(_config, "Forbidden entry '" + entryName + "' found.");
					}

					foundEntries.add(entryName);

					EntryConfig entryConfig = _config.getEntryConstraints().get(entryName);
					if (entryConfig != null) {
						missingEntries.remove(entryName);

						byte[] buffer;
						long entrySize = entry.getSize();
						if (entrySize >= 0) {
							buffer = new byte[(int) entrySize];
							int actuallyRead = StreamUtilities.readFully(zipStream, buffer);
							if (actuallyRead < entrySize) {
								throw ApplicationAssertions.fail(entryConfig, "Unexpected end of ZIP file entry.");
							}
						} else {
							try (ByteArrayOutputStream bufferStream = new ByteArrayOutputStream()) {
								StreamUtilities.copyStreamContents(zipStream, bufferStream);
								buffer = bufferStream.toByteArray();
							}
						}
						BinaryData entryData = BinaryDataFactory.createBinaryData(buffer);
						String contentType = BinaryData.CONTENT_TYPE_OCTET_STREAM;
						DefaultDataItem entryItem = new DefaultDataItem(entryName, entryData, contentType);

						for (ValueCheckConfig filterConfig : CollectionUtil
							.nonNull(entryConfig.getContentConstraints())) {
							ValueCheck<?> constraint =
								SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(filterConfig);
							constraint.check(context, entryItem);
						}
					}
				}
			}
		} catch (IOException ex) {
			throw ApplicationAssertions.fail(_config, "Error reading ZIP file.", ex);
		}

		if (missingEntries.size() > 0) {
			throw fail(_config,
				"Missing ZIP entries '" + StringServices.join(missingEntries, ", ") + "', all entries found: " +
					StringServices.join(foundEntries, ", "));
		}
	}

}
