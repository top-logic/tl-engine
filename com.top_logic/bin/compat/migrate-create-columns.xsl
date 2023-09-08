<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="*/column"></xsl:template>
	
	<xsl:template match="*[column]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates />
			<xsl:text>&#9;</xsl:text><columns>
					<xsl:for-each select="column">
			<!--
			Duplicates all texts, eventually also aliases in aliase files 
			<xsl:value-of select="preceding-sibling::text()[1]"/> 
			-->
					<xsl:text>&#9;</xsl:text><xsl:apply-templates select="."  mode="column"/>
					</xsl:for-each>
			<!--
			Duplicates all texts, eventually also aliases in aliase files 
			<xsl:value-of select="preceding-sibling::text()[1]"/> 
			-->
			<xsl:text>&#9;</xsl:text>
			</columns>
			<!--
			Duplicates all texts, eventually also aliases in aliase files 
			<xsl:value-of select="preceding-sibling::text()[1]"/> 
			-->
		</xsl:copy>	
	</xsl:template>
	
			<!--
			Duplicates all texts, eventually also aliases in aliase files 
	<xsl:template match="text()[local-name(following-sibling::node()[1]) = 'column']" />
			-->
	
	<xsl:template match="column"></xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()" mode="column">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="column"/>
			<xsl:apply-templates mode="column"/>
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