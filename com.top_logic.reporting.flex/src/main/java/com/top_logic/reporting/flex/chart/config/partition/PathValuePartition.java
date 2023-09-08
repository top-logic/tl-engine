/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.partition;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.reporting.flex.chart.config.model.Partition;
import com.top_logic.reporting.flex.chart.config.util.MetaAttributeProvider;
import com.top_logic.reporting.flex.search.chart.GenericModelPreparationBuilder.PartitionOptions;
import com.top_logic.util.Utils;

/**
 * {@link PartitionFunction} that partitions by single-valued attributes.
 * 
 * @see MultiValuePartition
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class PathValuePartition extends SingleValuePartition {

	public static class PathValueProvider implements ValueProvider {

		private String _path;

		public PathValueProvider(String path) {
			_path = path;
		}

		@Override
		public Object getValue(TLObject att, AbstractAttributeBasedPartition function) {
			return Utils.getValueByPath(_path, att);
		}
	}

	public static class PathOptions extends Function1<List<PartitionFunction.Config>, MetaAttributeProvider> {
		@Override
		public List<PartitionFunction.Config> apply(MetaAttributeProvider arg) {
			TLStructuredTypePart ma = arg.getMetaAttribute();
			TLType type = ma.getType();
			return PartitionOptions.getPartitionConfigOptions(TLModelUtil.getConcreteSpecializations((TLClass) type));
		}
	}

	/**
	 * Config-interface for {@link PathValuePartition}.
	 */
	public interface Config extends SingleValuePartition.Config {

		@Override
		@ClassDefault(PathValuePartition.class)
		public Class<? extends PathValuePartition> getImplementationClass();

		@Options(fun = PathOptions.class, args = @Ref({ META_ATTRIBUTE }))
		PartitionFunction.Config getNextPartition();

	}

	public PathValuePartition(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public List<Partition> createPartitions(Partition aParent) {
		List<AbstractAttributeBasedPartition.Config> partitions = new ArrayList<>();
		collectPartitionFunctions(partitions, getConfig());
		if (partitions.size() > 1) {
			String path = createPath(partitions);
			PathValueProvider provider = new PathValueProvider(path);
			AbstractAttributeBasedPartition.Config last = CollectionUtil.getLast(partitions);
			last.setValueProvider(provider);
			PartitionFunction partition = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(last);
			return partition.createPartitions(aParent);
		}
		return super.createPartitions(aParent);
	}

	private List<Object> mapObjects(List<Object> objects, String path) {
		List<Object> res = new ArrayList<>();
		for (Object object : objects) {
			res.add(Utils.getValueByPath(path, (TLObject) object));
		}
		return res;
	}

	private String createPath(List<AbstractAttributeBasedPartition.Config> partitions) {
		StringBuilder res = new StringBuilder();
		int size = partitions.size();
		for (int i = 0; i < size; i++) {
			String name = partitions.get(i).getMetaAttribute().getMetaAttributeName();
			res.append(name);
			res.append(".");
			
		}
		res.setLength(res.length() - 1);
		return res.toString();
	}

	private AbstractAttributeBasedPartition.Config next(Config config) {
		return (AbstractAttributeBasedPartition.Config) config.getNextPartition();
	}

	private void collectPartitionFunctions(List<AbstractAttributeBasedPartition.Config> partitions, AbstractAttributeBasedPartition.Config config) {
		if (config != null) {
			partitions.add(config);
			if (config instanceof Config) {
				collectPartitionFunctions(partitions, next((Config) config));
			}
		}
	}

}