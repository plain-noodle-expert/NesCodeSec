/**
 * XXE Security Configuration Regex Rules for XMLInputFactory (StAX)
 * 
 * This file contains regex patterns to detect secure configurations that prevent XXE attacks
 * when using javax.xml.stream.XMLInputFactory from the StAX (Streaming API for XML) library.
 * 
 * XXE (XML External Entity) Attack Background:
 * - StAX (Streaming API for XML) is a pull-parsing API introduced in Java 6
 * - XMLInputFactory creates XMLStreamReader for streaming XML processing
 * - Default configuration is vulnerable to XXE attacks
 * - StAX is memory-efficient for large XML files but still needs security hardening
 * 
 * Security Requirements for XMLInputFactory:
 * XMLInputFactory must be configured with specific properties to prevent XXE:
 * 
 * 1. Disable DTD support (STRONGEST PROTECTION)
 *    - Completely disables DTD processing
 *    - Pattern: factory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
 *    - This is the MOST EFFECTIVE defense for StAX parsers
 * 
 * 2. Disable external entities support
 *    - Pattern: factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
 *    - Prevents resolution of external entities
 * 
 * 3. Enable secure processing
 *    - Pattern: factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
 *    - Restricts access to external DTD
 * 
 * 4. Disable external entity access
 *    - Pattern: factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
 *    - Restricts access to external schema
 * 
 * Important Notes for XMLInputFactory:
 * - StAX uses property names instead of feature URIs
 * - Properties are set with setProperty(), not setFeature()
 * - SUPPORT_DTD is a boolean property (true/false)
 * - IS_SUPPORTING_EXTERNAL_ENTITIES is also boolean
 * - XMLConstants.ACCESS_EXTERNAL_DTD requires empty string "" to deny access
 * 
 * Best Practice for XMLInputFactory:
 * - ALWAYS set SUPPORT_DTD to false if DTD is not needed
 * - Set IS_SUPPORTING_EXTERNAL_ENTITIES to false
 * - Set ACCESS_EXTERNAL_DTD to "" (empty string)
 * - Combine all three for maximum protection
 * 
 * Common Mistakes:
 * - Setting ACCESS_EXTERNAL_DTD to "file" or "http" (UNSAFE!)
 * - Forgetting to disable SUPPORT_DTD
 * - Enabling IS_SUPPORTING_EXTERNAL_ENTITIES (should be false)
 */

package com.XXE.regex_evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class InputFactory_regex_rules {
    
    /**
     * Regex pattern to detect SUPPORT_DTD disabled
     * Matches: factory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
     * STRONGEST PROTECTION for StAX - Completely disables DTD processing
     * This is the PRIMARY defense mechanism for XMLInputFactory
     */
    public static final String SUPPORT_DTD_DISABLED_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLInputFactory\\.SUPPORT_DTD\\s*,\\s*(?:false|Boolean\\.FALSE)\\s*\\)";
    
    /**
     * Regex pattern to detect SUPPORT_DTD disabled using string property name
     * Matches: factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false)
     * Alternative way to disable DTD using string constant
     */
    public static final String SUPPORT_DTD_DISABLED_STRING_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*[\"']javax\\.xml\\.stream\\.supportDTD[\"']\\s*,\\s*(?:false|Boolean\\.FALSE)\\s*\\)";
    
    /**
     * Regex pattern to detect IS_SUPPORTING_EXTERNAL_ENTITIES disabled
     * Matches: factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
     * Prevents resolution of external entities
     */
    public static final String IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLInputFactory\\.IS_SUPPORTING_EXTERNAL_ENTITIES\\s*,\\s*(?:false|Boolean\\.FALSE)\\s*\\)";
    
    /**
     * Regex pattern to detect IS_SUPPORTING_EXTERNAL_ENTITIES disabled using string
     * Matches: factory.setProperty("javax.xml.stream.isSupportingExternalEntities", false)
     * Alternative string-based property name
     */
    public static final String IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*[\"']javax\\.xml\\.stream\\.isSupportingExternalEntities[\"']\\s*,\\s*(?:false|Boolean\\.FALSE)\\s*\\)";
    
    /**
     * Regex pattern to detect ACCESS_EXTERNAL_DTD restricted
     * Matches: factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "")
     * Empty string "" denies all external DTD access
     */
    public static final String ACCESS_EXTERNAL_DTD_RESTRICTED_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLConstants\\.ACCESS_EXTERNAL_DTD\\s*,\\s*[\"'][\"']\\s*\\)";
    
    /**
     * Regex pattern to detect ACCESS_EXTERNAL_DTD using string property name
     * Matches: factory.setProperty("http://javax.xml.XMLConstants/property/accessExternalDTD", "")
     * Alternative way using full property name string
     */
    public static final String ACCESS_EXTERNAL_DTD_RESTRICTED_STRING_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*[\"']http://javax\\.xml\\.XMLConstants/property/accessExternalDTD[\"']\\s*,\\s*[\"'][\"']\\s*\\)";
    
    /**
     * Regex pattern to detect ACCESS_EXTERNAL_SCHEMA restricted
     * Matches: factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "")
     * Prevents external schema access
     */
    public static final String ACCESS_EXTERNAL_SCHEMA_RESTRICTED_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLConstants\\.ACCESS_EXTERNAL_SCHEMA\\s*,\\s*[\"'][\"']\\s*\\)";
    
    /**
     * Regex pattern to detect XMLInputFactory instantiation
     * Matches: XMLInputFactory.newInstance() or XMLInputFactory factory = ...
     * Used to identify code blocks that need security configuration
     */
    public static final String XML_INPUT_FACTORY_CREATION_PATTERN = 
        "XMLInputFactory\\s*\\w*\\s*=\\s*XMLInputFactory\\.newInstance\\s*\\(\\s*\\)";
    
    /**
     * Regex pattern to detect XMLStreamReader creation
     * Matches: factory.createXMLStreamReader(input)
     * Identifies where XML parsing actually occurs
     */
    public static final String XML_STREAM_READER_CREATION_PATTERN = 
        "\\w+\\.createXMLStreamReader\\s*\\([^)]+\\)";
    
    /**
     * Composite pattern for MINIMUM secure configuration
     * An XMLInputFactory is considered minimally secure if it has:
     * - SUPPORT_DTD disabled (PRIMARY DEFENSE)
     * - OR IS_SUPPORTING_EXTERNAL_ENTITIES disabled
     * 
     * Note: SUPPORT_DTD=false is preferred as it's more comprehensive
     */
    public static final String MINIMAL_SECURE_CONFIG_PATTERN = 
        "(" + SUPPORT_DTD_DISABLED_PATTERN + "|" + SUPPORT_DTD_DISABLED_STRING_PATTERN + ")|" +
        "(" + IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN + "|" + 
        IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN + ")";
    
    /**
     * Comprehensive secure configuration pattern (BEST PRACTICE)
     * Includes all recommended security properties:
     * - SUPPORT_DTD disabled
     * - IS_SUPPORTING_EXTERNAL_ENTITIES disabled
     * - ACCESS_EXTERNAL_DTD restricted to ""
     * - ACCESS_EXTERNAL_SCHEMA restricted to ""
     * 
     * This represents DEFENSE IN DEPTH for XMLInputFactory
     */
    public static final String COMPREHENSIVE_SECURE_CONFIG_PATTERN = 
        "(" + SUPPORT_DTD_DISABLED_PATTERN + "|" + SUPPORT_DTD_DISABLED_STRING_PATTERN + ").*" +
        "(" + IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN + "|" + 
        IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN + ").*" +
        "(" + ACCESS_EXTERNAL_DTD_RESTRICTED_PATTERN + "|" + 
        ACCESS_EXTERNAL_DTD_RESTRICTED_STRING_PATTERN + ")";
    
    /**
     * Anti-pattern: Unsafe XMLInputFactory usage
     * Detects XMLInputFactory creation WITHOUT proper security configuration
     * This pattern should NOT match in secure code - indicates XXE vulnerability
     */
    public static final String UNSAFE_XML_INPUT_FACTORY_PATTERN = 
        "XMLInputFactory\\.newInstance\\s*\\(\\s*\\)(?!.*setProperty.*(?:SUPPORT_DTD|IS_SUPPORTING_EXTERNAL_ENTITIES|supportDTD))";
    
    /**
     * Anti-pattern: SUPPORT_DTD enabled
     * Matches: factory.setProperty(XMLInputFactory.SUPPORT_DTD, true)
     * Explicitly enabling DTD support is UNSAFE
     */
    public static final String SUPPORT_DTD_ENABLED_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLInputFactory\\.SUPPORT_DTD\\s*,\\s*(?:true|Boolean\\.TRUE)\\s*\\)";
    
    /**
     * Anti-pattern: IS_SUPPORTING_EXTERNAL_ENTITIES enabled
     * Matches: factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, true)
     * Enabling external entities support is UNSAFE
     */
    public static final String IS_SUPPORTING_EXTERNAL_ENTITIES_ENABLED_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLInputFactory\\.IS_SUPPORTING_EXTERNAL_ENTITIES\\s*,\\s*(?:true|Boolean\\.TRUE)\\s*\\)";
    
    /**
     * Anti-pattern: ACCESS_EXTERNAL_DTD allowing access
     * Matches: factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "file") or "http"
     * Non-empty ACCESS_EXTERNAL_DTD allows external access - UNSAFE
     */
    public static final String ACCESS_EXTERNAL_DTD_UNSAFE_PATTERN = 
        "\\w+\\.setProperty\\s*\\(\\s*XMLConstants\\.ACCESS_EXTERNAL_DTD\\s*,\\s*[\"'](?!\\s*[\"'])[^\"']+[\"']\\s*\\)";

    /**
     * Regex rule requiring XMLInputFactory to disable DTDs, block external entities,
     * and lock down both ACCESS_EXTERNAL_DTD and ACCESS_EXTERNAL_SCHEMA for XXE/DoS defense.
     */
    public static final String ESSENTIAL_SECURITY_RULE_PATTERN =
        "(?=.*\\w+\\.setProperty\\s*\\(\\s*(?:XMLInputFactory\\.SUPPORT_DTD|[\"']javax\\.xml\\.stream\\.supportDTD[\"'])\\s*,\\s*(?:false|Boolean\\.FALSE)\\s*\\))"
        + "(?=.*\\w+\\.setProperty\\s*\\(\\s*(?:XMLInputFactory\\.IS_SUPPORTING_EXTERNAL_ENTITIES|[\"']javax\\.xml\\.stream\\.isSupportingExternalEntities[\"'])\\s*,\\s*(?:false|Boolean\\.FALSE)\\s*\\))";
    
    /**
     * Get all compiled patterns for secure configurations
     * @return List of compiled Pattern objects for security validation
     */
    public static List<Pattern> getSecureConfigPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile(SUPPORT_DTD_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(SUPPORT_DTD_DISABLED_STRING_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ACCESS_EXTERNAL_DTD_RESTRICTED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ACCESS_EXTERNAL_DTD_RESTRICTED_STRING_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ACCESS_EXTERNAL_SCHEMA_RESTRICTED_PATTERN, Pattern.DOTALL));
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
        return Pattern.compile(UNSAFE_XML_INPUT_FACTORY_PATTERN, Pattern.DOTALL);
    }
    
    /**
     * Get all unsafe configuration patterns
     * These patterns detect explicitly unsafe configurations
     * @return List of compiled Pattern objects for unsafe configuration detection
     */
    public static List<Pattern> getUnsafeConfigPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile(SUPPORT_DTD_ENABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(IS_SUPPORTING_EXTERNAL_ENTITIES_ENABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ACCESS_EXTERNAL_DTD_UNSAFE_PATTERN, Pattern.DOTALL));
        return patterns;
    }
}
