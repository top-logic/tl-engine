/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.UUIDInitializer;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.filter.AllCellsExist;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.ColumnContainer;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.layout.table.provider.generic.ColumnInfoFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.AllClasses;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TypePartContext;

/**
 * {@link TableConfigurationProvider} that embeds attributes of a related object into a table's rows.
 *
 * <p>
 * For each row, a companion object is looked up. Selected attributes of that companion object are
 * displayed (and, in a grid, edited) as additional columns. Since the columns represent real model
 * attributes, their display, filtering, editing widget, and constraints are derived from the model.
 * </p>
 *
 * <p>
 * The companion object may be created on demand: if the lookup returns a transient object, the
 * anchor operation persists it and links it to its row when the row is saved.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 *
 * @see DynamicColumnProviderByExpression
 */
@InApp
@Label("Embedded object columns")
public class EmbeddedObjectColumnProvider
		extends AbstractConfiguredInstance<EmbeddedObjectColumnProvider.Config<?>>
		implements TableConfigurationProvider {

	/**
	 * Configuration options for {@link EmbeddedObjectColumnProvider}.
	 */
	@DisplayOrder({
		Config.OBJECT,
		Config.TYPE,
		Config.ATTRIBUTES,
		Config.GROUP_LABEL,
		Config.COLUMN_VISIBILITY,
		Config.ANCHOR,
	})
	public interface Config<I extends EmbeddedObjectColumnProvider> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getIdPrefix()
		 */
		String ID_PREFIX = "idPrefix";

		/**
		 * @see #getObject()
		 */
		String OBJECT = "object";

		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * @see #getAttributes()
		 */
		String ATTRIBUTES = "attributes";

		/**
		 * @see #getGroupLabel()
		 */
		String GROUP_LABEL = "group-label";

		/**
		 * @see #getColumnVisibility()
		 */
		String COLUMN_VISIBILITY = "columnVisibility";

		/**
		 * @see #getAnchor()
		 */
		String ANCHOR = "anchor";

		/**
		 * Common technical prefix that is added to each technical column name of all columns created
		 * by this provider.
		 *
		 * @implNote The value is only relevant to construct technical identifiers for columns that
		 *           serve as anchor for storing personalization information.
		 */
		@Name(ID_PREFIX)
		@Hidden
		@ValueInitializer(UUIDInitializer.class)
		@Mandatory
		String getIdPrefix();

		/**
		 * Function computing the companion object whose attributes are embedded into a row.
		 *
		 * <p>
		 * The function expects the row object as first argument and the component's model as optional
		 * second argument.
		 * </p>
		 *
		 * <pre>
		 * <code>row -> model -> $row.get(`my.module:MyType#companion`)</code>
		 * </pre>
		 *
		 * <p>
		 * The result is the object whose attributes are shown in the embedded columns for the given
		 * row. If the function returns nothing, the embedded cells of that row stay empty.
		 * </p>
		 *
		 * <p>
		 * The function may return a newly created transient object (e.g. via
		 * <code>new(`my.module:MyType`, transient: true)</code>) when no companion exists yet. Such a
		 * transient companion is persisted by the anchor operation when its row is saved.
		 * </p>
		 */
		@Name(OBJECT)
		@Mandatory
		Expr getObject();

		/**
		 * The type of the companion object.
		 *
		 * <p>
		 * The attributes selectable for embedding are the attributes of this type.
		 * </p>
		 */
		@Name(TYPE)
		@Mandatory
		@Options(fun = AllClasses.class, mapping = TLModelPartRef.PartMapping.class)
		TLModelPartRef getType();

		/**
		 * The attributes of the companion type to embed as columns.
		 *
		 * <p>
		 * If no attributes are selected, all visible attributes of the companion type are embedded.
		 * </p>
		 */
		@Name(ATTRIBUTES)
		@Options(fun = AttributesOfType.class, args = @Ref(TYPE), mapping = TLModelPartRef.PartMapping.class)
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		List<TLModelPartRef> getAttributes();

		/**
		 * Label of the group the embedded columns are sorted into.
		 *
		 * <p>
		 * When a group label is set, all embedded columns are sorted into a group with this name.
		 * Configuring this provider multiple times yields multiple such groups, each with the same
		 * attribute columns but a different companion lookup.
		 * </p>
		 */
		@Name(GROUP_LABEL)
		@DisplayMinimized
		ResKey getGroupLabel();

		/**
		 * The visibility of the created columns.
		 */
		@Name(COLUMN_VISIBILITY)
		DisplayMode getColumnVisibility();

		/**
		 * Optional operation persisting a transient companion object and linking it to its row.
		 *
		 * <p>
		 * The operation expects the row object as first argument, the companion object as second
		 * argument, and the component's model as optional third argument.
		 * </p>
		 *
		 * <pre>
		 * <code>row -> companion -> model -> $row.set(`my.module:MyType#companion`, copy($companion, transient: false, context: $row))</code>
		 * </pre>
		 *
		 * <p>
		 * The operation is executed when an edited row is saved and its companion object is
		 * transient. It is expected to create a persistent representation of the companion and link it
		 * to the row. If no operation is given, transient companion objects cannot be edited.
		 * </p>
		 */
		@Name(ANCHOR)
		Expr getAnchor();

		/**
		 * Option provider for {@link #getAttributes()}.
		 */
		class AttributesOfType extends Function1<List<? extends TLStructuredTypePart>, TLModelPartRef> {
			@Override
			public List<? extends TLStructuredTypePart> apply(TLModelPartRef arg) {
				if (arg == null) {
					return Collections.emptyList();
				}
				try {
					return arg.resolveClass().getAllParts();
				} catch (ConfigurationException ex) {
					return Collections.emptyList();
				}
			}
		}
	}

	private LayoutComponent _component;

	private final QueryExecutor _object;

	private final QueryExecutor _anchor;

	private final List<TLStructuredTypePart> _attributes;

	/**
	 * The companion object of a row while it is being edited.
	 *
	 * <p>
	 * A transient companion created on demand must stay the same instance from field creation until
	 * it is persisted on save, so that edits accumulate on it. The entry is dropped once the
	 * companion has been anchored.
	 * </p>
	 */
	private final Map<Object, TLObject> _companionByRow = new HashMap<>();

	/**
	 * Creates a {@link EmbeddedObjectColumnProvider} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EmbeddedObjectColumnProvider(InstantiationContext context, Config<?> config) throws ConfigurationException {
		super(context, config);

		context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class, c -> _component = c);
		_object = QueryExecutor.compile(config.getObject());
		_anchor = QueryExecutor.compileOptional(config.getAnchor());
		_attributes = resolveAttributes(config);
	}

	private static List<TLStructuredTypePart> resolveAttributes(Config<?> config) throws ConfigurationException {
		List<TLModelPartRef> configured = config.getAttributes();
		if (!configured.isEmpty()) {
			List<TLStructuredTypePart> result = new ArrayList<>(configured.size());
			for (TLModelPartRef ref : configured) {
				result.add((TLStructuredTypePart) ref.resolvePart());
			}
			return result;
		}

		List<TLStructuredTypePart> result = new ArrayList<>();
		for (TLStructuredTypePart part : config.getType().resolveClass().getAllParts()) {
			if (!DisplayAnnotations.isHidden(part)) {
				result.add(part);
			}
		}
		return result;
	}

	@Override
	public void adaptConfigurationTo(TableConfiguration table) {
		DisplayMode displayMode = getConfig().getColumnVisibility();

		ColumnContainer<ColumnConfiguration> container = withColumnGroup() ? createColumnGroup(table) : table;

		List<String> columnNames = new ArrayList<>();
		for (TLStructuredTypePart attribute : _attributes) {
			String columnName = columnName(attribute.getName());
			columnNames.add(columnName);

			ColumnConfiguration column = container.declareColumn(columnName);

			ResKey labelKey = ResKey.text(MetaLabelProvider.INSTANCE.getLabel(attribute));
			ColumnInfo columnInfo = new ColumnInfoFactory().createColumnInfo(new TypePartContext(attribute), labelKey);
			columnInfo.setVisibility(displayMode);
			columnInfo.adapt(column);

			/* The default WrapperValueExistenceTester assumes the row object carries the attribute the
			 * column is built for. Here the attribute lives on the companion object, so all cells
			 * exist. */
			column.setCellExistenceTester(AllCellsExist.INSTANCE);

			column.setAccessor(new CompanionAccessor(attribute));
			column.setFieldProvider(new CompanionFieldProvider(attribute));
		}

		if (displayMode.isDisplayed()) {
			columnNames.removeAll(table.getDefaultColumns());
			table.setDefaultColumns(CollectionUtil.concat(table.getDefaultColumns(), columnNames));
		}
	}

	private boolean withColumnGroup() {
		return getConfig().getGroupLabel() != null;
	}

	private ColumnConfiguration createColumnGroup(TableConfiguration table) {
		ColumnConfiguration groupColumn = table.declareColumn(columnName("group"));
		groupColumn.setColumnLabelKey(getConfig().getGroupLabel());
		return groupColumn;
	}

	private String columnName(String suffix) {
		return getConfig().getIdPrefix() + "-" + suffix;
	}

	/**
	 * Looks up the companion object for the given row without remembering a freshly created one.
	 *
	 * <p>
	 * Used for read-only display. An object being edited is served from {@link #_companionByRow} so
	 * that it stays stable across the edit; all other rows resolve their companion afresh.
	 * </p>
	 */
	TLObject displayCompanion(Object row) {
		TLObject editedCompanion = _companionByRow.get(row);
		if (editedCompanion != null) {
			return editedCompanion;
		}
		return lookupCompanion(row);
	}

	/**
	 * Looks up the companion object for the given row and remembers it for the duration of the edit.
	 */
	TLObject editCompanion(Object row) {
		TLObject companion = _companionByRow.get(row);
		if (companion == null) {
			companion = lookupCompanion(row);
			if (companion != null) {
				_companionByRow.put(row, companion);
			}
		}
		return companion;
	}

	private TLObject lookupCompanion(Object row) {
		Object result = _object.execute(row, _component.getModel());
		return result instanceof TLObject ? (TLObject) result : null;
	}

	private AttributeUpdateContainer updateContainer() {
		return ((AttributeFormContext) ((FormHandler) _component).getFormContext()).getAttributeUpdateContainer();
	}

	/**
	 * Persists the companion of the given row if it is transient and the row was edited.
	 *
	 * <p>
	 * Runs on save, after the companion's edited values have been written back (in memory for a
	 * transient companion). The anchor operation is expected to create a persistent copy and link it
	 * to the row. The remembered companion is dropped so that a possible second call for another
	 * edited column of the same row does nothing and a subsequent lookup resolves the now-linked
	 * persistent object.
	 * </p>
	 */
	void anchor(Object row) {
		TLObject companion = _companionByRow.get(row);
		if (companion == null) {
			return;
		}
		if (companion.tTransient() && _anchor != null) {
			_anchor.execute(row, companion, _component.getModel());
		}
		_companionByRow.remove(row);
	}

	private final class CompanionAccessor implements Accessor<Object> {

		private final TLStructuredTypePart _attribute;

		CompanionAccessor(TLStructuredTypePart attribute) {
			_attribute = attribute;
		}

		@Override
		public Object getValue(Object row, String property) {
			TLObject companion = displayCompanion(row);
			return companion == null ? null : companion.tValue(_attribute);
		}

		@Override
		public void setValue(Object row, String property, Object value) {
			/* The value has already been stored on the companion through its AttributeUpdate when the
			 * form context was stored. A transient companion additionally needs to be persisted and
			 * linked to its row. */
			anchor(row);
		}
	}

	private final class CompanionFieldProvider extends AbstractFieldProvider {

		private final TLStructuredTypePart _attribute;

		CompanionFieldProvider(TLStructuredTypePart attribute) {
			_attribute = attribute;
		}

		@Override
		public FormMember createField(Object row, Accessor accessor, String columnName) {
			if (_attribute.isDerived()) {
				return null;
			}

			TLObject companion = editCompanion(row);
			if (companion == null) {
				return null;
			}

			boolean disabled = companion.tTransient() && _anchor == null;

			AttributeUpdateContainer container = updateContainer();
			TLFormObject overlay = container.editObject(companion);
			AttributeUpdate update = overlay.newEditUpdateDefault(_attribute, disabled);
			return AttributeFormFactory.getInstance().toFormMember(update, container, columnName);
		}
	}

}
