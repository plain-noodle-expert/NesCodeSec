/**
 * XXE Security Configuration Regex Rules for DocumentBuilder/DocumentBuilderFactory
 * 
 * This file contains regex patterns to detect secure configurations that prevent XXE attacks
 * when using javax.xml.parsers.DocumentBuilderFactory and DocumentBuilder.
 * 
 * XXE (XML External Entity) Attack Background:
 * - XXE attacks exploit XML parsers that process external entity references
 * - Attackers can read local files, perform SSRF, or cause DoS
 * - Default configurations of many XML parsers are vulnerable
 * 
 * Security Requirements for DocumentBuilderFactory:
 * DocumentBuilderFactory must be configured with specific features to prevent XXE:
 * 
 * 1. FEATURE_SECURE_PROCESSING (XMLConstants.FEATURE_SECURE_PROCESSING)
 *    - Enables secure processing mode
 *    - Pattern: factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
 * 
 * 2. Disallow DOCTYPE declarations
 *    - Completely disables DTD processing
 *    - Pattern: factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
 *    - This is the MOST SECURE option as it prevents all DTD-related attacks
 * 
 * 3. Disable external general entities
 *    - Prevents loading external general entities
 *    - Pattern: factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
 * 
 * 4. Disable external parameter entities
 *    - Prevents loading external parameter entities
 *    - Pattern: factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
 * 
 * 5. Disable loading external DTD
 *    - Pattern: factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
 * 
 * 6. Disable DTD grammar loading
 *    - Pattern: factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
 * 
 * Best Practice:
 * - Use disallow-doctype-decl if your application doesn't need DTD
 * - If DTD is needed, disable external entities (both general and parameter)
 * - Always combine FEATURE_SECURE_PROCESSING with entity restrictions
 */

package com.XXE.regex_evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DocumentBuilder_regex_rules {
    
    /**
     * Regex pattern to detect FEATURE_SECURE_PROCESSING configuration
     * Matches: factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
     * Allows for variations in whitespace and variable names
     */
    public static final String FEATURE_SECURE_PROCESSING_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect disallow-doctype-decl configuration
     * Matches: factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
     * This is the STRONGEST protection - completely disables DTD processing
     */
    public static final String DISALLOW_DOCTYPE_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled
     * Matches: factory.setFeature("http://xml.org/sax/features/external-general-entities", false)
     * Prevents loading of external general entities (e.g., <!ENTITY xxe SYSTEM "file:///etc/passwd">)
     */
    public static final String EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect external-parameter-entities disabled
     * Matches: factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
     * Prevents loading of external parameter entities (used in DTD definitions)
     */
    public static final String EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-external-dtd disabled
     * Matches: factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
     * Prevents loading external DTD files
     */
    public static final String LOAD_EXTERNAL_DTD_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-dtd-grammar disabled
     * Matches: factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
     * Prevents loading DTD grammar
     */
    public static final String LOAD_DTD_GRAMMAR_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-dtd-grammar[\"']\\s*,\\s*false\\s*\\)";

    /**
     * Regex pattern to detect XInclude disabled
     * Matches: factory.setXIncludeAware(false)
     */
    public static final String XINCLUDE_DISABLED_PATTERN =
        "\\w+\\.setXIncludeAware\\s*\\(\\s*false\\s*\\)";

    /**
     * Regex pattern to detect entity expansion disabled
     * Matches: factory.setExpandEntityReferences(false)
     */
    public static final String EXPAND_ENTITY_REFERENCES_DISABLED_PATTERN =
        "\\w+\\.setExpandEntityReferences\\s*\\(\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect DocumentBuilderFactory instantiation
     * Matches: DocumentBuilderFactory.newInstance() or DocumentBuilderFactory factory = ...
     * Used to identify code blocks that need security configuration
     */
    public static final String DOCUMENT_BUILDER_FACTORY_CREATION_PATTERN = 
        "DocumentBuilderFactory\\s*\\w*\\s*=\\s*DocumentBuilderFactory\\.newInstance\\s*\\(\\s*\\)";
    
    /**
     * Composite pattern for MINIMUM secure configuration
     * A DocumentBuilderFactory is considered minimally secure if it has:
     * - Either disallow-doctype-decl enabled (STRONGEST)
     * - OR both external-general-entities AND external-parameter-entities disabled
     * 
     * Usage: Check if code block contains factory creation followed by security configs
     */
    public static final String MINIMAL_SECURE_CONFIG_PATTERN = 
        "(" + DISALLOW_DOCTYPE_PATTERN + ")|" +
        "(" + EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ".*" + EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + ")";
    
    /**
     * Comprehensive secure configuration pattern
     * Includes all recommended security features:
     * - FEATURE_SECURE_PROCESSING
     * - disallow-doctype-decl OR (external-general-entities + external-parameter-entities)
     * - load-external-dtd disabled
     * 
     * This represents BEST PRACTICE configuration
     */
    public static final String COMPREHENSIVE_SECURE_CONFIG_PATTERN = 
        "(" + FEATURE_SECURE_PROCESSING_PATTERN + ").*(" + 
        DISALLOW_DOCTYPE_PATTERN + "|" +
        "(" + EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ".*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + "))";

    /**
     * Regex rule that ensures DocumentBuilderFactory enables secure processing,
     * blocks XXE vectors, and prevents XML bomb style DoS by disabling external DTD loading.
     */
    public static final String ESSENTIAL_SECURITY_RULE_PATTERN =
        "(?=.*\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\))"
        + "(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\))"
        + "(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\))"
        + "(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\))"
        + "(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\))";
    
    /**
     * Anti-pattern: Unsafe DocumentBuilderFactory usage
     * Detects DocumentBuilderFactory creation WITHOUT proper security configuration
     * This pattern should NOT match in secure code
     */
    public static final String UNSAFE_DOCUMENT_BUILDER_PATTERN = 
        "DocumentBuilderFactory\\.newInstance\\s*\\(\\s*\\)(?!.*setFeature.*(?:disallow-doctype-decl|external-general-entities))";
    
    /**
     * Get all compiled patterns for secure configurations
     * @return List of compiled Pattern objects for security validation
     */
    public static List<Pattern> getSecureConfigPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile(FEATURE_SECURE_PROCESSING_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(DISALLOW_DOCTYPE_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_EXTERNAL_DTD_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_DTD_GRAMMAR_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(XINCLUDE_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(EXPAND_ENTITY_REFERENCES_DISABLED_PATTERN, Pattern.DOTALL));
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
        return Pattern.compile(UNSAFE_DOCUMENT_BUILDER_PATTERN, Pattern.DOTALL);
    }
}
