<?xml version="1.0" encoding="UTF-8"?>

<!--
	Script for creating typed configurations from legacy "ConnectionPoolRegistry" configurations.

	You may use the service http://www.freeformatter.com/xsl-transformer.html 
	to rewrite your configurations. Cut and paste this script and the complete 
	legacy configuration file to the transformer page. The produced result can 
	be inserted into the typed configuration section of the application. 
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:config="http://www.top-logic.com/ns/config/6.0">

	<xsl:template match="/">
		<application>
			<services>
				<config service-class="com.top_logic.basic.sql.ConnectionPoolRegistry">
					<instance>
						<pools>
							<xsl:apply-templates mode="registry"/>
							<xsl:apply-templates mode="poolOverride" select="//section[starts-with(@name, 'ConnectionPool.')]"/>
							<xsl:apply-templates mode="dsOverride" select="//section[starts-with(@name, 'DataSource.')]"/>
						</pools>
					</instance>
				</config>
			</services>
		</application>
	</xsl:template>
	
	<xsl:template match="//properties/section[@name='ConnectionPoolRegistry']">
		<application>
			<services>
				<config service-class="com.top_logic.basic.sql.ConnectionPoolRegistry">
					<instance>
						<pools>
							<xsl:apply-templates mode="registry"/>
							<xsl:apply-templates mode="poolOverride" select="//section[starts-with(@name, 'ConnectionPool.')]"/>
							<xsl:apply-templates mode="dsOverride" select="//section[starts-with(@name, 'DataSource.')]"/>
						</pools>
					</instance>
				</config>
			</services>
		</application>
	</xsl:template>
	
	<xsl:template mode="registry" match="section[@name='ConnectionPoolRegistry']/entry">
		<xsl:call-template name="makePool">
			<xsl:with-param name="name" select="@name"/>
			<xsl:with-param name="sectionName" select="@value"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template name="makePool">
		<xsl:param name="name"/>
		<xsl:param name="sectionName"/>
		
		<xsl:element name="pool">
			<xsl:attribute name="name">
				<xsl:value-of select="$name"/>
			</xsl:attribute>
			<xsl:if test="//section[@name=$sectionName]/@inherit='no'">
				<xsl:attribute name="config:override">
					<xsl:value-of select="'true'"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates mode="pool" select="//section[@name=$sectionName]" />
		</xsl:element>		
	</xsl:template>

	<xsl:template mode="poolOverride" match="section">
		<xsl:variable name="poolName" select="substring-after(@name, 'ConnectionPool.')"/>
		
		<xsl:if test="not(//section[@name='ConnectionPoolRegistry']/entry[@name=$poolName])">
			<xsl:call-template name="makePool">
				<xsl:with-param name="name" select="$poolName"/>
				<xsl:with-param name="sectionName" select="@name"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="dsOverride" match="section">
		<xsl:variable name="poolName" select="substring-after(@name, 'DataSource.')"/>
		
		<xsl:if test="not(//section[@name='ConnectionPoolRegistry']/entry[@name=$poolName]) and not(//section[@name=concat('ConnectionPool.', $poolName)])">
			<pool name="{$poolName}">
				<xsl:call-template name="datasource">
					<xsl:with-param name="sectionName" select="@name"/>
				</xsl:call-template>
			</pool>
		</xsl:if>
	</xsl:template>

	<xsl:template mode="pool" match="section">
		<xsl:apply-templates mode="pool" />
		
		<read-pool>
			<xsl:apply-templates mode="readpool" select="entry[not(contains(@name, '.write'))]" />
		</read-pool>
		
		<write-pool>
			<xsl:apply-templates mode="writepool" select="entry[contains(@name, '.write')]"/>
		</write-pool>
	</xsl:template>

	<xsl:template mode="pool" match="entry[@name='datasourceSection']">
		<xsl:call-template name="datasource">
			<xsl:with-param name="sectionName" select="@value"/>
		</xsl:call-template>		
	</xsl:template>

	<xsl:template mode="pool" match="entry[@name='class']">
		<xsl:element name="{@name}"><xsl:value-of select="@value"/></xsl:element>
	</xsl:template>

	<xsl:template mode="readpool" match="entry[@name='maxCut']">
		<!-- Drop legacy property. -->
	</xsl:template>

	<xsl:template mode="readpool" match="entry[@name='datasourceSection']">
		<!-- Transformed separately. -->
	</xsl:template>

	<xsl:template mode="readpool" match="entry[@name='class']">
		<!-- Transformed separately. -->
	</xsl:template>

	<xsl:template mode="readpool" match="entry">
		<xsl:element name="{@name}"><xsl:value-of select="@value"/></xsl:element>
	</xsl:template>

	<xsl:template mode="writepool" match="entry">
		<xsl:element name="{substring-before(@name, '.write')}"><xsl:value-of select="@value"/></xsl:element>
	</xsl:template>

	<xsl:template name="datasource">
		<xsl:param name="sectionName"/>
		
		<xsl:element name="data-source">
			<xsl:if test="//section[@name=$sectionName]/@inherit='no'">
				<xsl:attribute name="config:override">
					<xsl:value-of select="'true'"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates mode="datasource" select="//section[@name=$sectionName]/entry"/>
			
			<options>
				<xsl:apply-templates mode="options" select="//section[@name=$sectionName]/entry"/>
			</options>
		</xsl:element>
	</xsl:template>

	<xsl:template mode="datasource" match="entry[@name='jndiName']">
		<xsl:element name="jndi-name"><xsl:value-of select="@value"/></xsl:element>
	</xsl:template>

	<xsl:template mode="datasource" match="entry[@name='dataSource']">
		<xsl:element name="driver-class"><xsl:value-of select="@value"/></xsl:element>
	</xsl:template>

	<xsl:template mode="options" match="entry[@name='jndiName']">
	</xsl:template>

	<xsl:template mode="options" match="entry[@name='dataSource']">
	</xsl:template>

	<xsl:template mode="options" match="entry">
		<xsl:variable name="optionElement">
			<xsl:choose>
				<xsl:when test="@encrypted = 'true'">
					<xsl:value-of select="'encrypted-option'"></xsl:value-of>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'option'"></xsl:value-of>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:element name="{$optionElement}">
			<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
			<xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
		</xsl:element>
	</xsl:template>

	<!-- standard skip template -->
	<xsl:template match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template mode="datasource" match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template mode="pool" match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template mode="readpool" match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template mode="writepool" match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
	<xsl:template mode="options" match="@*|node()">
		<xsl:apply-templates/>
	</xsl:template>
</xsl:stylesheet>
