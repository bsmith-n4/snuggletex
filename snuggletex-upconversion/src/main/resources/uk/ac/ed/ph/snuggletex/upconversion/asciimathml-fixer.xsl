<!--

$Id$

This stylesheet fixes the raw MathML produced by ASCIIMathML. The result
is intended to be equivalent to what is generated by SnuggleTeX, which
allows all of the existing up-conversion work done for SnuggleTeX to be
applied here as well.

Copyright (c) 2008-2011, The University of Edinburgh
All Rights Reserved

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:s="http://www.ph.ed.ac.uk/snuggletex"
  xmlns="http://www.w3.org/1998/Math/MathML"
  exclude-result-prefixes="xs m s"
  xpath-default-namespace="http://www.w3.org/1998/Math/MathML">

  <xsl:import href="pmathml-utilities.xsl"/>
  <xsl:import href="snuggletex-utilities.xsl"/>
  <xsl:strip-space elements="m:*"/>
  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

  <!-- ************************************************************ -->

  <xsl:variable name="s:asciimath-input-annotation" as="xs:string" select="'ASCIIMathInput'"/>

  <!-- ************************************************************ -->

  <!-- This is the entry point for the traditional ASCIIMathML output -->
  <xsl:template match="math[@title]">
    <math>
      <xsl:copy-of select="@*[not(name()='title')]"/>
      <semantics>
        <xsl:call-template name="s:maybe-wrap-in-mrow">
          <xsl:with-param name="elements" as="element()*">
            <xsl:apply-templates/>
          </xsl:with-param>
        </xsl:call-template>
        <!-- Move the @title to an annotation -->
        <annotation encoding="{$s:asciimath-input-annotation}">
          <xsl:value-of select="normalize-space(@title)"/>
        </annotation>
      </semantics>
    </math>
  </xsl:template>

  <!-- Skip over pointless top-level <mstyle/> -->
  <xsl:template match="math[@title]/mstyle">
    <!-- Descend down -->
    <xsl:apply-templates/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!-- This is the entry point for the newer ASCIIMathParser.js output -->
  <xsl:template match="math[not(@title)]">
    <math>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </math>
  </xsl:template>

  <xsl:template match="semantics">
    <semantics>
      <!-- (Make sure first child maps to a single child) -->
      <xsl:call-template name="s:maybe-wrap-in-mrow">
        <xsl:with-param name="elements" as="element()*">
          <xsl:apply-templates select="*[1]"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:apply-templates select="*[position()!=1]"/>
    </semantics>
  </xsl:template>

  <xsl:template match="annotation|annotation-xml">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- ************************************************************ -->

  <!--
  ASCIIMath often outputs empty operators when it expects input that
  never actually occurs. I'm going to convert this to an empty <mrow/>
  -->
  <xsl:template match="mo[.='']">
    <mrow/>
  </xsl:template>

  <!-- ***************************************************************

  Fence balanced parentheses where possible. ASCIIMath outputs the
  following structure so it's quite easy to do:

  <mrow>
    <mo>...opener...</mo>
    ...
    <mo>...closer...</mo>
  </mrow>

  **************************************************************** -->

  <xsl:variable name="s:parentheses" as="element()+">
    <s:pair open='(' close=')'/>
    <s:pair open='[' close=']'/>
    <s:pair open='{{' close='}}'/>
    <s:pair open='&lt;' close='&gt;'/>
  </xsl:variable>

  <xsl:function name="s:is-open-parenthesis" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $s:parentheses[@open=$element]])"/>
  </xsl:function>

  <xsl:function name="s:is-close-parenthesis" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $s:parentheses[@close=$element]])"/>
  </xsl:function>

  <xsl:function name="s:is-separator" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and .=','])"/>
  </xsl:function>

  <!-- (This is no longer used as we're not forcing parentheses to match) -->
  <xsl:function name="s:get-matching-closer-value" as="xs:string">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="$s:parentheses[@open=$element]/@close"/>
  </xsl:function>

  <xsl:template match="mrow[*[1][self::mo and s:is-open-parenthesis(.)] and *[position()=last()][self::mo and s:is-close-parenthesis(.)]]">
    <xsl:variable name="opener" select="*[1]" as="element()"/>
    <xsl:variable name="contents" select="*[position() &gt; 1 and position() &lt; last()]" as="element()*"/>
    <xsl:variable name="closer" select="*[position()=last()]" as="element()"/>
    <mfenced open="{$opener}" close="{$closer}">
      <xsl:for-each-group select="$contents" group-adjacent="s:is-separator(.)">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <!-- Separator => ignore -->
          </xsl:when>
          <xsl:otherwise>
            <!-- Item => becomes single child so might need wrapped up -->
            <xsl:call-template name="s:maybe-wrap-in-mrow">
              <xsl:with-param name="elements" as="element()*">
                <xsl:apply-templates select="current-group()"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each-group>
    </mfenced>
  </xsl:template>

  <!-- ***************************************************************

  ASCIIMathML outputs elementary functions as <mo/> instead of <mi/> and
  always wraps the result in an <mrow/> since the operators are assumed
  to apply only to the next token, even though it is visually ambiguous.

  It's not currently safe to change this behaviour as the '/' operator
  only binds with the next token and is visually clear in this behaviour,
  so I'll keep this convention.

  (This is one area where ASCIIMathML deviates from SnuggleTeX.)

  FIXME: This goes horribly wrong if the <mrow/> represents the num/dom of
  a fraction or somrthing!!

  **************************************************************** -->

  <xsl:variable name="invertible-elementary-functions" as="xs:string+"
    select="('sin', 'cos', 'tan',
             'sec', 'csc' ,'cot',
             'sinh', 'cosh', 'tanh',
             'sech', 'csch', 'coth')"/>

  <xsl:variable name="elementary-functions" as="xs:string+"
    select="($invertible-elementary-functions,
            'ln', 'log', 'exp')"/>

  <xsl:function name="s:is-elementary-function" as="xs:boolean">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="boolean($element[self::mo and $elementary-functions=string(.)])"/>
  </xsl:function>

  <xsl:template match="mo[s:is-elementary-function(.)]">
    <mi>
      <xsl:value-of select="."/>
    </mi>
  </xsl:template>

  <!-- *********************************************************** -->

  <!-- Join unary minus and literal numbers together -->
  <xsl:template match="mo[.='-' and not(preceding-sibling::*) and following-sibling::*[1][self::mn]]">
    <mn>
      <xsl:text>-</xsl:text>
      <xsl:value-of select="following-sibling::*[1]"/>
    </mn>
  </xsl:template>

  <xsl:template match="mn[preceding-sibling::*[1][self::mo and .='-'] and not(preceding-sibling::*[2])]">
    <!-- This has been handled above -->
  </xsl:template>

  <!-- ***********************************************************

  f and g are treated as functions by default, but done in an odd
  way. This fixes them back up.

  **************************************************************** -->

  <xsl:template match="mrow[*[1][self::mi[.=('f','g')]] and *[2][self::mo[.='']]]" priority="1">
    <xsl:copy-of select="*[1]"/>
  </xsl:template>

  <xsl:template match="mrow[*[1][self::mi[.=('f','g')]]]">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <!-- *********************************************************** -->

  <!-- Strip off redundant <mrow/> elements -->
  <xsl:template match="mrow">
    <!-- Process children to see if we really need this -->
    <xsl:variable name="processed-children" as="element()*">
      <xsl:apply-templates/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($processed-children)=1">
        <xsl:copy-of select="$processed-children"/>
      </xsl:when>
      <xsl:otherwise>
        <mrow>
          <xsl:copy-of select="$processed-children"/>
        </mrow>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Handle over MathML elements as normal -->
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

