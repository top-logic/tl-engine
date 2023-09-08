<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	exclude-result-prefixes="ext" 
	xmlns=""
	xmlns:ext="xalan://com.top_logic.element.config.ConfigTypeResolver"
>
	<xsl:template match="element/attributes">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="element/attributes/attribute">
		<xsl:element name="{@name}">
			<xsl:value-of select="@value"/>		
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="metaelement/attributes">
	</xsl:template>
	
	<xsl:template match="structure">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<elements>
				<xsl:apply-templates select="child::element"/>
			</elements>
			<xsl:apply-templates select="child::node()[not(self::element)]"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="metaelementdefinition[metaelement]">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<metaelements>
				<xsl:apply-templates select="child::metaelement"/>
			</metaelements>
			<xsl:apply-templates select="child::node()[not(self::metaelement)]"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="metaattribute">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			
			<xsl:if test="@additionalParam">
				<xsl:call-template name="toAttributes">
					<xsl:with-param name="keyValues" select="@additionalParam"/>
				</xsl:call-template>
			</xsl:if>
			
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="toAttributes">
		<xsl:param name="keyValues"/>
		
		<xsl:if test="string-length($keyValues) &gt; 0">
			<xsl:choose>
				<xsl:when test="contains($keyValues, ';')">
					<xsl:call-template name="toAttribute">
						<xsl:with-param name="keyValue" select="substring-before($keyValues, ';')"/>
					</xsl:call-template>
					<xsl:call-template name="toAttributes">
						<xsl:with-param name="keyValues" select="substring-after($keyValues, ';')"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="toAttribute">
						<xsl:with-param name="keyValue" select="$keyValues"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="toAttribute">
		<xsl:param name="keyValue"/>
		<xsl:variable name="oldName" select="substring-before($keyValue, ':')"/>
		<xsl:variable name="name">
			<xsl:call-template name="convertOptionName">
				<xsl:with-param name="name" select="$oldName"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="value">
			<xsl:call-template name="convertOptionValue">
				<xsl:with-param name="oldName" select="$oldName"/>
				<xsl:with-param name="value" select="substring-after($keyValue, ':')"/>
			</xsl:call-template>
		</xsl:variable>
		
		<xsl:if test="string-length($name) &gt; 0">
			<xsl:attribute name="{$name}">
				<xsl:choose>
					<xsl:when test="$name = 'calculatedType'">
						<xsl:value-of select="ext:normalizeImplementationType($value)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$value"/>		
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="convertOptionName">
		<xsl:param name="name"/>
		
		<xsl:choose>
			<xsl:when test="$name = 'selection'">
				<xsl:value-of select="'multiple'"/>
			</xsl:when>
			
			<xsl:when test="$name = 'sorted'">
				<xsl:value-of select="'ordered'"/>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$name"/>
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:template>
	
	<xsl:template name="convertOptionValue">
		<xsl:param name="oldName"/>
		<xsl:param name="value"/>
		
		<xsl:choose>
			<xsl:when test="$oldName = 'selection'">
				<xsl:choose>
					<xsl:when test="$value = 'multi'">
						<xsl:value-of select="'true'"/>
					</xsl:when>
					
					<xsl:when test="$value = 'single'">
						<xsl:value-of select="'false'"/>
					</xsl:when>
				
					<xsl:otherwise>
						<xsl:value-of select="$value"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			
			<xsl:otherwise>
				<xsl:value-of select="$value"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="metaattribute/attribute::additionalParam">
		<!-- Drop additionalParam attribute. -->
	</xsl:template>
	
	<xsl:template match="metaattribute/attribute::type">
		<xsl:attribute name="impl">
			<xsl:value-of select="ext:normalizeImplementationType(.)"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="//structuredElements/metaelementdefinition/@holderType">
		<!-- Drop common senseless attribute in top-level definitions. -->
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