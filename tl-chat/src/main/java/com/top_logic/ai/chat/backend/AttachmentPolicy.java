/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.ai.chat.view.I18NConstants;
import com.top_logic.ai.core.message.ImageContent;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProviderShared;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.upload.AcceptedFileTypesConfig;
import com.top_logic.layout.form.values.edit.editor.AcceptedTypesChecker;
import com.top_logic.layout.react.control.upload.UploadSupport;

/**
 * Which files a message of a conversation may carry: what the answerer of the conversation can
 * process.
 *
 * <p>
 * A {@link ConversationBackend} states it in its {@link BackendFeatures#attachments()}; for the
 * framework's own agent the application configures it with
 * {@link ConversationBackendService.Config#getInternalAttachments()}. The upload of a file to the
 * chat and the send of a message are checked against the policy of the conversation's answerer
 * ({@link #check(String, long, int, long)}), and the browser offers and accepts only what the policy
 * allows.
 * </p>
 *
 * @param acceptedTypes
 *        The file types a message may carry, as a comma separated list in the syntax of
 *        {@link AcceptedFileTypesConfig#getAcceptedTypes()}: MIME types like
 *        <code>text/csv</code>, MIME patterns like <code>text/*</code> and file patterns like
 *        <code>*.zip</code>. <code>*&#47;*</code>, <code>*.*</code> and an empty list allow every
 *        type. A file's type is checked by its name, see {@link AcceptedTypesChecker}.
 * @param maxFiles
 *        How many files one message may carry; <code>0</code> for none.
 * @param maxFileSize
 *        The largest file a message may carry, in bytes; <code>0</code> for no limit.
 * @param maxMessageSize
 *        The largest sum of the sizes of all files of one message, in bytes; <code>0</code> for no
 *        limit.
 */
public record AttachmentPolicy(String acceptedTypes, int maxFiles, long maxFileSize, long maxMessageSize) {

	/** The media type of a PDF document. */
	public static final String PDF_MEDIA_TYPE = "application/pdf";

	/** {@link #maxFiles()} of {@link #MODEL_READABLE}. */
	public static final int MODEL_READABLE_MAX_FILES = 5;

	/** {@link #maxFileSize()} of {@link #MODEL_READABLE}: 10 MB. */
	public static final long MODEL_READABLE_MAX_FILE_SIZE = 10L * 1024 * 1024;

	/** {@link #maxMessageSize()} of {@link #MODEL_READABLE}: 50 MB. */
	public static final long MODEL_READABLE_MAX_MESSAGE_SIZE = 50L * 1024 * 1024;

	/**
	 * {@link #acceptedTypes()} of {@link #MODEL_READABLE}: the images a language model reads
	 * ({@link ImageContent#ACCEPTED_MEDIA_TYPES}) and PDF.
	 */
	public static final String MODEL_READABLE_TYPES = modelReadableTypes();

	/**
	 * The files the framework's own agent passes to the language model: images and PDF, at most
	 * {@value #MODEL_READABLE_MAX_FILES} files of up to 10 MB each and 50 MB in all.
	 */
	public static final AttachmentPolicy MODEL_READABLE = new AttachmentPolicy(MODEL_READABLE_TYPES,
		MODEL_READABLE_MAX_FILES, MODEL_READABLE_MAX_FILE_SIZE, MODEL_READABLE_MAX_MESSAGE_SIZE);

	/** No files at all. */
	public static final AttachmentPolicy NONE = new AttachmentPolicy("", 0, 0, 0);

	/**
	 * Creates an {@link AttachmentPolicy}.
	 */
	public AttachmentPolicy {
		acceptedTypes = acceptedTypes == null ? "" : acceptedTypes.trim();
		maxFiles = Math.max(maxFiles, 0);
		maxFileSize = Math.max(maxFileSize, 0);
		maxMessageSize = Math.max(maxMessageSize, 0);
	}

	/**
	 * The policy the given configuration describes.
	 */
	public static AttachmentPolicy of(Config config) {
		return new AttachmentPolicy(config.getAcceptedTypes(), config.getMaxFiles(), config.getMaxFileSize(),
			config.getMaxMessageSize());
	}

	/** Whether a message may carry any file at all. */
	public boolean allowsAny() {
		return maxFiles > 0;
	}

	/**
	 * Why a message may not carry the given file, or <code>null</code> if it may.
	 *
	 * @param fileName
	 *        The name of the file, which its type is checked by.
	 * @param size
	 *        The size of the file in bytes.
	 * @param attachedFiles
	 *        How many files the message carries besides it.
	 * @param attachedSize
	 *        The sum of the sizes of the files the message carries besides it, in bytes.
	 * @return The reason of the refusal, in a sentence naming the file.
	 */
	public ResKey check(String fileName, long size, int attachedFiles, long attachedSize) {
		if (!allowsAny()) {
			return com.top_logic.ai.chat.backend.I18NConstants.ATTACHMENTS_NOT_SUPPORTED;
		}
		if (new AcceptedTypesChecker(typeSet()).checkFileName(fileName) != null) {
			return I18NConstants.ERROR_ATTACHMENT_TYPE__FILE_TYPES.fill(fileName, typesLabel());
		}
		if (maxFileSize > 0 && size > maxFileSize) {
			return I18NConstants.ERROR_ATTACHMENT_TOO_LARGE__FILE_MAX.fill(fileName,
				UploadSupport.sizeLabel(maxFileSize));
		}
		if (attachedFiles >= maxFiles) {
			return I18NConstants.ERROR_TOO_MANY_ATTACHMENTS__FILE_MAX.fill(fileName, Integer.valueOf(maxFiles));
		}
		if (maxMessageSize > 0 && attachedSize + size > maxMessageSize) {
			return I18NConstants.ERROR_MESSAGE_TOO_LARGE__FILE_MAX.fill(fileName,
				UploadSupport.sizeLabel(maxMessageSize));
		}
		return null;
	}

	/**
	 * The {@link #acceptedTypes()} one by one, in their order.
	 */
	public List<String> types() {
		List<String> result = new ArrayList<>();
		for (String entry : acceptedTypes.split(",")) {
			String type = entry.trim();
			if (!type.isEmpty() && !result.contains(type)) {
				result.add(type);
			}
		}
		return result;
	}

	/**
	 * The {@link #acceptedTypes()} for a sentence telling the user what they may attach.
	 */
	public String typesLabel() {
		return String.join(", ", types());
	}

	private Set<String> typeSet() {
		Set<String> result = StringServices.toSet(acceptedTypes, ',');
		return result == null ? Set.of() : result;
	}

	private static String modelReadableTypes() {
		Set<String> types = new LinkedHashSet<>(ImageContent.ACCEPTED_MEDIA_TYPES);
		types.add(PDF_MEDIA_TYPE);
		return String.join(",", types);
	}

	/**
	 * Configuration of an {@link AttachmentPolicy}, see {@link AttachmentPolicy#of(Config)}.
	 *
	 * <p>
	 * Without settings of its own it describes {@link AttachmentPolicy#MODEL_READABLE}.
	 * </p>
	 */
	public interface Config extends AcceptedFileTypesConfig {

		/** Configuration name of {@link #getMaxFiles()}. */
		String MAX_FILES = "max-files";

		/** Configuration name of {@link #getMaxFileSize()}. */
		String MAX_FILE_SIZE = "max-file-size";

		/** Configuration name of {@link #getMaxMessageSize()}. */
		String MAX_MESSAGE_SIZE = "max-message-size";

		/**
		 * Comma separated list of the file types a message may carry.
		 *
		 * <p>
		 * A file type can either be a MIME type like <code>image/*</code> or
		 * <code>application/pdf</code>, or a file pattern such as <code>*.csv</code>. A file of a
		 * type the language model cannot read reaches it as a placeholder only. By default the
		 * images the language model reads and PDF.
		 * </p>
		 */
		@Override
		@Name(ACCEPTED_TYPES_NAME)
		@Label("Accepted file types")
		@ComplexDefault(ModelReadableTypes.class)
		String getAcceptedTypes();

		/**
		 * How many files one message may carry; <code>0</code> for none.
		 */
		@Name(MAX_FILES)
		@IntDefault(MODEL_READABLE_MAX_FILES)
		int getMaxFiles();

		/**
		 * The largest file a message may carry; <code>0</code> for no limit.
		 *
		 * <p>
		 * The application-wide limit of an upload applies as well.
		 * </p>
		 */
		@Name(MAX_FILE_SIZE)
		@Format(MemorySizeFormat.class)
		@LongDefault(MODEL_READABLE_MAX_FILE_SIZE)
		long getMaxFileSize();

		/**
		 * The largest sum of the sizes of all files of one message; <code>0</code> for no limit.
		 */
		@Name(MAX_MESSAGE_SIZE)
		@Format(MemorySizeFormat.class)
		@LongDefault(MODEL_READABLE_MAX_MESSAGE_SIZE)
		long getMaxMessageSize();
	}

	/**
	 * The default of {@link Config#getAcceptedTypes()}: {@link AttachmentPolicy#MODEL_READABLE_TYPES}.
	 */
	public static class ModelReadableTypes extends DefaultValueProviderShared {
		@Override
		public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
			return MODEL_READABLE_TYPES;
		}
	}
}
