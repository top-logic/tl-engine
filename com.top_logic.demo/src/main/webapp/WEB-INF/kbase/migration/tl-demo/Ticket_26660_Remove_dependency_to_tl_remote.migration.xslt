<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
<!-- 
	<xsl:template match="/config/module[@name='Ticket']">
	</xsl:template>
	<xsl:template match="/config/module[@name='DemoRemote']">
	</xsl:template>
 -->
	
	<xsl:template match="@type[string(.)='Ticket:ExternalReference']">
		<xsl:attribute name="type">
			<xsl:value-of select="'tl.model:TLObject'"/>
		</xsl:attribute>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>