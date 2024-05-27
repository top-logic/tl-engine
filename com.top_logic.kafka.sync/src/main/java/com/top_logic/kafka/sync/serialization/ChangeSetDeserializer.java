/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.serialization;

import static com.top_logic.basic.xml.XMLStreamUtil.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.kafka.common.serialization.Deserializer;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.kafka.serialization.TLDeserializer;
import com.top_logic.kafka.sync.knowledge.service.TLSyncRecord;
import com.top_logic.kafka.sync.knowledge.service.TLSyncUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.KnowledgeBaseName;
import com.top_logic.knowledge.service.db2.migration.ChangeSetReader;
import com.top_logic.knowledge.service.db2.migration.FuzzyTableNameMapping;
import com.top_logic.knowledge.service.db2.migration.TypeMapping;

/**
 * {@link Deserializer} for a {@link ChangeSet} formerly serialized by {@link ChangeSetSerializer}.
 * 
 * @see ChangeSetSerializer
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeSetDeserializer extends TLDeserializer<TLSyncRecord<ChangeSet>> {

	/**
	 * COnfiguration for a {@link ChangeSetDeserializer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ChangeSetDeserializer>, KnowledgeBaseName {

		/**
		 * Mapping that resolves the serialised type names.
		 */
		@ItemDefault(FuzzyTableNameMapping.Config.class)
		PolymorphicConfiguration<TypeMapping> getTypeMapping();

	}

	private static class ModifiableByteArrayInputStream extends ByteArrayInputStream {

		public ModifiableByteArrayInputStream() {
			super(ArrayUtil.EMPTY_BYTE_ARRAY);
		}

		public void updateByteArray(byte[] newInput, int position) {
			count = newInput.length;
			buf = newInput;
			pos = position;
			mark = pos;
		}

	}

	private static final int BYTES_PER_LONG = Long.SIZE / Byte.SIZE;

	private static final int MAX_HEADER_BYTES = 100;

	private static final int HEADER_SNIPPET_SIZE_IN_ERRORS = 1000;

	private final ModifiableByteArrayInputStream _byteArrayBuffer = new ModifiableByteArrayInputStream();

	private TypeMapping _typeMapper;

	private Config _config;

	/**
	 * Creates a new {@link ChangeSetDeserializer} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ChangeSetDeserializer}.
	 */
	public ChangeSetDeserializer(InstantiationContext context, Config config) {
		_config = config;
		_typeMapper = context.getInstance(config.getTypeMapping());
		_typeMapper.initTypeRepository(getRepository(_config.getKnowledgeBase()));
	}

	private MORepository getRepository(String kb) {
		return KnowledgeBaseFactory.getInstance().getKnowledgeBase(kb).getMORepository();
	}

	@Override
	public TLSyncRecord<ChangeSet> deserialize(String topic, byte[] message) {
		/* The "position" is passed through the methods and updated there, to avoid having to return
		 * the new value everywhere, which would require multiple return values or unnecessary
		 * instance variables. */
		MutableInt position = new MutableInt(0);
		boolean hasHeader = readHeader(message, position);
		long systemId = readLong(message, position);
		long lastMessageRevision = readLastMessageRevision(message, hasHeader, position);
		ChangeSet changeSet = readChangeSet(message, position.intValue());
		return new TLSyncRecord<>(systemId, lastMessageRevision, changeSet);
	}

	/**
	 * Returns whether there is a header.
	 * <p>
	 * If not, this is a legacy message without header.
	 * </p>
	 */
	private boolean readHeader(byte[] message, MutableInt position) {
		if (containsAtPosition(message, position.intValue(), ChangeSetSerializer.MESSAGE_TYPE_BYTES)) {
			position.add(ChangeSetSerializer.MESSAGE_TYPE_BYTES.length);
			checkVersionCompatibility(message, position);
			return true;
		}
		return false;
	}

	private boolean containsAtPosition(byte[] main, int position, byte[] contained) {
		return (main.length >= (contained.length + position))
			&& Arrays.equals(main, position, position + contained.length, contained, 0, contained.length);
	}

	private void checkVersionCompatibility(byte[] message, MutableInt position) {
		readSeparatorAfterType(message, position);
		String version = readVersion(message, position);
		checkVersionCompatibility(version, message);
	}

	private void readSeparatorAfterType(byte[] message, MutableInt position) {
		if (message[position.intValue()] == TLSyncUtils.MESSAGE_FIELD_SEPARATOR) {
			position.increment();
			return;
		}
		throw failMissingSeparatorAfterTypeField(message);
	}

	/**
	 * @implNote The actual return type is void, or better "Never Returns". But the latter does not
	 *           exist in Java and the former leads to an error about a missing return value in the
	 *           caller, as Eclipse does not understand that this method will abort the caller.
	 */
	private RuntimeException failMissingSeparatorAfterTypeField(byte[] message) {
		int headerSnippetSize = Math.min(HEADER_SNIPPET_SIZE_IN_ERRORS, message.length);
		throw new RuntimeException("The message starts with the TL-Sync message type header ('"
			+ TLSyncUtils.MESSAGE_TYPE + "'). But the separator ('" + TLSyncUtils.MESSAGE_FIELD_SEPARATOR
			+ "') after it is missing. Message start: "
			+ new String(message, 0, headerSnippetSize, TLSyncUtils.MESSAGE_HEADER_CHARSET));
	}

	private String readVersion(byte[] message, MutableInt position) {
		int beginPosition = position.intValue();
		findVersionEnd(message, position);
		int endPosition = position.intValue();
		int length = endPosition - beginPosition;
		return new String(message, beginPosition, length, TLSyncUtils.MESSAGE_HEADER_CHARSET);
	}

	private void findVersionEnd(byte[] message, MutableInt position) {
		while (message[position.intValue()] != TLSyncUtils.MESSAGE_FIELD_SEPARATOR) {
			position.increment();
			if (position.intValue() > MAX_HEADER_BYTES) {
				/* The version is never that long. Something is wrong with the data. Don't search
				 * the rest of the message. */
				throw failSeparatorAfterVersionNotFound(message);
			}
		}
		/* Add one to skip over the MESSAGE_FIELD_SEPARATOR. */
		position.increment();
	}

	private RuntimeException failSeparatorAfterVersionNotFound(byte[] message) {
		int headerSnippetSize = Math.min(HEADER_SNIPPET_SIZE_IN_ERRORS, message.length);
		throw new RuntimeException("The message starts with the TL-Sync message type header ('"
			+ TLSyncUtils.MESSAGE_TYPE + "'). But the version is missing or way too long. Message start: "
			+ new String(message, 0, headerSnippetSize, TLSyncUtils.MESSAGE_HEADER_CHARSET));
	}

	private void checkVersionCompatibility(String version, byte[] message) {
		int majorEnd = version.indexOf('.');
		if (majorEnd == -1) {
			failInvalidVersionFormat(message);
		}
		String majorVersion = version.substring(0, majorEnd);
		if (!majorVersion.equals(TLSyncUtils.MESSAGE_CURRENT_MAJOR_VERSION)) {
			failUnsupportedVersion(version);
		}
		/* Minor and micro versions don't mark incompatible version changes. They are only for
		 * debugging and don't need to be analyzed here. */
	}

	private void failInvalidVersionFormat(byte[] message) {
		int headerSnippetSize = Math.min(HEADER_SNIPPET_SIZE_IN_ERRORS, message.length);
		throw new RuntimeException("The message starts with the TL-Sync message type header ('"
			+ TLSyncUtils.MESSAGE_TYPE + "'). But the version is invalid, as it does not contain a dot"
			+ " to separate the major and minor part. Message start: "
			+ new String(message, 0, headerSnippetSize, TLSyncUtils.MESSAGE_HEADER_CHARSET));
	}

	private void failUnsupportedVersion(String version) {
		throw new RuntimeException("Unsupported version: '" + version + "'. Supported major version: "
			+ TLSyncUtils.MESSAGE_CURRENT_MAJOR_VERSION);
	}

	private long readLastMessageRevision(byte[] message, boolean hasHeader, MutableInt position) {
		if (hasHeader) {
			return readLong(message, position);
		}
		/* This is a legacy message without the field "last message revision". */
		return TLSyncRecord.LAST_MESSAGE_REVISION_NONE_RECEIVED;
	}

	private long readLong(byte[] data, MutableInt position) {
		long result = Utils.bytesToLong(data, position.intValue());
		position.add(BYTES_PER_LONG);
		return result;
	}

	private ChangeSet readChangeSet(byte[] data, int position) {
		XMLStreamReader streamReader;
		_byteArrayBuffer.updateByteArray(data, position);
		try {
			streamReader =
				getDefaultInputFactory().createXMLStreamReader(_byteArrayBuffer, StringServices.UTF8);
			nextStartTag(streamReader);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		}
		try (ChangeSetReader reader = new ChangeSetReader(_typeMapper, streamReader)) {
			return reader.read();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		// nothing to do here
	}

}

