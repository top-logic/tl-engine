<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="include[@name='templates/masterFrame.xml']">
		<include name="templates/masterFrame.xml">
			<xsl:apply-templates select="components"/>		
		</include>
	</xsl:template>

	<xsl:template match="include[@name='templates/contentTree.xml' or @name='templates/levelTwoTree.xml']">
		<include name="templates/treeWithContent.xml">
			<xsl:if test="@domain">
				<xsl:attribute name="domain">
					<xsl:value-of select="@domain"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@navigationRole">
				<xsl:attribute name="navigationRole">
					<xsl:value-of select="@navigationRole"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@componentName">
				<xsl:attribute name="z_legacy_componentName">
					<xsl:value-of select="@componentName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:variable name="navigationTreeName">
				<xsl:if test="@navigationTreeName">
					<xsl:value-of select="@navigationTreeName"/>
				</xsl:if>
				<xsl:if test="not(@navigationTreeName)">
					<xsl:value-of select="concat('templates/contentTree.xml', '#', @componentName, '_navigationTree')"/>
				</xsl:if>
			</xsl:variable>
			<xsl:attribute name="z_legacy_treeName">
				<xsl:value-of select="$navigationTreeName"/>
			</xsl:attribute>
			
			<xsl:if test="@projectLayoutName">
				<xsl:attribute name="z_legacy_contentName">
					<xsl:value-of select="@projectLayoutName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="not(@projectLayoutName)">
				<xsl:attribute name="z_legacy_contentName">
					<xsl:value-of select="concat(@componentName, '_PL')"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@resPrefix">
				<xsl:attribute name="z_legacy_tabLabel">
					<xsl:value-of select="concat(@resPrefix, 'tabber')"/>
				</xsl:attribute>
				<xsl:attribute name="z_legacy_treeTitle">
					<xsl:value-of select="concat(@resPrefix, 'tree.title')"/>
				</xsl:attribute>
				<xsl:attribute name="z_legacy_contentTitle">
					<xsl:value-of select="concat(@resPrefix, 'view.title')"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@image">
				<xsl:attribute name="tabIcon">
					<xsl:value-of select="inject/tabInfo/@image"/>
				</xsl:attribute>
			</xsl:if>
			
			<components>
				<include name="{@layout}"
					master="{$navigationTreeName}"
				/>
			</components>
		</include>
	</xsl:template>
	
	<xsl:template match="include[@name='templates/levelOneTree.xml' or @name='templates/levelTwoTabbedTree.xml']">
		<include name="templates/treeWithTabs.xml">
			<xsl:if test="@componentName">
				<xsl:attribute name="z_legacy_componentName">
					<xsl:value-of select="@componentName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@navigationTreeName">
				<xsl:attribute name="z_legacy_navigationTreeName">
					<xsl:value-of select="@navigationTreeName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="not(@navigationTreeName)">
				<xsl:attribute name="z_legacy_navigationTreeName">
					<xsl:value-of select="concat('templates/levelOneTree.xml', '#', @componentName, '_navigationTree')"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@tabBarName">
				<xsl:attribute name="z_legacy_tabBarName">
					<xsl:value-of select="@tabBarName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="not(@tabBarName)">
				<xsl:attribute name="z_legacy_tabBarName">
					<xsl:value-of select="concat(@componentName, '_tabBar')"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@domain">
				<xsl:attribute name="domain">
					<xsl:value-of select="@domain"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@resPrefix">
				<xsl:attribute name="z_legacy_resPrefix">
					<xsl:value-of select="@resPrefix"/>
				</xsl:attribute>
			</xsl:if>
			
			<xsl:if test="inject/tabInfo/@image">
				<xsl:attribute name="tabIcon">
					<xsl:value-of select="inject/tabInfo/@image"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@id">
				<xsl:attribute name="z_legacy_tabId">
					<xsl:value-of select="inject/tabInfo/@id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="inject/tabInfo/@label">
					<xsl:attribute name="z_legacy_tabLabel">
						<xsl:value-of select="inject/tabInfo/@label"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="@resPrefix">
						<xsl:attribute name="z_legacy_tabLabel">
							<xsl:value-of select="concat(@resPrefix, 'tabber')"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="inject/tabInfo/@rendered = 'false'">
				<xsl:attribute name="tabVisible">
					<xsl:value-of select="inject/tabInfo/@rendered"/>
				</xsl:attribute>
			</xsl:if>

			<xsl:apply-templates select="components"/>		
		</include>	
	</xsl:template>
	
	<xsl:template match="include[@name='templates/projectLayoutBrace.xml' or @name='templates/levelTwo.xml' or @name='templates/contentStructure.xml']">
		<include name="templates/contentTab.xml">
			<xsl:if test="@componentName">
				<xsl:attribute name="z_legacy_componentName">
					<xsl:value-of select="@componentName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@domain">
				<xsl:attribute name="domain">
					<xsl:value-of select="@domain"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@compoundMasterName">
				<xsl:attribute name="securityModel">
					<xsl:value-of select="concat('selection(', @compoundMasterName, ')')"/>
				</xsl:attribute>
			</xsl:if>

			<xsl:if test="inject/tabInfo/@image">
				<xsl:attribute name="tabIcon">
					<xsl:value-of select="inject/tabInfo/@image"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@id">
				<xsl:attribute name="z_legacy_tabId">
					<xsl:value-of select="inject/tabInfo/@id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="inject/tabInfo/@label">
					<xsl:attribute name="z_legacy_tabLabel">
						<xsl:value-of select="inject/tabInfo/@label"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="@resPrefix">
						<xsl:attribute name="z_legacy_tabLabel">
							<xsl:value-of select="concat(@resPrefix, 'tabber')"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="inject/tabInfo/@rendered = 'false'">
				<xsl:attribute name="tabVisible">
					<xsl:value-of select="inject/tabInfo/@rendered"/>
				</xsl:attribute>
			</xsl:if>

			<components>
				<include name="{@layout}"><xsl:if test="@name='templates/contentStructure.xml' and boolean(@domain)">
					<xsl:attribute name="structure">
						<xsl:value-of select="@domain"/>
					</xsl:attribute>
				</xsl:if></include>
			</components>
		</include>
	</xsl:template>
	
	<xsl:template match="include[@name='templates/levelOne.xml']">
		<include name="templates/tabbar.xml">
			<xsl:if test="@componentName">
				<xsl:attribute name="z_legacy_componentName">
					<xsl:value-of select="@componentName"/>
				</xsl:attribute>
				<xsl:attribute name="z_legacy_tabBarName">
					<xsl:value-of select="concat(@componentName, '_tabBar')"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@domain">
				<xsl:attribute name="domain">
					<xsl:value-of select="@domain"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@useDefaultChecker">
				<xsl:attribute name="useDefaultChecker">
					<xsl:value-of select="@useDefaultChecker"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@image">
				<xsl:attribute name="tabIcon">
					<xsl:value-of select="inject/tabInfo/@image"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@id">
				<xsl:attribute name="z_legacy_tabId">
					<xsl:value-of select="inject/tabInfo/@id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="inject/tabInfo/@label">
					<xsl:attribute name="z_legacy_tabLabel">
						<xsl:value-of select="inject/tabInfo/@label"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="@resPrefix">
						<xsl:attribute name="z_legacy_tabLabel">
							<xsl:value-of select="concat(@resPrefix, 'tabber')"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="inject/tabInfo/@rendered = 'false'">
				<xsl:attribute name="tabVisible">
					<xsl:value-of select="inject/tabInfo/@rendered"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="components"/>		
		</include>
	</xsl:template>

	<xsl:template match="include[@name='templates/levelTwoTabbed.xml']">
		<include name="templates/tabbarInner.xml">
			<xsl:if test="@componentName">
				<xsl:attribute name="z_legacy_componentName">
					<xsl:value-of select="@componentName"/>
				</xsl:attribute>
				<xsl:attribute name="z_legacy_tabBarName">
					<xsl:value-of select="concat(@componentName, '_tabBar')"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@useDefaultChecker">
				<xsl:attribute name="useDefaultChecker">
					<xsl:value-of select="@useDefaultChecker"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@image">
				<xsl:attribute name="tabIcon">
					<xsl:value-of select="inject/tabInfo/@image"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="inject/tabInfo/@id">
				<xsl:attribute name="z_legacy_tabId">
					<xsl:value-of select="inject/tabInfo/@id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="inject/tabInfo/@label">
					<xsl:attribute name="z_legacy_tabLabel">
						<xsl:value-of select="inject/tabInfo/@label"/>
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="@resPrefix">
						<xsl:attribute name="z_legacy_tabLabel">
							<xsl:value-of select="concat(@resPrefix, 'tabber')"/>
						</xsl:attribute>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="inject/tabInfo/@rendered = 'false'">
				<xsl:attribute name="tabVisible">
					<xsl:value-of select="inject/tabInfo/@rendered"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="components"/>		
		</include>
	</xsl:template>
	
	<xsl:template match="include[@name='templates/levelOneSelectProgram.xml']">
		<include name="templates/levelOneSelectProgram.xml">
			<xsl:if test="@componentName">
				<xsl:attribute name="componentName">
					<xsl:value-of select="@componentName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@domain">
				<xsl:attribute name="domain">
					<xsl:value-of select="@domain"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@resPrefix">
				<xsl:attribute name="resPrefix">
					<xsl:value-of select="@resPrefix"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@tabBarName">
				<xsl:attribute name="tabBarName">
					<xsl:value-of select="@tabBarName"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="inject"/>
			<xsl:apply-templates select="components"/>
		</include>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>