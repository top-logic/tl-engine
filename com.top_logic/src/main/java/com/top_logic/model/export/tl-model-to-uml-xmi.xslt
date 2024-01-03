<?xml version="1.0" encoding="UTF-8"?>

<!--
 *	Transformation from TopLogic 6 Model specification to XMI/UML
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 -->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns='org.omg.xmi.namespace.XMI'	
	xmlns:UML='org.omg.xmi.namespace.UML'
	xmlns:m="http://www.top-logic.com/ns/model/6.0"	
	xmlns:i18n="annotation:com.top_logic.model.annotate.TLLocalizedName"	
	xmlns:tl5="annotation:com.top_logic.model.v5.TL5Name"	
>

	<xsl:template match="m:model">
		<XMI xmi.version='1.2' timestamp='Wed Jan 13 12:51:00 CET 2010'>
			<XMI.header>
				<XMI.documentation>
					<XMI.exporter>TopLogic Model Export</XMI.exporter>
				</XMI.documentation>
				<XMI.metamodel xmi.name="UML" xmi.version="1.4" />
			</XMI.header>
			<XMI.content>
				<UML:Model 
					name='Model' isSpecification='false' isRoot='false'
					isLeaf='false' isAbstract='false'>
					<UML:Namespace.ownedElement>
						<xsl:apply-templates/>
						
						<UML:DataType xmi.id="tl5.boolean" name="boolean" isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false"/>
						<UML:DataType xmi.id="tl5.int" name="int" isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false"/>
						<UML:DataType xmi.id="tl5.float" name="float" isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false"/>
						<UML:DataType xmi.id="tl5.string" name="string" isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false"/>
						<UML:DataType xmi.id="tl5.date" name="date" isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false"/>
						<UML:DataType xmi.id="tl5.binary" name="binary" isSpecification="false" isRoot="false" isLeaf="false" isAbstract="false"/>
					
				        <UML:TagDefinition xmi.id = 'tag.documentation' name = 'documentation' isSpecification = 'false'>
				          <UML:TagDefinition.multiplicity>
				            <UML:Multiplicity>
				              <UML:Multiplicity.range>
				                <UML:MultiplicityRange lower = '0' upper = '1'/>
				              </UML:Multiplicity.range>
				            </UML:Multiplicity>
				          </UML:TagDefinition.multiplicity>
				        </UML:TagDefinition>
					
					</UML:Namespace.ownedElement>
				</UML:Model>
			</XMI.content>
		</XMI>
	</xsl:template>
	
	<xsl:template name="documentation">
		<xsl:if test="boolean(@i18n:name) or boolean(@tl5:name)">
			<UML:ModelElement.taggedValue>
			  <UML:TaggedValue isSpecification = 'false'>
			    <UML:TaggedValue.dataValue>
					<xsl:if test="boolean(@i18n:name)">
						<xsl:text>Localized-Name: </xsl:text>
						<xsl:value-of select="@i18n:name"/>
						<xsl:value-of select="'&#10;'"/>
					</xsl:if>
					<xsl:if test="boolean(@tl5:name)">
						<xsl:text>Implementation-Name: </xsl:text>
						<xsl:value-of select="@tl5:name"/>
						<xsl:value-of select="'&#10;'"/>
					</xsl:if>
			    </UML:TaggedValue.dataValue>
			    <UML:TaggedValue.type>
			      <UML:TagDefinition xmi.idref = 'tag.documentation'/>
			    </UML:TaggedValue.type>
			  </UML:TaggedValue>
			</UML:ModelElement.taggedValue>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="m:module">
		<xsl:variable name="package-name" select="@name" />
		
        <UML:Package xmi.id = '{@name}'
			name = '{@name}' isSpecification = 'false' isRoot = 'false' isLeaf = 'false'
			isAbstract = 'false'>
			<xsl:call-template name="documentation"/>
			
			<UML:Namespace.ownedElement>
				<xsl:apply-templates>
					<xsl:with-param name="package-name" select="$package-name"/>
				</xsl:apply-templates>
				
				<xsl:apply-templates select="./m:class/m:reference/m:association">
					<xsl:with-param name="package-name" select="$package-name"/>
				</xsl:apply-templates>
			</UML:Namespace.ownedElement>
		</UML:Package>
	</xsl:template>

	<xsl:template match="m:enum">
		<xsl:param name="package-name" />

		<xsl:variable name="enumeration-id">
			<xsl:value-of select="concat($package-name, '.', @name)"/>
		</xsl:variable>
		
        <UML:Enumeration xmi.id = '{$enumeration-id}'
          name = '{@name}' isSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = 'false'>
			<xsl:call-template name="documentation"/>

			<UML:Enumeration.literal>
				<xsl:apply-templates mode="enumeration-contents">
					<xsl:with-param name="package-name" select="$package-name" />
					<xsl:with-param name="enumeration-id" select="$enumeration-id" />
				</xsl:apply-templates>
			</UML:Enumeration.literal>
        </UML:Enumeration>
	</xsl:template>
	
	<xsl:template match="m:classifier" mode="enumeration-contents">
		<xsl:param name="package-name" />
		<xsl:param name="enumeration-id" />
		
		<UML:EnumerationLiteral xmi.id = '{$enumeration-id}#{@name}'
		  name = '{@name}' isSpecification = 'false'>
			<xsl:call-template name="documentation"/>
		</UML:EnumerationLiteral>
	</xsl:template>
	
	<xsl:template match="m:association">
		<xsl:param name="package-name" />
		
		<xsl:variable name="association-name">
			<xsl:choose>
				<xsl:when test="boolean(@name)">
					<xsl:value-of select="@name"/>
				</xsl:when>			
				<xsl:otherwise>
					<xsl:for-each select="m:end">
						<xsl:if test="position() &gt; 1">
							<xsl:value-of select="'-'"/>
						</xsl:if>
						<xsl:value-of select="@name"/>
					</xsl:for-each>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		<xsl:variable name="association-id">
			<xsl:choose>
				<xsl:when test="boolean(@name)">
					<xsl:value-of select="concat($package-name, '.', @name)"/>
				</xsl:when>			
				<xsl:otherwise>
					<xsl:value-of select="@id"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:choose>
			<xsl:when test="boolean(m:property)">
		        <UML:AssociationClass xmi.id = '{$association-id}'
		          name = '{$association-name}' visibility = 'public' isSpecification = 'false' isRoot = 'false'
		          isLeaf = 'false' isAbstract = 'false' isActive = 'false'>
					<UML:Association.connection>
						<xsl:apply-templates mode="association-contents">
							<xsl:with-param name="package-name" select="$package-name" />
							<xsl:with-param name="association-id" select="$association-id" />
						</xsl:apply-templates>
					</UML:Association.connection>
					<xsl:apply-templates mode="type-contents">
						<xsl:with-param name="package-name" select="$package-name" />
						<xsl:with-param name="association-id" select="$association-id" />
					</xsl:apply-templates>
				</UML:AssociationClass>
			</xsl:when>
			<xsl:otherwise>
		        <UML:Association xmi.id = '{$association-id}'
					name = '{$association-name}' isSpecification = 'false' isRoot = 'false' isLeaf = 'false' isAbstract = 'false'>
					<UML:Association.connection>
					<xsl:apply-templates mode="association-contents">
						<xsl:with-param name="package-name" select="$package-name" />
						<xsl:with-param name="association-id" select="$association-id" />
					</xsl:apply-templates>
		          </UML:Association.connection>
		        </UML:Association>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="m:end" mode="association-contents">
		<xsl:param name="package-name" />
		<xsl:param name="association-id" />
		
		<xsl:variable name="end-id">
			<xsl:value-of select="concat($association-id, '.', @name)"/>
		</xsl:variable>
		<xsl:variable name="target-id">
			<xsl:choose>
				<xsl:when test="@type = 'BOOLEAN'">
					<xsl:value-of select="'tl5.boolean'"/>
				</xsl:when>
				<xsl:when test="@type = 'INT'">
					<xsl:value-of select="'tl5.int'"/>
				</xsl:when>
				<xsl:when test="@type = 'FLOAT'">
					<xsl:value-of select="'tl5.float'"/>
				</xsl:when>
				<xsl:when test="@type = 'STRING'">
					<xsl:value-of select="'tl5.string'"/>
				</xsl:when>
				<xsl:when test="@type = 'BINARY'">
					<xsl:value-of select="'tl5.binary'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:call-template name="mk-qualified-name">
						<xsl:with-param name="package-name" select="$package-name" />
						<xsl:with-param name="name" select="@type" />
					</xsl:call-template>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="aggregation">
			<xsl:choose>
				<xsl:when test="@aggregate = 'true'">
					<xsl:value-of select="'composite'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'none'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="lower">
			<xsl:choose>
				<xsl:when test="@mandatory = 'true'">
					<xsl:value-of select="'1'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'0'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="upper">
			<xsl:choose>
				<xsl:when test="@multiple = 'true'">
					<xsl:value-of select="'-1'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'1'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="ordering">
			<xsl:choose>
				<xsl:when test="@ordered = 'true'">
					<xsl:value-of select="'ordered'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="'unordered'"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

        <UML:AssociationEnd xmi.id = '{$end-id}'
          name = '{@name}' visibility = 'public' isSpecification = 'false' isNavigable = 'true'
          ordering = '{$ordering}' aggregation = '{$aggregation}' targetScope = 'instance' changeability = 'changeable'>
		  <xsl:call-template name="documentation"/>
		
          <UML:AssociationEnd.multiplicity>
            <UML:Multiplicity>
              <UML:Multiplicity.range>
                <UML:MultiplicityRange
                  lower = '{$lower}' upper = '{$upper}'/>
              </UML:Multiplicity.range>
            </UML:Multiplicity>
          </UML:AssociationEnd.multiplicity>
          <UML:AssociationEnd.participant>
            <UML:Class xmi.idref = '{$target-id}'/>
          </UML:AssociationEnd.participant>
        </UML:AssociationEnd>
	</xsl:template>
	
	<xsl:template match="m:class">
		<xsl:param name="package-name" />
		
		<xsl:variable name="class-name" select="concat($package-name, '.', @name)" />
		<UML:Class xmi.id = '{$class-name}'
              name = '{@name}' visibility = 'public' isSpecification = 'false' isRoot = 'false'
              isLeaf = 'false' isAbstract = '{@abstract}' isActive = 'false'>
			<xsl:call-template name="documentation"/>
			
			<xsl:apply-templates mode="type-contents" select="m:property">
				<xsl:with-param name="package-name" select="$package-name"/>
				<xsl:with-param name="class-name" select="@name" />
			</xsl:apply-templates>
		</UML:Class>
		
		<xsl:apply-templates mode="type-contents" select="m:extends">
			<xsl:with-param name="package-name" select="$package-name"/>
			<xsl:with-param name="class-name" select="@name" />
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="m:extends" mode="type-contents">
		<xsl:param name="package-name" />
		<xsl:param name="class-name" />
		
		<xsl:variable name="target-name">
			<xsl:call-template name="mk-qualified-name">
				<xsl:with-param name="package-name" select="$package-name" />
				<xsl:with-param name="name" select="@name" />
			</xsl:call-template>
		</xsl:variable>

        <UML:Generalization isSpecification = 'false' name='{$class-name} extends {@name}' >
          <UML:Generalization.child>
            <UML:Class xmi.idref = '{$package-name}.{$class-name}'/>
          </UML:Generalization.child>
          <UML:Generalization.parent>
            <UML:Class xmi.idref = '{$target-name}'/>
          </UML:Generalization.parent>
        </UML:Generalization>
	</xsl:template>
	
	<xsl:template match="m:property" mode="type-contents">
		<xsl:param name="class-name" />

		<xsl:variable name="type">
			<xsl:choose>
				<xsl:when test="@type = 'BOOLEAN'">
					<xsl:value-of select="'tl5.boolean'"/>
				</xsl:when>
				<xsl:when test="@type = 'INT'">
					<xsl:value-of select="'tl5.int'"/>
				</xsl:when>
				<xsl:when test="@type = 'FLOAT'">
					<xsl:value-of select="'tl5.float'"/>
				</xsl:when>
				<xsl:when test="@type = 'STRING'">
					<xsl:value-of select="'tl5.string'"/>
				</xsl:when>
				<xsl:when test="@type = 'DATE'">
					<xsl:value-of select="'tl5.date'"/>
				</xsl:when>
				<xsl:when test="@type = 'BINARY'">
					<xsl:value-of select="'tl5.binary'"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">Unknown primitive type '<xsl:value-of select="@type"/>' in propert '<xsl:value-of select="@name"/>' of class '<xsl:value-of select="$class-name"/>'.</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

	    <UML:Classifier.feature>
			<UML:Attribute 
			  name = '{@name}' visibility = 'public' isSpecification = 'false' ownerScope = 'instance'
			  changeability = 'changeable' targetScope = 'instance'>
			  <xsl:call-template name="documentation"/>
				
			  <UML:StructuralFeature.type>
			    <UML:DataType xmi.idref = '{$type}' />
			  </UML:StructuralFeature.type>
			</UML:Attribute>
	    </UML:Classifier.feature>
	</xsl:template>
	
	<xsl:template name="mk-qualified-name">
		<xsl:param name="package-name" />
		<xsl:param name="name" />

		<xsl:choose>
			<xsl:when test="contains($name, '.')">
				<xsl:value-of select="$name" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat($package-name, '.', $name)" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>