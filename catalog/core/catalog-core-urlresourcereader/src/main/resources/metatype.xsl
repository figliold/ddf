<?xml version="1.0" encoding="UTF-8"?>
<!--
/**
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 *
 **/
-->
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text" omit-xml-declaration="yes" />

<!-- note that the layout for the newline variable is deliberate, don't format -->
<xsl:variable name='newline'><xsl:text>
</xsl:text></xsl:variable>

<xsl:variable name='quote'>
  <xsl:text>"</xsl:text>
</xsl:variable>

<xsl:variable name='equal'>
  <xsl:text>=</xsl:text>
</xsl:variable>

<xsl:variable name='comma'>
  <xsl:text>,</xsl:text>
</xsl:variable>

<xsl:variable name='servicePid'>
  <xsl:text>service.pid</xsl:text>
</xsl:variable>

<xsl:variable name='Long'>
  <xsl:text>L</xsl:text>
</xsl:variable>

<xsl:variable name='arrayStart'>
  <xsl:text>[</xsl:text>
</xsl:variable>

<xsl:variable name='arrayEnd'>
  <xsl:text>]</xsl:text>
</xsl:variable>

<xsl:template match="/">
  <xsl:apply-templates />
</xsl:template>

<xsl:template match="*[local-name()='MetaData']">
  <xsl:apply-templates select="./*[local-name()='OCD']"/>
  <xsl:apply-templates select="./*[local-name()='Designate']"/>
</xsl:template>

<xsl:template match="*[local-name()='OCD']">
  <xsl:apply-templates select="./*[local-name()='AD']"/>
</xsl:template>

<xsl:template match="*[local-name()='Designate']">
  <xsl:apply-templates select="./*[local-name()='Object']"/>
</xsl:template>

<xsl:template match="*[local-name()='Object']">
  <xsl:variable name="ocdref" select="@ocdref"/>
  <xsl:value-of select="concat($servicePid,$equal,$quote,$ocdref,$quote,$newline)" />
</xsl:template>

<xsl:template match="*[local-name()='AD']">
   <xsl:variable name="type" select="@type"/>
   <xsl:variable name="id" select="@id"/>
   <xsl:variable name="default" select="@default"/>
   <xsl:variable name="cardinality" select="@cardinality"/>
    <xsl:choose>
    <xsl:when test="$type = 'String'">
        <xsl:choose>
            <xsl:when test="$cardinality &gt; 0">
                <xsl:variable name="list">
                    <xsl:call-template name="split">
                        <xsl:with-param name="input" select="$default" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="concat($id,$equal,$arrayStart,$list,$arrayEnd,$newline)" />
            </xsl:when>
            <xsl:when test="$cardinality &lt; 0">
                <xsl:value-of select="concat($id,$equal,$arrayStart,$quote,$default,$quote,$newline)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($id,$equal,$quote,$default,$quote,$newline)" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:when>
   <xsl:when test="$type = 'Long'">
     <xsl:value-of select="concat($id,$equal,$Long,$quote,$default,$quote,$newline)" />
   </xsl:when>
   <xsl:when test="$type = 'Double'">
     <xsl:value-of select="$id" />=D"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Float'">
     <xsl:value-of select="$id" />=F"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Integer'">
     <xsl:value-of select="$id" />=I"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Byte'">
     <xsl:value-of select="$id" />=X"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Char'">
     <xsl:value-of select="$id" />=C"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Boolean'">
     <xsl:value-of select="$id" />=B"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Short'">
     <xsl:value-of select="$id" />=S"<xsl:value-of select="$default" />"
   </xsl:when>
   <xsl:when test="$type = 'Password'">
     <xsl:value-of select="$id" />=P"<xsl:value-of select="$default" />"
   </xsl:when>
     <xsl:otherwise>
      <xsl:text>Failed to process metatype file.</xsl:text>
     </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<xsl:template name="split">
    <xsl:param name="input" />
    <xsl:if test="string-length($input) &gt; 0">
        <xsl:variable name="v"
            select="substring-before(concat($input, ','), ',')" />
        <xsl:choose>
            <xsl:when test="string-length(substring-after($input, ',')) &gt; 0">
                <xsl:value-of select="concat($quote,$v,$quote, $comma)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($quote,$v,$quote)" />
            </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="split">
            <xsl:with-param name="input" select="substring-after($input, ',')" />
        </xsl:call-template>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>