<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="columns">
		<table>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</table>
	</xsl:template>
	
	<xsl:template match="columnDescription">
		<column>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</column>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>