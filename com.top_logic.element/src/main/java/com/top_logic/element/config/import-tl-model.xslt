<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:m="http://www.top-logic.com/ns/model/6.0"
	exclude-result-prefixes="m ext" 
	xmlns="http://www.top-logic.com/ns/dynamic-types/6.0"
	xmlns:ext="xalan://com.top_logic.element.config.ConfigTypeResolver"
>
	<xsl:template match="m:model">
		<xsl:element name="{local-name(.)}">
			<xsl:apply-templates select="@*"/>
			<modules>
				<xsl:apply-templates/>
			</modules>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="m:module">
		<xsl:element name="{local-name(.)}">
			<xsl:apply-templates select="@*"/>
			<types>
				<xsl:apply-templates/>
			</types>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="m:class">
		<xsl:variable name="interfaceName" select="@name"/>
		<xsl:element name="interface">
			<xsl:attribute name="name">
				<xsl:value-of select="$interfaceName"/>
			</xsl:attribute>
			<xsl:apply-templates select="@*"/>
			
			<generalizations>
				<xsl:apply-templates select="m:extends"/>
			</generalizations>
			
			<attributes>
				<xsl:apply-templates select="child::node()[not(self::m:extends)]"/>
			</attributes>
		</xsl:element>
		
		<xsl:if test="not(@abstract) or @abstract='false'">
			<xsl:element name="class">
				<xsl:attribute name="name">
					<xsl:value-of select="concat('Persistent', @name)"/>
				</xsl:attribute>
				<xsl:attribute name="java-class">
					<xsl:value-of select="concat('com.top_logic.model.meta.Persistent', @name)"/>
				</xsl:attribute>
				<xsl:attribute name="table">
					<xsl:value-of select="@name"/>
				</xsl:attribute>
				
				<generalization type="{$interfaceName}" />
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:class/@name">
		<!-- Handled explicitly. -->
	</xsl:template>
	
	<xsl:template match="m:extends">
		<xsl:element name="generalization">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>

	<xsl:template match="m:extends/@name">
		<xsl:attribute name="type">
			<xsl:value-of select="."/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="m:class/@abstract">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="m:association">
		<xsl:element name="{local-name(.)}">
			<xsl:apply-templates select="@*"/>
			<ends>
				<xsl:apply-templates select="m:end"/>
			</ends>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="m:end/@aggregate">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="m:end/@composite">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="m:end/@mandatory">
		<!-- Drop -->
	</xsl:template>
	
	<xsl:template match="m:property/@type">
		<xsl:attribute name="type">
			<xsl:value-of select="concat('datatype:', .)"/>
		</xsl:attribute>
	</xsl:template>
	
	<xsl:template match="m:reference">
		<xsl:variable name="associationName">
			<xsl:choose>
				<xsl:when test="@association">
					<xsl:value-of select="@association"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="substring-before(@end, '#')"/>
				</xsl:otherwise>
			</xsl:choose>			
		</xsl:variable>
		<xsl:variable name="endName">
			<xsl:choose>
				<xsl:when test="@association or m:association">
					<xsl:value-of select="@end"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="substring-after(@end, '#')"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="association" select="(m:association | //m:association[@name=$associationName])[1]"/>
		<xsl:variable name="end" select="$association/m:end[@name=$endName]"/>
		<xsl:variable name="otherEnd" select="$association/m:end[@name!=$endName]"/>
	
		<xsl:if test="not($association/m:union)">
			<xsl:element name="{local-name(.)}">
				<xsl:attribute name="type">
					<xsl:value-of select="$end/@type"/>
				</xsl:attribute>
				<xsl:if test="$end/@multiple">
					<xsl:attribute name="multiple">
						<xsl:value-of select="$end/@multiple"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="$end/@ordered">
					<xsl:attribute name="ordered">
						<xsl:value-of select="$end/@ordered"/>
					</xsl:attribute>
				</xsl:if>
				<xsl:if test="$end/@bag">
					<xsl:attribute name="bag">
						<xsl:value-of select="$end/@bag"/>
					</xsl:attribute>
				</xsl:if>
				
				<xsl:apply-templates select="@*"/>
			</xsl:element>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:reference/@end">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="m:reference/@association">
		<!-- Drop. -->
	</xsl:template>
	
	<xsl:template match="m:enum">
		<xsl:element name="{local-name(.)}">
			<xsl:apply-templates select="@*"/>
			<classifiers>
				<xsl:apply-templates/>
			</classifiers>
		</xsl:element>
	</xsl:template>
	
	<!-- Switch namespace. -->	
	<xsl:template match="m:*">
		<xsl:element name="{local-name(.)}">
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
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
