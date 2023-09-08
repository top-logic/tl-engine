/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLAssociationPart;
import com.top_logic.model.TLType;
import com.top_logic.model.format.TLObjectFormat;
import com.top_logic.model.search.ui.model.function.part.PartType;
import com.top_logic.model.search.ui.model.misc.Multiplicity;
import com.top_logic.model.search.ui.model.options.IncomingAssociationParts;
import com.top_logic.model.search.ui.model.options.OutgoingAssociationParts;
import com.top_logic.model.search.ui.model.structure.SearchPart;
import com.top_logic.model.search.ui.model.structure.WithValueContext;
import com.top_logic.model.search.ui.model.ui.StructuredTypePartWithOwnerType;

/**
 * A {@link SearchPart} that is based on using a {@link TLAssociation}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface AssociationBased extends ValueContext, WithValueContext {

	/**
	 * Property name of {@link #getIncomingPart()}.
	 */
	String INCOMING_PART = "incoming-part";

	/**
	 * Property name of {@link #getOutgoingPart()}.
	 */
	String OUTGOING_PART = "outgoing-part";

	/**
	 * The incoming {@link TLAssociationEnd}.
	 * <p>
	 * The result is the {@link #getOutgoingPart()}, while the {@link #getIncomingPart()} points to
	 * the context object.
	 * </p>
	 */
	@Name(INCOMING_PART)
	@Mandatory
	@Format(TLObjectFormat.class)
	@OptionLabels(StructuredTypePartWithOwnerType.class)
	@Options(fun = IncomingAssociationParts.class, args = {
		@Ref(CONTEXT_TYPE),
		@Ref(CONFIG_NAME) })
	TLAssociationPart getIncomingPart();

	/**
	 * The outgoing {@link TLAssociationEnd}.
	 * 
	 * @see #getIncomingPart()
	 */
	@Name(OUTGOING_PART)
	@Mandatory
	@Format(TLObjectFormat.class)
	@Options(fun = OutgoingAssociationParts.class, args = {
		@Ref(INCOMING_PART),
		@Ref(CONFIG_NAME)
	})
	TLAssociationPart getOutgoingPart();

	@Override
	@Derived(fun = PartType.class, args = @Ref(OUTGOING_PART))
	TLType getValueType();

	@Override
	@Derived(fun = Multiplicity.CollectionValue.class, args = {})
	boolean getValueMultiplicity();

}
