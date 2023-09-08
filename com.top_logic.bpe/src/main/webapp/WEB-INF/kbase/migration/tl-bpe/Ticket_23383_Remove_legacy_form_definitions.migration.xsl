<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.top-logic.com/ns/config/6.0" xmlns:config="http://www.top-logic.com/ns/config/6.0">

	<xsl:template match="/config">
		<config xmlns:config="http://www.top-logic.com/ns/config/6.0"
			config:interface="com.top_logic.model.form.definition.FormDefinition"
			class="com.top_logic.model.form.implementation.FormDefinitionTemplateProvider">
			<xsl:apply-templates/>
		</config>
	</xsl:template>

	<xsl:template match="part[@config:interface='com.top_logic.bpe.bpml.display.DisplayDescription$AttributePart']">
		<field attribute="{@name}">
			<xsl:if test="boolean(@visibility)">
				<xsl:attribute name="visibility">
					<xsl:value-of select="@visibility"/>
				</xsl:attribute>			
			</xsl:if>
		</field>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>