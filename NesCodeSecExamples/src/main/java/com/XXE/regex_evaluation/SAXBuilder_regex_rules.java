/**
 * XXE Security Configuration Regex Rules for SAXBuilder (JDOM2)
 * 
 * This file contains regex patterns to detect secure configurations that prevent XXE attacks
 * when using org.jdom2.input.SAXBuilder from the JDOM2 library.
 * 
 * XXE (XML External Entity) Attack Background:
 * - JDOM2 SAXBuilder is a popular wrapper around SAX parsers
 * - Provides a simpler API for building DOM trees from XML
 * - Default configuration is vulnerable to XXE attacks
 * 
 * Security Requirements for SAXBuilder:
 * SAXBuilder must be configured with specific features to prevent XXE:
 * 
 * 1. Disallow DOCTYPE declarations (STRONGEST PROTECTION)
 *    - Completely disables DTD processing
 *    - Pattern: builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
 *    - Throws JDOMException if DOCTYPE is encountered
 * 
 * 2. Disable external general entities
 *    - Prevents loading external general entities
 *    - Pattern: builder.setFeature("http://apache.org/xml/features/external-general-entities", false)
 *    - Note: Apache feature prefix, not xml.org (JDOM2 specific)
 * 
 * 3. Disable external parameter entities
 *    - Prevents loading external parameter entities
 *    - Pattern: builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
 * 
 * 4. FEATURE_SECURE_PROCESSING
 *    - Enables secure processing mode
 *    - Pattern: builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
 * 
 * 5. Disable DTD processing entirely
 *    - Pattern: builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
 * 
 * Important Notes for SAXBuilder:
 * - SAXBuilder uses underlying SAX parser (often Xerces)
 * - Feature names may use Apache-specific prefixes
 * - setFeature() throws SAXException if feature not supported
 * - JDOM2 provides cleaner exception handling than raw SAX
 * 
 * Best Practice for SAXBuilder:
 * - Always use disallow-doctype-decl if DTD is not needed
 * - Disable both types of external entities if DTD is required
 * - Combine with FEATURE_SECURE_PROCESSING for defense in depth
 */

package com.XXE.regex_evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SAXBuilder_regex_rules {
    
    /**
     * Regex pattern to detect FEATURE_SECURE_PROCESSING configuration
     * Matches: builder.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
     * Enables secure processing with resource limits
     */
    public static final String FEATURE_SECURE_PROCESSING_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect disallow-doctype-decl configuration
     * Matches: builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
     * STRONGEST PROTECTION - Completely prevents DTD processing
     * Note: Uses Apache feature prefix (common in Xerces-based parsers)
     */
    public static final String DISALLOW_DOCTYPE_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled (Apache version)
     * Matches: builder.setFeature("http://apache.org/xml/features/external-general-entities", false)
     * JDOM2 specific: Uses Apache prefix instead of xml.org
     * Prevents external entity resolution like: <!ENTITY xxe SYSTEM "file:///etc/passwd">
     */
    public static final String APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled (SAX version)
     * Matches: builder.setFeature("http://xml.org/sax/features/external-general-entities", false)
     * Alternative SAX standard version - also works with SAXBuilder
     */
    public static final String SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Combined pattern for external-general-entities (either Apache or SAX)
     * Accepts both feature URI formats
     */
    public static final String EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "(" + APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + "|" + 
        SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ")";
    
    /**
     * Regex pattern to detect external-parameter-entities disabled
     * Matches: builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
     * Prevents external parameter entities in DTD
     */
    public static final String EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-external-dtd disabled
     * Matches: builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
     * Prevents loading external DTD files
     */
    public static final String LOAD_EXTERNAL_DTD_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-dtd-grammar disabled
     * Matches: builder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
     * Prevents DTD grammar processing
     */
    public static final String LOAD_DTD_GRAMMAR_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-dtd-grammar[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect validation disabled
     * Matches: builder.setFeature("http://xml.org/sax/features/validation", false)
     * Disables DTD validation
     */
    public static final String VALIDATION_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/validation[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect SAXBuilder instantiation
     * Matches: new SAXBuilder() or SAXBuilder builder = new SAXBuilder()
     * Used to identify code blocks that need security configuration
     */
    public static final String SAX_BUILDER_CREATION_PATTERN = 
        "new\\s+SAXBuilder\\s*\\(\\s*\\)";
    
    /**
     * Regex pattern to detect SAXBuilder.build() method calls
     * Matches: builder.build(file) or builder.build(inputStream)
     * Identifies where XML parsing actually occurs
     */
    public static final String SAX_BUILDER_BUILD_PATTERN = 
        "\\w+\\.build\\s*\\([^)]+\\)";
    
    /**
     * Composite pattern for MINIMUM secure configuration
     * A SAXBuilder is considered minimally secure if it has:
     * - Either disallow-doctype-decl enabled (STRONGEST)
     * - OR both external entities disabled (general + parameter)
     * 
     * Accepts both Apache and SAX feature URI formats
     */
    public static final String MINIMAL_SECURE_CONFIG_PATTERN = 
        "(" + DISALLOW_DOCTYPE_PATTERN + ")|" +
        "((?:" + APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + "|" + 
        SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ").*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + ")";
    
    /**
     * Comprehensive secure configuration pattern (BEST PRACTICE)
     * Includes all recommended security features:
     * - FEATURE_SECURE_PROCESSING
     * - disallow-doctype-decl OR (external-general-entities + external-parameter-entities)
     * - load-external-dtd disabled
     * 
     * This represents the GOLD STANDARD for SAXBuilder security
     */
    public static final String COMPREHENSIVE_SECURE_CONFIG_PATTERN = 
        "(" + FEATURE_SECURE_PROCESSING_PATTERN + ").*(" + 
        DISALLOW_DOCTYPE_PATTERN + "|" +
        "((?:" + APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + "|" + 
        SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ").*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + "))";

    /**
     * Regex rule requiring SAXBuilder to enable secure processing, disable validation/DTD loading,
     * and block XXE via disallow-doctype or both external entity toggles.
     */
    public static final String ESSENTIAL_SECURITY_RULE_PATTERN =
        "(?=.*\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/validation[\"']\\s*,\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\))(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\))|(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*(?:[\"']http://apache\\.org/xml/features/external-general-entities[\"']|[\"']http://xml\\.org/sax/features/external-general-entities[\"'])\\s*,\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\))))";
    
    /**
     * Anti-pattern: Unsafe SAXBuilder usage
     * Detects SAXBuilder creation WITHOUT proper security configuration
     * This pattern should NOT match in secure code - indicates XXE vulnerability
     */
    public static final String UNSAFE_SAX_BUILDER_PATTERN = 
        "new\\s+SAXBuilder\\s*\\(\\s*\\)(?!.*setFeature.*(?:disallow-doctype-decl|external-general-entities))";
    
    /**
     * Get all compiled patterns for secure configurations
     * @return List of compiled Pattern objects for security validation
     */
    public static List<Pattern> getSecureConfigPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile(FEATURE_SECURE_PROCESSING_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(DISALLOW_DOCTYPE_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_EXTERNAL_DTD_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_DTD_GRAMMAR_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(VALIDATION_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ESSENTIAL_SECURITY_RULE_PATTERN, Pattern.DOTALL));
        return patterns;
    }
    
    /**
     * Get the minimal secure configuration pattern
     * Code must match this pattern to be considered minimally secure
     * @return Compiled Pattern for minimal security check
     */
    public static Pattern getMinimalSecurePattern() {
        return Pattern.compile(MINIMAL_SECURE_CONFIG_PATTERN, Pattern.DOTALL);
    }
    
    /**
     * Get the comprehensive secure configuration pattern
     * Code matching this pattern follows all best practices
     * @return Compiled Pattern for comprehensive security check
     */
    public static Pattern getComprehensiveSecurePattern() {
        return Pattern.compile(COMPREHENSIVE_SECURE_CONFIG_PATTERN, Pattern.DOTALL);
    }
    
    /**
     * Get the unsafe usage pattern
     * Code matching this pattern is VULNERABLE to XXE attacks
     * @return Compiled Pattern for vulnerability detection
     */
    public static Pattern getUnsafePattern() {
        return Pattern.compile(UNSAFE_SAX_BUILDER_PATTERN, Pattern.DOTALL);
    }
}
