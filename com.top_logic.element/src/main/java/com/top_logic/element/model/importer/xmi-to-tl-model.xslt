<?xml version="1.0" encoding="UTF-8"?>

<!--Transformation from XMI/UML to TopLogic 7 Model XML -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:xmi='http://www.omg.org/spec/XMI/20131001'
	xmlns:uml='http://www.omg.org/spec/UML/20131001'
	xmlns="http://www.top-logic.com/ns/dynamic-types/6.0"
	xmlns:tl="http://www.top-logic.com/ns/dynamic-types/6.0"
>

	<xsl:template match="xmi:XMI">
		<model>
			<xsl:apply-templates/>
		</model>
	</xsl:template>

	<xsl:template match="uml:Model">
		<xsl:if test="count(packagedElement) &gt; 0">
			<module name="{name|@name}">
				<xsl:apply-templates/>
			</module>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="uml:Package">
		<xsl:variable name="name">
			<xsl:value-of select="name | @name"/>
		</xsl:variable>
		<module name="{$name}">
			<xsl:apply-templates/>
		</module>
		
		<xsl:apply-templates mode="innerPackages">
			<xsl:with-param name="outerName" select="$name"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="innerPackages" match="packagedElement[@xmi:type='uml:Package']">
  		<xsl:param name="outerName"/>

  		<xsl:variable name="name">
			<xsl:value-of select="concat($outerName, '.', name | @name)"/>
		</xsl:variable>

		<module name="{$name}">
			<xsl:apply-templates/>
		</module>

		<xsl:apply-templates mode="innerPackages">
			<xsl:with-param name="outerName" select="$name"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="innerPackages" match="*|@*|text()">
	</xsl:template>	
	
	<xsl:template match="packagedElement[@xmi:type='uml:Enumeration'] | nestedClassifier[@xmi:type='uml:Enumeration']">
		<enum name="{name|@name}">
			<xsl:for-each select="ownedLiteral[@xmi:type='uml:EnumerationLiteral']">
				<classifier name="{name|@name}"/>
			</xsl:for-each>
		</enum>
	</xsl:template>
	
	<xsl:template match="packagedElement[@xmi:type='uml:Class'] | nestedClassifier[@xmi:type='uml:Class']">
		<xsl:variable name="ownId">
			<xsl:value-of select="@xmi:id"/>
		</xsl:variable>
		<class name="{name|@name}">
			<xsl:if test="isAbstract|@isAbstract">
				<xsl:attribute name="final">
					<xsl:value-of select="isAbstract|@isAbstract"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="isFinalSpecialization|@isFinalSpecialization">
				<xsl:attribute name="abstract">
					<xsl:value-of select="isFinalSpecialization|@isFinalSpecialization"/>
				</xsl:attribute>
			</xsl:if>
					
			<xsl:if test="count(generalization) &gt; 0">
				<generalizations>
					<xsl:for-each select="generalization">
						<xsl:variable name="typeName">
							<xsl:choose>
								<xsl:when test="general/type">
									<xsl:apply-templates select="general" mode="type"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:call-template name="findtype">
										<xsl:with-param name="name" select="@general"/>
									</xsl:call-template>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<generalization type="{$typeName}"/>
					</xsl:for-each>
				</generalizations>
			</xsl:if>
		
			<attributes>
				<xsl:apply-templates mode="attributes"/>
				<xsl:for-each select="//packagedElement[@xmi:type='uml:Association'][ownedEnd[1]/type/@xmi:idref=$ownId]">
					<xsl:variable name="refType">
						<xsl:choose>
							<xsl:when test="ownedEnd[2]/type">
								<xsl:apply-templates mode="type" select="ownedEnd[2]/type"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="findtype">
									<xsl:with-param name="name" select="ownedEnd[2]/@type"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					
					<reference name="{ownedEnd[2]/name}" type="{$refType}"/>
				</xsl:for-each>

				<xsl:for-each select="//packagedElement[@xmi:type='uml:Association'][ownedEnd[2]/type/@xmi:idref=$ownId]">
					<xsl:if test="string-length(ownedEnd[1]/name) &gt; 0">
					<xsl:variable name="refType">
						<xsl:choose>
							<xsl:when test="ownedEnd[1]/type">
								<xsl:apply-templates mode="type" select="ownedEnd[1]/type"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="findtype">
									<xsl:with-param name="name" select="ownedEnd[1]/@type"/>
								</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					
					<reference name="{ownedEnd[1]/name}" type="{$refType}" kind="backwards" inverse-reference="{ownedEnd[2]/name}"/>
					</xsl:if>
				</xsl:for-each>
			</attributes>
		</class>
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="packagedElement[@xmi:type='uml:DataType'] | nestedClassifier[@xmi:type='uml:DataType']">
		<datatype name="{name|@name}" db_type="varchar" db_size="255" kind="String">
			<storage-mapping 
				class="com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping" 
				application-type="java.lang.String"
			/>
					
			<annotations>
				<config-type value="STRING"/>
			</annotations>
		</datatype>
		
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template mode="attributes" match="ownedAttribute[@xmi:type='uml:Property']">
		<xsl:variable name="typeName">
			<xsl:choose>
				<xsl:when test="type">
					<xsl:apply-templates select="type" mode="type"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="findtype">
						<xsl:with-param name="name" select="@type"/>
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="typeKind">
			<xsl:apply-templates select="type" mode="typeKind"/>
		</xsl:variable>

		<xsl:variable name="mandatory">
			<xsl:choose>
				<xsl:when test="lowerValue and not((lowerValue/value and number(lowerValue/value) = 0) or (lowerValue/@value and number(lowerValue/@value) = 0))">
					<xsl:text>true</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>false</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:choose>
			<xsl:when test="$typeKind = 'property'">
				<property name="{name|@name}" type="{$typeName}" mandatory="{$mandatory}">
					<xsl:if test="redefinedProperty">
						<xsl:attribute name="override">
							<xsl:text>true</xsl:text>						
						</xsl:attribute>
					</xsl:if>
					<xsl:apply-templates/>
				</property>
			</xsl:when>
			<xsl:otherwise>
				<reference name="{name|@name}" type="{$typeName}" mandatory="{$mandatory}">
					<xsl:if test="not(redefinedProperty)">
						<xsl:variable name="multiple">
							<xsl:choose>
								<xsl:when test="upperValue/value and (string(upperValue/value) = '*' or number(upperValue/value) &gt; 1) or upperValue/@value and (string(upperValue/@value) = '*' or number(upperValue/@value) &gt; 1)">
									<xsl:text>true</xsl:text>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>false</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="ordered">
							<xsl:choose>
								<xsl:when test="ordered|@ordered">
									<xsl:value-of select="ordered|@ordered"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>false</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						<xsl:variable name="composite">
							<xsl:choose>
								<xsl:when test="aggregation">
									<xsl:value-of select="string(aggregation) = 'composite'"/>
								</xsl:when>
								<xsl:when test="@aggregation">
									<xsl:value-of select="string(@aggregation) = 'composite'"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:text>false</xsl:text>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>
						
						<xsl:attribute name="multiple">
							<xsl:value-of select="$multiple"/>
						</xsl:attribute>
						<xsl:attribute name="ordered">
							<xsl:value-of select="$ordered"/>
						</xsl:attribute>
						<xsl:attribute name="composite">
							<xsl:value-of select="$composite"/>
						</xsl:attribute>
					</xsl:if>
					<xsl:if test="redefinedProperty">
						<xsl:attribute name="override">
							<xsl:text>true</xsl:text>						
						</xsl:attribute>
					</xsl:if>
					<xsl:apply-templates/>
				</reference>
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>

	<xsl:template mode="attributes" match="*|@*|text()">
	</xsl:template>	

	<xsl:template mode="type" match="type[@href]">
		<xsl:text>unknown:</xsl:text>
		<xsl:value-of select="@href"/>
	</xsl:template>

	<xsl:template mode="type" match="type[@href='http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#String']">
		<xsl:text>tl.core:String</xsl:text>
	</xsl:template>

	<xsl:template mode="type" match="type[@href='http://www.aris.com/metamodels/Base/1.0/Base.xmi#TimeStamp']">
		<xsl:text>tl.core:DateTime</xsl:text>
	</xsl:template>

	<xsl:template mode="type" match="type[@href='http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Boolean']">
		<xsl:text>tl.core:Boolean</xsl:text>
	</xsl:template>

	<xsl:template mode="type" match="type[@href='http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Integer']">
		<xsl:text>tl.core:Integer</xsl:text>
	</xsl:template>

	<xsl:template mode="type" match="type[@href='http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#UnlimitedNatural']">
		<xsl:text>tl.core:Long</xsl:text>
	</xsl:template>

	<xsl:template mode="type" match="type[@href='http://www.omg.org/spec/UML/20131001/PrimitiveTypes.xmi#Real']">
		<xsl:text>tl.core:Double</xsl:text>
	</xsl:template>

	<xsl:template mode="type" match="type[@xmi:idref] | general[@xmi:idref]">
		<xsl:variable name="idref" select="@xmi:idref"/>
		<xsl:variable name="node" select="//*[@xmi:id=$idref]"/>
		
		<xsl:choose>
			<xsl:when test="count($node) = 1">
				<xsl:call-template name="qtypename">
					<xsl:with-param name="node" select="$node"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="count($node) = 0">
				<xsl:message>
					<xsl:text>Invalid reference: </xsl:text>
					<xsl:value-of select="$idref"/>
				</xsl:message>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message>
					<xsl:text>Ambiguous reference: </xsl:text>
					<xsl:value-of select="$idref"/>
				</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="findtype">
		<xsl:param name="name"/>
		
		<xsl:variable name="node" select="//packagedElement[(@xmi:type='uml:Class' or @xmi:type='uml:Enumeration') and @name=$name]"/>
		
		<xsl:choose>
			<xsl:when test="count($node) = 1">
				<xsl:call-template name="qtypename">
					<xsl:with-param name="node" select="$node"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="count($node) = 0">
				<xsl:message>
					<xsl:text>Invalid reference: </xsl:text>
					<xsl:value-of select="$name"/>
				</xsl:message>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message>
					<xsl:text>Ambiguous reference: </xsl:text>
					<xsl:value-of select="$name"/>
				</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="qname">
		<xsl:param name="node"/>
		
		<xsl:variable name="parent" select="$node/parent::*[local-name(.) = 'Package' or (local-name() = 'packagedElement' and @xmi:type='uml:Package')]"/>
		
		<xsl:if test="$parent">
			<xsl:call-template name="qname">
				<xsl:with-param name="node" select="$parent"/>
			</xsl:call-template>
			<xsl:text>.</xsl:text>
		</xsl:if>
		<xsl:value-of select="$node/@name | $node/name"/>
	</xsl:template>
	
	<xsl:template name="qtypename">
		<xsl:param name="node"/>
		
		<xsl:variable name="parent" select="$node/parent::*[local-name(.) = 'Package' or (local-name() = 'packagedElement' and @xmi:type='uml:Package')]"/>

		<xsl:call-template name="qname">
			<xsl:with-param name="node" select="$parent"/>
		</xsl:call-template>
		<xsl:text>:</xsl:text>
		<xsl:value-of select="$node/@name | $node/name"/>
	</xsl:template>
	
	<xsl:template mode="type" match="*|@*|text()">
	</xsl:template>	

	<xsl:template mode="typeKind" match="type[@href]">
		<xsl:text>property</xsl:text>
	</xsl:template>

	<xsl:template mode="typeKind" match="type[@xmi:idref] | general[@xmi:idref]">
		<xsl:variable name="idref" select="@xmi:idref"/>
		<xsl:variable name="node" select="//*[@xmi:id=$idref]"/>
		
		<xsl:choose>
			<xsl:when test="$node/@xmi:type='uml:DataType'">
				<xsl:text>property</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>reference</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template mode="typeKind" match="*|@*|text()">
	</xsl:template>	
	
	<xsl:template match="*|@*|text()">
	</xsl:template>	
	
</xsl:stylesheet>
