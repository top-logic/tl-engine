<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>

<!-- 
				<action class="com.top_logic.layout.scripting.action.FormInput$Op"
					user-id="root"
				>
					<field config:interface="com.top_logic.layout.form.FuzzyFormMemberNaming$Name"
						label="ID"
					>
						<component config:interface="com.top_logic.layout.scripting.recorder.ref.ui.FuzzyComponentNaming$Name"
							name="com.top_logic.element/admin/model/modelEditor/typePartEditor.layout.xml#createPropertyTypePartForm"
							tab-path=""
						/>
					</field>
					<value config:interface="com.top_logic.layout.scripting.recorder.ref.value.StringNaming$Name"
						value="canBeDeleted"
					/>
				</action>
 -->
 
	<xsl:template match="action[@class='com.top_logic.layout.scripting.action.FormInput$Op' and starts-with(field/component/@name, 'com.top_logic.element/admin/model/modelEditor/typePartEditor.layout.xml#') and field/@label='ID']">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
		
		<action class="com.top_logic.layout.scripting.action.FormInput$Op"
			user-id="root"
		>
			<field config:interface="com.top_logic.layout.scripting.recorder.ref.ui.form.DefaultFormMemberNaming$Name">
				<form config:interface="com.top_logic.layout.scripting.recorder.ref.ui.FuzzyComponentNaming$Name"
					name="{field/component/@name}"
					tab-path=""
				/>
				<path>
					<labeled-member label="Konfiguration"/>
					<named-member name="contentContainer"/>
					<named-member name="content"/>
					<labeled-member label="Bezeichnung"/>
					<labeled-member label="Deutsch"/>
				</path>
			</field>
			<value config:interface="com.top_logic.layout.scripting.recorder.ref.value.StringNaming$Name"
				value="{value/@value}"
			/>
		</action>
		<action class="com.top_logic.layout.scripting.action.FormInput$Op"
			user-id="root"
		>
			<field config:interface="com.top_logic.layout.scripting.recorder.ref.ui.form.DefaultFormMemberNaming$Name">
				<form config:interface="com.top_logic.layout.scripting.recorder.ref.ui.FuzzyComponentNaming$Name"
					name="{field/component/@name}"
					tab-path=""
				/>
				<path>
					<labeled-member label="Konfiguration"/>
					<named-member name="contentContainer"/>
					<named-member name="content"/>
					<labeled-member label="Bezeichnung"/>
					<labeled-member label="Englisch"/>
				</path>
			</field>
			<value config:interface="com.top_logic.layout.scripting.recorder.ref.value.StringNaming$Name"
				value="{value/@value}"
			/>
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