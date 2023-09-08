/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.display;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.Function2;
import com.top_logic.bpe.bpml.display.DisplayDescription.AttributePart.NamedPartNames;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.element.meta.form.fieldprovider.ConfigurationFieldProvider.EditContext;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.config.TLModelPartMapping;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.resources.TLPartScopedResourceProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.AllTypes;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Description of a form.
 * 
 * @deprecated Note: Class definition must be kept to allow easy upgrading existing instances to
 *             {@link FormDefinition}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public interface DisplayDescription extends EditContextPart {

	/**
	 * The edit context, in which this configuration is edited.
	 */
	@Override
	@Container
	EditContext getContext();

	/**
	 * The UI elements to display.
	 */
	@Name("parts")
	List<UIPart> getParts();

	/**
	 * Base interface for parts of the UI.
	 */
	@Abstract
	@Deprecated
	interface UIPart extends ConfigPart {
		// Pure base interface.
	}

	/**
	 * Contents allowed in {@link GroupPart}s.
	 * 
	 * @see GroupPart#getParts()
	 */
	@Abstract
	@Deprecated
	interface GroupContentPart extends UIPart {
		// Pure base interface.
	}

	/**
	 * A group of attributes.
	 */
	@DisplayOrder({ GroupPart.TITLE, GroupPart.PARTS })
	@Deprecated
	interface GroupPart extends UIPart, EditContextPart {

		String PARTS = "parts";

		String TITLE = "title";
		String OWNER = "owner";

		@Hidden
		@Container
		@Name(OWNER)
		DisplayDescription getOwner();

		@Override
		@DerivedRef({ OWNER, DisplayDescription.CONTEXT })
		EditContext getContext();

		@Name(TITLE)
		@Mandatory
		String getTitle();

		/**
		 * The names of the {@link TLStructuredTypePart}s to display.
		 */
		@Name(PARTS)
		List<GroupContentPart> getParts();

	}

	/**
	 * Display of a dynamic table.
	 * 
	 * @see #getRows()
	 */
	@DisplayOrder({
		TablePart.TITLE,
		TablePart.TYPE_SPEC,
		TablePart.ROWS,
		TablePart.COLUMNS,
	})
	@Deprecated
	interface TablePart extends GroupContentPart {

		String TITLE = "title";

		String TYPE = "type";

		String ROWS = "rows";

		String COLUMNS = "columns";

		String TYPE_SPEC = "typeSpec";

		@Name(TITLE)
		String getTitle();

		/**
		 * Name of {@link #getType()}.
		 */
		@Name(TYPE_SPEC)
		@Options(fun = AllTypes.class, mapping = TLModelPartMapping.class)
		@OptionLabels(TLPartScopedResourceProvider.class)
		@ControlProvider(SelectionControlProvider.class)
		@Nullable
		String getTypeSpec();

		/**
		 * The type of objects displayed as table.
		 */
		@Name(TYPE)
		@InstanceFormat
		@Derived(fun = ResolveType.class, args = { @Ref(TYPE_SPEC) })
		@Hidden
		TLClass getType();

		@Deprecated
		class ResolveType extends Function1<TLClass, String> {
			@Override
			public TLClass apply(String name) {
				try {
					TLType result = TLModelUtil.findType(name);
					if (result instanceof TLClass) {
						return (TLClass) result;
					} else {
						return null;
					}
				} catch (TopLogicException ex) {
					return null;
				}
			}
		}

		/**
		 * Expression creating a list of objects edited in the displayed table as rows.
		 */
		@Name(ROWS)
		@ItemDisplay(ItemDisplayType.VALUE)
		Expr getRows();

		/**
		 * The columns to display for the given {@link #getRows()}.
		 */
		@Name(COLUMNS)
		List<ColumnDisplay> getColumns();

		@Deprecated
		interface ColumnDisplay extends NamedConfigMandatory, ConfigPart {

			String TABLE = "table";
			String VISIBILITY = "visibility";

			String TYPE_PART = "typePart";

			@Hidden
			@Name(TABLE)
			@Container
			TablePart getTable();

			/**
			 * Name of the attribute to display in this column.
			 */
			@Override
			@Options(fun = TypeAttributes.class, args = {
				@Ref({ TABLE, TablePart.TYPE }) }, mapping = NamedPartNames.class)
			String getName();

			/**
			 * Resolved {@link #getName()}.
			 */
			@Name(TYPE_PART)
			@InstanceFormat
			@Derived(fun = ResolvePart.class, args = { @Ref({ TABLE, TablePart.TYPE }), @Ref({ NAME_ATTRIBUTE }) })
			@Hidden
			TLStructuredTypePart getPart();

			@Deprecated
			class ResolvePart extends Function2<TLStructuredTypePart, TLClass, String> {

				@Override
				public TLStructuredTypePart apply(TLClass type, String name) {
					if (type == null || StringServices.isEmpty(name)) {
						return null;
					}
					return type.getPart(name);
				}
			}

			@Deprecated
			class TypeAttributes extends Function1<Collection<? extends TLStructuredTypePart>, TLClass> {

				@Override
				public Collection<? extends TLStructuredTypePart> apply(TLClass type) {
					if (type == null) {
						return Collections.emptyList();
					}
					return list(type.getAllParts());
				}
			}

			/**
			 * How the columns (content) {@link FormVisibility}.
			 */
			@Name(VISIBILITY)
			FormVisibility getVisibility();
		}

	}

	/**
	 * A reference to a single displayed attribute.
	 */
	@Deprecated
	interface AttributePart extends GroupContentPart {

		String OWNER = "owner";

		String NAME = "name";

		/**
		 * The context of the attribute display.
		 */
		@Name(OWNER)
		@Hidden
		@Container
		EditContextPart getOwner();

		/**
		 * The name of the displayed attribute.
		 */
		@Name(NAME)
		@Mandatory
		@Options(fun = ParticipantAttributes.class, mapping = NamedPartNames.class, args = {
			@Ref({ OWNER, EditContextPart.CONTEXT, EditContext.BASE_MODEL }) })
		String getName();

		@Hidden
		@Derived(fun = CalculateLabel.class, args = { @Ref({ OWNER, EditContextPart.CONTEXT, EditContext.BASE_MODEL }),
			@Ref(NAME) })
		String getLabel();

		/**
		 * How the attribute is displayed.
		 */
		@Name("visibility")
		FormVisibility getVisibility();

		@Deprecated
		class CalculateLabel extends Function2<String, TLObject, String> {

			@Override
			public String apply(TLObject baseModel, String name) {
				TLClass modelType = ParticipantAttributes.modelType(baseModel);
				if (modelType == null) {
					return name;
				}
				TLStructuredTypePart part = modelType.getPart(name);
				if (part == null) {
					return name;
				}
				return MetaLabelProvider.INSTANCE.getLabel(part);
			}

		}

		@Deprecated
		class ParticipantAttributes extends Function1<Iterable<? extends TLStructuredTypePart>, TLObject> {
			@Override
			public Iterable<? extends TLStructuredTypePart> apply(TLObject baseModel) {
				TLClass modelType = modelType(baseModel);
				if (modelType == null) {
					return Collections.emptyList();
				}
				return list(modelType.getAllParts());
			}

			static TLClass modelType(TLObject baseModel) {
				if (baseModel == null) {
					return null;
				}
				if(baseModel instanceof Participant) {
					return (TLClass) ((Participant) baseModel).getModelType();
				}
				
				Process process = ((Node) baseModel).getProcess();
				if (process == null) {
					return null;
				}
				Participant participant = process.getParticipant();
				if (participant == null) {
					return null;
				}
				return (TLClass) participant.getModelType();
			}
		}

		@Deprecated
		class NamedPartNames implements OptionMapping {

			@Override
			public Object toSelection(Object option) {
				if (option == null) {
					return null;
				}
				return ((TLNamedPart) option).getName();
			}

			@Override
			public Object asOption(Iterable<?> allOptions, Object selection) {
				return find(allOptions, (String) selection);
			}

			private Object find(Iterable<?> allOptions, String selection) {
				for (Object entry : allOptions) {
					if (entry instanceof TLNamedPart) {
						if (selection.equals(((TLNamedPart) entry).getName())) {
							return entry;
						}
					}
				}
				return null;
			}
			
		}

	}

}
