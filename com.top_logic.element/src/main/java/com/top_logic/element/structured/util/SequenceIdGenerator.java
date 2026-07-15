/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.service.CommitHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;

/**
 * {@link NumberHandler} based on a {@link SequenceManager} generating formatted string IDs.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SequenceIdGenerator implements NumberHandler {

	/**
	 * Technical suffix appended to the physical sequence name (as stored in the sequence table) to
	 * ensure that no clash is produced with internal sequences.
	 *
	 * <p>
	 * The suffix is appended last, after the base name and the optional context (see
	 * {@link #sequenceName(String, Object)}), so that all mechanisms addressing a sequence (this
	 * generator, {@link SequenceDefaultProvider} and the {@code generateSequenceId}/
	 * {@code resetSequence} TL-Script functions) compute the same physical name.
	 * </p>
	 */
	public static final String SEQUENCE_SUFFIX = "_SequenceId";

	/**
	 * @see ConfiguredNumberHandler.UIConfig#getNumberPattern()
	 */
	public static final String NUMBER_PLACEHOLDER = "%NUMBER%";

	/**
	 * @see ConfiguredNumberHandler.UIConfig#getDatePattern()
	 */
	public static final String DATE_PLACEHOLDER = "%DATE%";

	/**
	 * @see ConfiguredNumberHandler.UIConfig#getDynamicSequenceName()
	 */
	public static final String OBJECT_PLACEHOLDER = "%OBJECT%";

	private static final SequenceManager SEQUENCE_MANAGER = new RowLevelLockingSequenceManager();

	private String _sequenceName;

	private String _idPattern;

	private String _numberPattern;

	private String _datePattern;

	/**
	 * The label provider used to format the context object for generating context-sensitive number
	 * groups.
	 */
	private final DynamicSequenceName _dynamicContext;

	private int _retryCount;

	/**
	 * Creates a {@link SequenceIdGenerator}.
	 * 
	 * @param sequenceName
	 *        The base name of the sequence to use. Generators with the same value take numbers from
	 *        the same number group.
	 * @param idPattern
	 *        See {@link ConfiguredNumberHandler.UIConfig#getPattern()}.
	 * @param numberPattern
	 *        See {@link ConfiguredNumberHandler.UIConfig#getNumberPattern()}.
	 * @param datePattern
	 *        See {@link ConfiguredNumberHandler.UIConfig#getDatePattern()}.
	 * @param dynamicSequenceName
	 *        See {@link ConfiguredNumberHandler.UIConfig#getDynamicSequenceName()}.
	 * @param retryCount
	 *        See {@link ConfiguredNumberHandler.Config#getRetryCount()}.
	 */
	public SequenceIdGenerator(String sequenceName, String idPattern, String numberPattern, String datePattern,
			DynamicSequenceName dynamicSequenceName, int retryCount) {
		super();
		_sequenceName = sequenceName;
		_idPattern = idPattern;
		_numberPattern = numberPattern;
		_datePattern = datePattern;
		_dynamicContext = dynamicSequenceName;
		_retryCount = retryCount;
	}

	@Override
	public Object generateId(Object context) throws GenerateNumberException {
		try {
			StringBuilder sequenceNameBuilder = new StringBuilder(_sequenceName);
			String result = _idPattern;

			SimpleDateFormat dateFormat = createDateFormat();
			String formattedDate;
			if (dateFormat != null) {
				formattedDate = dateFormat.format(new Date());
				sequenceNameBuilder.append('_').append(formattedDate);
			} else {
				formattedDate = "";
			}
			result = StringServices.replace(result, DATE_PLACEHOLDER, formattedDate);

			String dynamicName;
			DynamicSequenceName labelProvider = _dynamicContext;
			if (labelProvider != null) {
				Object contextName = labelProvider.getSequenceName(context);
				addNames(sequenceNameBuilder, contextName);

				dynamicName = MetaLabelProvider.INSTANCE.getLabel(contextName);
			} else {
				dynamicName = "";
			}
			result = StringServices.replace(result, OBJECT_PLACEHOLDER, dynamicName);

			sequenceNameBuilder.append(SEQUENCE_SUFFIX);

			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			PooledConnection connection = ((CommitHandler) kb).createCommitContext().getConnection();
			long nextId = SEQUENCE_MANAGER.nextSequenceNumber(
				connection.getSQLDialect(), connection, _retryCount, sequenceNameBuilder.toString());

			String formattedId = createNumberFormat().format(nextId);
			result = StringServices.replace(result, NUMBER_PLACEHOLDER, formattedId);

			return result;
		} catch (SQLException e) {
			Logger.error("Unable to generate number due to SQLException.", e, ConfiguredNumberHandler.class);
			throw (GenerateNumberException) new GenerateNumberException(
				"Unable to generate number due to SQLException.").initCause(e);
		}
	}

	/**
	 * {@link DateFormat} that creates the {@link #DATE_PLACEHOLDER} part of the ID from the current
	 * time.
	 */
	protected final SimpleDateFormat createDateFormat() throws IllegalArgumentException {
		if (_datePattern == null) {
			return null;
		} else {
			return CalendarUtil.newSimpleDateFormat(_datePattern, Resources.getSystemInstance().getLocale());
		}
	}

	/**
	 * {@link NumberFormat} that creates the {@link #NUMBER_PLACEHOLDER} part of the ID from a
	 * unique sequence number.
	 */
	protected final NumberFormat createNumberFormat() throws IllegalArgumentException {
		return new DecimalFormat(_numberPattern);
	}

	/**
	 * Computes the physical sequence name (as stored in the sequence table) for the given base name
	 * and context.
	 *
	 * <p>
	 * The name is built as base name, followed by the context (see {@link #addNames(StringBuilder,
	 * Object)}), followed by the technical {@link #SEQUENCE_SUFFIX}. All mechanisms addressing a
	 * sequence must use this method (or the same layout) so that they operate on the same counter.
	 * </p>
	 *
	 * @param baseName
	 *        The configured base name of the sequence.
	 * @param context
	 *        The optional context discriminating the sequence (a {@link TLObject}, a value or a
	 *        {@link Collection} thereof), or <code>null</code> for a single global sequence.
	 * @return The physical sequence name.
	 */
	public static String sequenceName(String baseName, Object context) {
		StringBuilder sequenceNameBuilder = new StringBuilder(baseName);
		addNames(sequenceNameBuilder, context);
		sequenceNameBuilder.append(SEQUENCE_SUFFIX);
		return sequenceNameBuilder.toString();
	}

	/**
	 * Adds the given sequence name identifier to the given sequence name builder.
	 */
	public static void addNames(StringBuilder sequenceNameBuilder, Object id) {
		if (id instanceof Collection<?>) {
			for (Object element : (Collection<?>) id) {
				addNames(sequenceNameBuilder, element);
			}
		} else if (id instanceof TLObject) {
			addNameSeparator(sequenceNameBuilder);
			sequenceNameBuilder.append(((TLObject) id).tId().asString());
		} else if (id == null) {
			return;
		} else {
			addNameSeparator(sequenceNameBuilder);
			sequenceNameBuilder.append(MetaLabelProvider.INSTANCE.getLabel(id));
		}
	}

	private static void addNameSeparator(StringBuilder builder) {
		if (builder.length() > 0) {
			builder.append('_');
		}
	}

}
