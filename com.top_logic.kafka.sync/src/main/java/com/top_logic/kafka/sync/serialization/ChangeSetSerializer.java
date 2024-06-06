/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.kafka.common.serialization.Serializer;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.kafka.serialization.TLSerializer;
import com.top_logic.kafka.sync.knowledge.service.TLSyncRecord;
import com.top_logic.kafka.sync.knowledge.service.TLSyncUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.service.db2.migration.ChangeSetDumper;

/**
 * {@link Serializer} of a {@link ChangeSet}.
 * 
 * @see ChangeSetDeserializer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetSerializer extends TLSerializer<TLSyncRecord<ChangeSet>>
		implements ConfiguredInstance<ChangeSetSerializer.Config> {

	/** {@link ConfigurationItem} for the {@link ChangeSetSerializer}. */
	public interface Config extends PolymorphicConfiguration<ChangeSetSerializer> {

		/** Property name of {@link #getMessageVersion()}. */
		String MESSAGE_VERSION = "message-version";

		/**
		 * Which message version should be written.
		 * <p>
		 * Supported values:
		 * <ol>
		 * <li>2.0.0: The current version. This is the default value.</li>
		 * <li>1.0.0: The old version, which did not include the revision of the last message. With
		 * this version, it is not possible to detect missing messages.</li>
		 * </ol>
		 * </p>
		 */
		@StringDefault(TLSyncUtils.MESSAGE_CURRENT_VERSION)
		@Name(MESSAGE_VERSION)
		String getMessageVersion();

	}

	/**
	 * @implNote This constant is not in {@link TLSyncUtils}, as arrays are bad public constants.
	 *           The array can accidentally be changed. The constant here are not public, which
	 *           reduces the risk drastically.
	 */
	static final byte[] MESSAGE_TYPE_BYTES =
		TLSyncUtils.MESSAGE_TYPE.getBytes(TLSyncUtils.MESSAGE_HEADER_CHARSET);

	static final byte[] MESSAGE_VERSION_BYTES =
		TLSyncUtils.MESSAGE_CURRENT_VERSION.getBytes(TLSyncUtils.MESSAGE_HEADER_CHARSET);

	private final ByteArrayOutputStream _out;

	private ChangeSetDumper _dumper;

	private final byte[] _longBuffer = new byte[Long.SIZE / Byte.SIZE];

	private final Config _config;

	private final boolean _useLegacyFormat;

	/** {@link TypedConfiguration} constructor for {@link ChangeSetSerializer}. */
	public ChangeSetSerializer(InstantiationContext context, Config config) {
		_config = config;
		_useLegacyFormat = config.getMessageVersion().equals(TLSyncUtils.MESSAGE_LEGACY_VERSION_1);
		checkSupportedMessageVersion(context, config.getMessageVersion());
		_out = new ByteArrayOutputStream();
		OutputStreamWriter outStream = new OutputStreamWriter(_out, StringServices.CHARSET_UTF_8);
		TagWriter outWriter = new TagWriter(outStream);
		_dumper = new ChangeSetDumper(outWriter);
	}

	private void checkSupportedMessageVersion(InstantiationContext context, String messageVersion) {
		if (!((messageVersion.equals(TLSyncUtils.MESSAGE_LEGACY_VERSION_1))
			|| messageVersion.equals(TLSyncUtils.MESSAGE_CURRENT_VERSION))) {
			context.error("Unsupported TL-Sync message version: '" + messageVersion + "'. Supported version are: "
				+ TLSyncUtils.MESSAGE_CURRENT_VERSION + ", " + TLSyncUtils.MESSAGE_LEGACY_VERSION_1);
		}
	}

	@Override
	public byte[] serialize(String topic, TLSyncRecord<ChangeSet> data) {
		try {
			writeMessage(data);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		byte[] result = _out.toByteArray();
		_out.reset();
		return result;
	}

	private void writeMessage(TLSyncRecord<ChangeSet> data) throws IOException {
		if (!_useLegacyFormat) {
			writeHeader();
		}
		writeLong(data.getSystemId());
		if (!_useLegacyFormat) {
			writeLong(data.getLastMessageRevision());
		}
		writeChangeSet(data.getRecord());
	}

	private void writeHeader() {
		_out.writeBytes(MESSAGE_TYPE_BYTES);
		_out.write(TLSyncUtils.MESSAGE_FIELD_SEPARATOR);
		_out.writeBytes(MESSAGE_VERSION_BYTES);
		_out.write(TLSyncUtils.MESSAGE_FIELD_SEPARATOR);
	}

	private void writeLong(long systemId) throws IOException {
		Utils.longToBytes(_longBuffer, 0, systemId);
		_out.write(_longBuffer);
	}

	private void writeChangeSet(ChangeSet data) throws IOException {
		_dumper.writeChangeSet(data);
		_dumper.flush();
	}

	@Override
	public void close() {
		if (_dumper == null) {
			return;
		}
		_out.reset();
		ChangeSetDumper dumper = _dumper;
		_dumper = null;
		try {
			dumper.close();
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}

