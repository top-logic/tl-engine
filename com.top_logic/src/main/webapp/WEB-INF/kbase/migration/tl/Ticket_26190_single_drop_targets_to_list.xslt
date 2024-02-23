<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>

	<!-- rename tableDrop to dropTarget and wrap it into dropTargets -->
	<xsl:template match="arguments/tableDrop">
		<dropTargets>
			<dropTarget>
				<xsl:apply-templates select="node()|@*"/>
			</dropTarget>
		</dropTargets>
	</xsl:template>
	
	<!-- rename treeDrop to dropTarget and wrap it into dropTargets -->
	<xsl:template match="arguments/treeDrop">
		<dropTargets>
			<dropTarget>
				<xsl:apply-templates select="node()|@*"/>
			</dropTarget>
		</dropTargets>
	</xsl:template>

	<!-- Standard copy template. -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>