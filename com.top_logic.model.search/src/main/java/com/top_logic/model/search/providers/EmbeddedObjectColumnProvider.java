/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

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
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormField;
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
	 * The companion object of a row while its embedded columns are being edited.
	 *
	 * <p>
	 * A transient companion created on demand must stay the same instance across all embedded
	 * columns of a row, so that edits accumulate on a single object. Keyed by the row's form
	 * overlay, which is stable while the row is edited and is discarded (and garbage collected)
	 * afterwards.
	 * </p>
	 */
	private final Map<TLObject, TLObject> _companionByRow = new WeakHashMap<>();

	/**
	 * Row overlays for which the companion anchor operation is already installed, ensuring it runs
	 * at most once per row regardless of the number of embedded columns.
	 */
	private final Set<TLObject> _anchorInstalled = Collections.newSetFromMap(new WeakHashMap<TLObject, Boolean>());

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
	 * Looks up the companion object for the given row by evaluating the configured
	 * {@link Config#getObject() object function}.
	 *
	 * @return The companion object, or <code>null</code> if the function yields no object.
	 */
	TLObject lookupCompanion(TLObject row) {
		Object result = _object.execute(row, _component.getModel());
		return result instanceof TLObject ? (TLObject) result : null;
	}

	/**
	 * Looks up the companion for a row that is being edited, keeping the same instance for all
	 * embedded columns of that row.
	 *
	 * <p>
	 * The row's {@link TLFormObject form overlay} is used both as the stable cache key and as the
	 * input to the {@link Config#getObject() object function}, so that the lookup sees the current
	 * form state (e.g. a reference just set in the form) instead of only the persisted value.
	 * </p>
	 */
	private TLObject editCompanion(TLFormObject rowOverlay) {
		TLObject companion = _companionByRow.get(rowOverlay);
		if (companion == null) {
			companion = lookupCompanion(rowOverlay);
			if (companion != null) {
				_companionByRow.put(rowOverlay, companion);
			}
		}
		return companion;
	}

	/**
	 * Persists a transient companion and links it to its row when the row's form is stored.
	 *
	 * <p>
	 * Runs as a deferred {@link Modification} at the end of the form store, i.e. after the row has
	 * been created (for a new row) and after the companion's edited values have been written to it.
	 * The configured {@link Config#getAnchor() anchor operation} creates a persistent representation
	 * of the companion and links it to the (now persistent) row. Nothing is done when the companion
	 * was not actually edited, so that leaving the embedded columns untouched does not create an
	 * empty companion.
	 * </p>
	 */
	private void anchorCompanion(TLFormObject rowOverlay, TLFormObject companionOverlay, TLObject companion) {
		_companionByRow.remove(rowOverlay);
		_anchorInstalled.remove(rowOverlay);

		if (!companion.tTransient()) {
			return;
		}
		if (!isEdited(companionOverlay)) {
			return;
		}
		TLObject row = rowOverlay.getEditedObject();
		if (row == null) {
			return;
		}
		_anchor.execute(row, companion, _component.getModel());
	}

	private static boolean isEdited(TLFormObject companionOverlay) {
		for (AttributeUpdate update : companionOverlay.getUpdates()) {
			FormMember field = update.getField();
			if (field instanceof FormField input && input.isChanged()) {
				return true;
			}
		}
		return false;
	}

	private final class CompanionAccessor implements Accessor<Object> {

		private final TLStructuredTypePart _attribute;

		CompanionAccessor(TLStructuredTypePart attribute) {
			_attribute = attribute;
		}

		@Override
		public Object getValue(Object row, String property) {
			TLObject companion = row instanceof TLObject ? lookupCompanion((TLObject) row) : null;
			return companion == null ? null : companion.tValue(_attribute);
		}

		@Override
		public void setValue(Object row, String property, Object value) {
			/* The embedded columns are edited through the companion's own AttributeUpdate and
			 * persisted when the row's form is stored (see CompanionFieldProvider). Storing the cell
			 * value through the row accessor is neither possible nor necessary. */
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
			if (!(row instanceof TLFormObject)) {
				/* Fields are only created in an editable form context, where the grid passes the
				 * row's form overlay. */
				return null;
			}
			TLFormObject rowOverlay = (TLFormObject) row;

			TLObject companion = editCompanion(rowOverlay);
			if (companion == null) {
				return null;
			}

			boolean disabled = companion.tTransient() && _anchor == null;

			AttributeUpdateContainer container = rowOverlay.getScope();
			TLFormObject companionOverlay = container.editObject(companion);
			AttributeUpdate update = companionOverlay.newEditUpdateDefault(_attribute, disabled);

			if (companion.tTransient() && _anchor != null && _anchorInstalled.add(rowOverlay)) {
				installAnchor(update, rowOverlay, companionOverlay, companion);
			}

			return AttributeFormFactory.getInstance().toFormMember(update, container, columnName);
		}
	}

	/**
	 * Installs a {@link AttributeUpdate.StoreAlgorithm} that, once the given update has been stored,
	 * persists and links the transient companion through {@link #anchorCompanion}.
	 *
	 * <p>
	 * Tying the companion persistence to the form store makes it run exactly once per row for both
	 * edited and newly created rows (both go through
	 * {@link com.top_logic.element.meta.form.AttributeFormContext#store()}), inside the store
	 * transaction, and after the row and the companion values have been written.
	 * </p>
	 */
	private void installAnchor(AttributeUpdate update, TLFormObject rowOverlay, TLFormObject companionOverlay,
			TLObject companion) {
		update.setStoreAlgorithm(new AttributeUpdate.DefaultStorageAlgorithm() {
			@Override
			public Modification store(AttributeUpdate storedUpdate) {
				Modification result = super.store(storedUpdate);
				return result.andThen(() -> anchorCompanion(rowOverlay, companionOverlay, companion));
			}
		});
	}

}
