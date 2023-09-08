<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="action[@class='com.top_logic.layout.scripting.runtime.action.CommandActionOp' and @command-name='displayDialog_DemoTypesnewElementLayoutDialog' and @component-name='DemoTypesEdit']">
		<action class="com.top_logic.layout.scripting.runtime.action.CommandActionOp"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  command-implementation-comment="com.top_logic.tool.boundsec.OpenModalDialogCommandHandler"
		  command-label="Knoten anlegen"
		  command-name="openNewElementDialog"
		  component-implementation-comment="com.top_logic.layout.tree.component.TreeComponent"
		  component-name="DemoTypesTree"
		>
		  <arguments/>
		</action>
	</xsl:template>

	<xsl:template match="action[@class='com.top_logic.layout.scripting.runtime.action.CommandActionOp' and @command-name='displayDialog_DemoSecuritynewElementLayoutDialog' and @component-name='DemoSecurityEdit']">
		<action class="com.top_logic.layout.scripting.runtime.action.CommandActionOp"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  command-implementation-comment="com.top_logic.tool.boundsec.OpenModalDialogCommandHandler"
		  command-label="Neu..."
		  command-name="openCreateDialog"
		  component-implementation-comment="com.top_logic.element.layout.structured.AdminElementComponent"
		  component-name="DemoSecurityEdit"
		>
		  <arguments/>
		</action>
	</xsl:template>
	
	<xsl:template match="action[@class='com.top_logic.layout.scripting.runtime.action.CommandActionOp' and @command-name='displayDialog_tl_demo_aspectnewElementLayoutDialog' and @component-name='tl.demo.aspectEdit']">
		<action class="com.top_logic.layout.scripting.runtime.action.CommandActionOp"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  command-implementation-comment="com.top_logic.tool.boundsec.OpenModalDialogCommandHandler"
		  command-label="Neu..."
		  command-name="openCreateDialog"
		  component-implementation-comment="com.top_logic.element.layout.structured.AdminElementComponent"
		  component-name="tl.demo.aspectEdit"
		>
		  <arguments/>
		</action>
	</xsl:template>

	<xsl:template match="
		action[
			@class='com.top_logic.layout.scripting.runtime.action.SelectActionOp' and 
			selection/selectee/@config:interface='com.top_logic.layout.scripting.recorder.ref.value.object.CompactLabelPath' and
			selection/selectee/@label-path='' and
			selection-model-name/@config:interface='com.top_logic.layout.scripting.recorder.ref.ui.FuzzyComponentNaming$Name' and
			selection-model-name/@name='tl.demo.aspectTree'
		]
	">
		<action class="com.top_logic.layout.scripting.runtime.action.SelectActionOp"
		  xmlns:config="http://www.top-logic.com/ns/config/6.0"
		  change-kind="ABSOLUTE"
		  user-id="root"
		>
		  <selection>
		    <selectee config:interface="com.top_logic.layout.scripting.recorder.ref.value.ListValue">
		      <list/>
		    </selectee>
		    <selection-state boolean="true"/>
		  </selection>
		  <selection-model-name config:interface="com.top_logic.layout.tree.TreeDataName">
		    <tree-data-owner config:interface="com.top_logic.layout.scripting.recorder.ref.ui.FuzzyComponentNaming$Name"
		      name="tl.demo.aspectTree"
		      tab-path="Strukturen > Aspektvererbung"
		    />
		  </selection-model-name>
		</action>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>