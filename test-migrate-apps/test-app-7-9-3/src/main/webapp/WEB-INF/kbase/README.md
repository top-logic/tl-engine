# Database schema definition folder

Place custom database schema definitions here. This folder is necessary to export database schema
definitions created in-app to the IDE.

By convenience, the schema for the types of an application called `MyApp` is defined in a file named 
`myAppMeta.xml` in this folder. Each schema definition must be added to the application configuration 
in the configuration section `` like that:

```
<config config:interface="com.top_logic.basic.db.schema.setup.config.ApplicationTypes">
	<type-systems>
		<type-system name="Default">
			<declarations>
				<declaration resource="webinf://kbase/myAppMeta.xml" />
			</declarations>
		</type-system>
	</type-systems>
</config>
```

A minimal schema definition creating a single table would be:

`myAppMeta.xml`:
```
<objectlist>
	<metaobject
		object_name="MyTable"
		super_class="TLObject"
	>
		<mo_attribute
			att_name="name"
			att_type="String"
			db_size="255"
		/>
		<mo_attribute
			att_name="x"
			att_type="Integer"
		/>
	</metaobject>
</objectlist>
```
