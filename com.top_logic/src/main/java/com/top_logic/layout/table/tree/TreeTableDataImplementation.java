package com.top_logic.layout.table.tree;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.structure.DefaultExpandable;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.toolbar.DefaultToolBar;

/**
 * Default implementation for a given {@link TreeTableData} proxy.
 */
public class TreeTableDataImplementation extends DefaultTreeTableData {

	private final TreeTableData _proxy;

	/**
	 * Creates an instance of this class to be used as implementation for the specified object.
	 * 
	 * @param proxy
	 *        the object to create a implementation instance for
	 * @param owner
	 *        the {@link TreeTableData#getOwner() owner} of the new {@link TreeTableData}.
	 * @param configKey
	 *        The key to store personal table configuration with.
	 * @param updateSelectionOnTableEvents
	 *        True if the {@link TreeTableData} updates his selection when table model events are
	 *        sent.
	 */
	public TreeTableDataImplementation(TreeTableData proxy, Maybe<? extends TreeTableDataOwner> owner,
			ConfigKey configKey,
			boolean updateSelectionOnTableEvents) {
		super(owner, configKey, updateSelectionOnTableEvents);

		_proxy = proxy;
		setToolBar(new DefaultToolBar(proxy, new DefaultExpandable()));
	}

	@Override
	protected TableData self() {
		return _proxy;
	}

	@Override
	public boolean isSet(Property<?> property) {
		return _proxy.isSet(property);
	}

	@Override
	public <T> T get(Property<T> property) {
		return _proxy.get(property);
	}

	@Override
	public <T> T set(Property<T> property, T value) {
		return _proxy.set(property, value);
	}

	@Override
	public <T> T reset(Property<T> property) {
		return _proxy.reset(property);
	}
}
