<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0"
>
	<xsl:template match="@type">
		<xsl:choose>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MailFolderTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.mail:MailFolder'"/></xsl:attribute></xsl:when>
			<xsl:when test="string(.) = 'tl.legacy.tabletypes:MailTable'"><xsl:attribute name="type"><xsl:value-of select="'tl.mail:Mail'"/></xsl:attribute></xsl:when>

			<xsl:otherwise>
				<xsl:copy>
					<xsl:apply-templates select="@*"/>
					<xsl:apply-templates/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>