<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns="http://www.top-logic.com/ns/dynamic-types/6.0"
	exclude-result-prefixes="ext" 
	xmlns:ext="xalan://com.top_logic.element.config.ConfigTypeResolver"
>

	<xsl:template match="list">
		<xsl:element name="enum">
			<xsl:attribute name="name">
				<xsl:value-of select="@name"/>
			</xsl:attribute>
			
			<xsl:variable name="hasTypeAnnotation" select="@type and @type != 'Classification'"/>
			<xsl:variable name="hasMultiSelectAnnotation" select="@multiselect and @multiselect != 'false'"/>
			<xsl:variable name="hasSystemAnnotation" select="@system and @system != 'false'"/>
			<xsl:variable name="hasUnorderedAnnotation" select="@ordered and @ordered = 'false'"/>
			
			<xsl:if test="$hasTypeAnnotation or $hasMultiSelectAnnotation or $hasSystemAnnotation or $hasUnorderedAnnotation">
				<annotations xmlns:config="http://www.top-logic.com/ns/config/6.0">
					<xsl:if test="$hasTypeAnnotation">
						<annotation 
							config:interface="com.top_logic.model.config.annotation.EnumScope" value="{@type}"/>
					</xsl:if>
					<xsl:if test="$hasMultiSelectAnnotation">
						<annotation 
							config:interface="com.top_logic.model.config.annotation.MultiSelect" value="{@multiselect}"/>
					</xsl:if>
					<xsl:if test="$hasSystemAnnotation">
						<annotation 
							config:interface="com.top_logic.model.config.annotation.SystemEnum" value="{@system}"/>
					</xsl:if>
					<xsl:if test="$hasUnorderedAnnotation">
						<annotation config:interface="com.top_logic.model.config.annotation.UnorderedEnum" />
					</xsl:if>
				</annotations>
			</xsl:if>
			
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="elements">
		<xsl:element name="classifiers">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="element">
		<xsl:element name="classifier">
			<xsl:apply-templates select="@*"/>
		</xsl:element>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="/">
		<xsl:text>&#10;</xsl:text>
		<xsl:apply-templates select="child::node()"/>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	

</xsl:stylesheet>