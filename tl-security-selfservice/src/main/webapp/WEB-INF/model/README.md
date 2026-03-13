# Model repository folder

Place custom model definitions here. This folder is necessary to be able to export model
definitions created in-app to the IDE.

The definition for a module `my.module` is  created in a file named `my.module.model.xml`. 
Each model definition must be added to the application configuration in the configuration 
section `com.top_logic.util.model.ModelService` like that:

```
<config service-class="com.top_logic.util.model.ModelService">
   <instance>
      <declarations>
         <declaration file="/WEB-INF/model/my.module.model.xml"/>
      </declarations>
   </instance>
</config>
```

A minimal model definition creating a new module with two trivial classes would be:

`my.module.model.xml`:
```
<model xmlns="http://www.top-logic.com/ns/dynamic-types/6.0">
	<module name="my.module">
		<types>
			<class name="A">
				<attributes>
					<property name="name" type="tl.core:String" mandatory="true"/>
				</attributes>
			</class>
			
			<class name="B">
				<generalizations>
					<generalization type="A"/>
				</generalizations>
				
				<attributes>
					<property name="x" type="tl.core:Integer"/>
					<reference name="others" type="A" multiple="true"/>
				</attributes>
			</class>
			
		</types>
	</module>
</model>
```
