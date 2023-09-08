<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="reference[@type='tl.folder:WebFolder'][not(annotations/storage-algorithm)]/@composite">
		<!-- Delete, if it already exists. -->
	</xsl:template>

	<xsl:template match="reference[@type='tl.folder:WebFolder'][not(annotations/storage-algorithm)]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:attribute name="composite">
				<xsl:value-of select="'true'"/>
			</xsl:attribute>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>