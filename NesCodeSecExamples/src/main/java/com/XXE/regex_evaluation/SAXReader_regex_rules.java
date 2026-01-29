/**
 * XXE Security Configuration Regex Rules for SAXReader (DOM4J)
 * 
 * This file contains regex patterns to detect secure configurations that prevent XXE attacks
 * when using org.dom4j.io.SAXReader from the DOM4J library.
 * 
 * XXE (XML External Entity) Attack Background:
 * - DOM4J is a popular XML processing library for Java
 * - SAXReader is the main class for parsing XML into DOM4J Document objects
 * - Default configuration is vulnerable to XXE attacks
 * - DOM4J 2.1.1+ has improved security but still requires explicit configuration
 * 
 * Security Requirements for SAXReader:
 * SAXReader must be configured with specific features to prevent XXE:
 * 
 * 1. Disallow DOCTYPE declarations (STRONGEST PROTECTION)
 *    - Completely disables DTD processing
 *    - Pattern: reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
 * 
 * 2. Disable external general entities
 *    - Prevents loading external general entities
 *    - Pattern: reader.setFeature("http://xml.org/sax/features/external-general-entities", false)
 * 
 * 3. Disable external parameter entities
 *    - Prevents loading external parameter entities
 *    - Pattern: reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
 * 
 * 4. FEATURE_SECURE_PROCESSING
 *    - Enables secure processing mode
 *    - Pattern: reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
 * 
 * 5. Disable loading external DTD
 *    - Pattern: reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
 * 
 * 6. Set validation to false
 *    - Pattern: reader.setValidation(false)
 * 
 * 7. Set EntityResolver to null or custom secure resolver
 *    - Pattern: reader.setEntityResolver(null)
 *    - Prevents entity resolution entirely
 * 
 * Important Notes for SAXReader:
 * - DOM4J wraps SAX parsers (usually Xerces)
 * - Feature setting may throw SAXException
 * - Some features may not be supported by all parsers
 * - EntityResolver can be used for additional control
 * 
 * Best Practice for SAXReader:
 * - Use disallow-doctype-decl if DTD is not needed
 * - Set validation to false
 * - Disable both external entity types
 * - Consider setting EntityResolver to null or secure implementation
 */

package com.XXE.regex_evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SAXReader_regex_rules {
    
    /**
     * Regex pattern to detect FEATURE_SECURE_PROCESSING configuration
     * Matches: reader.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
     * Enables secure processing with resource limits
     */
    public static final String FEATURE_SECURE_PROCESSING_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect disallow-doctype-decl configuration
     * Matches: reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
     * STRONGEST PROTECTION - Completely prevents DTD processing
     */
    public static final String DISALLOW_DOCTYPE_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled
     * Matches: reader.setFeature("http://xml.org/sax/features/external-general-entities", false)
     * Prevents external entity resolution
     */
    public static final String EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect external-parameter-entities disabled
     * Matches: reader.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
     * Prevents external parameter entities in DTD
     */
    public static final String EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-external-dtd disabled
     * Matches: reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
     * Prevents loading external DTD files
     */
    public static final String LOAD_EXTERNAL_DTD_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect validation disabled
     * Matches: reader.setValidation(false)
     * DOM4J-specific method to disable DTD validation
     */
    public static final String VALIDATION_DISABLED_PATTERN = 
        "\\w+\\.setValidation\\s*\\(\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect EntityResolver set to null
     * Matches: reader.setEntityResolver(null)
     * Prevents any entity resolution by removing resolver
     */
    public static final String ENTITY_RESOLVER_NULL_PATTERN = 
        "\\w+\\.setEntityResolver\\s*\\(\\s*null\\s*\\)";
    
    /**
     * Regex pattern to detect custom secure EntityResolver
     * Matches: reader.setEntityResolver(new SecureEntityResolver())
     * Allows controlled entity resolution with custom security logic
     */
    public static final String ENTITY_RESOLVER_CUSTOM_PATTERN = 
        "\\w+\\.setEntityResolver\\s*\\(\\s*new\\s+\\w+EntityResolver\\s*\\(\\s*\\)\\s*\\)";
    
    /**
     * Regex pattern to detect SAXReader instantiation
     * Matches: new SAXReader() or SAXReader reader = new SAXReader()
     * Used to identify code blocks that need security configuration
     */
    public static final String SAX_READER_CREATION_PATTERN = 
        "new\\s+SAXReader\\s*\\(\\s*\\)";
    
    /**
     * Regex pattern to detect SAXReader.read() method calls
     * Matches: reader.read(file) or reader.read(inputStream)
     * Identifies where XML parsing actually occurs
     */
    public static final String SAX_READER_READ_PATTERN = 
        "\\w+\\.read\\s*\\([^)]+\\)";
    
    /**
     * Composite pattern for MINIMUM secure configuration
     * A SAXReader is considered minimally secure if it has:
     * - Either disallow-doctype-decl enabled (STRONGEST)
     * - OR both external entities disabled (general + parameter)
     * - AND validation disabled
     */
    public static final String MINIMAL_SECURE_CONFIG_PATTERN = 
        "(" + VALIDATION_DISABLED_PATTERN + ").*(" +
        "(" + DISALLOW_DOCTYPE_PATTERN + ")|" +
        "(" + EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ".*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + "))";
    
    /**
     * Alternative minimal secure pattern with EntityResolver
     * Uses EntityResolver set to null as primary defense
     * - EntityResolver set to null
     * - AND validation disabled
     */
    public static final String MINIMAL_SECURE_WITH_RESOLVER_PATTERN = 
        "(" + VALIDATION_DISABLED_PATTERN + ").*(" + ENTITY_RESOLVER_NULL_PATTERN + ")";
    
    /**
     * Comprehensive secure configuration pattern (BEST PRACTICE)
     * Includes all recommended security features:
     * - FEATURE_SECURE_PROCESSING
     * - Validation disabled
     * - disallow-doctype-decl OR (external-general-entities + external-parameter-entities)
     * - load-external-dtd disabled
     * - EntityResolver set to null or custom secure implementation
     */
    public static final String COMPREHENSIVE_SECURE_CONFIG_PATTERN = 
        "(" + VALIDATION_DISABLED_PATTERN + ").*" +
        "(" + FEATURE_SECURE_PROCESSING_PATTERN + ").*(" + 
        DISALLOW_DOCTYPE_PATTERN + "|" +
        "(" + EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ".*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + ")).*" +
        "(?:" + ENTITY_RESOLVER_NULL_PATTERN + "|" + ENTITY_RESOLVER_CUSTOM_PATTERN + ")";

    /**
     * Regex rule requiring SAXReader to disable validation, enable secure processing,
     * block external DTDs, and either disallow DOCTYPE or turn off both external entity types,
     * while also forcing a safe EntityResolver to mitigate XXE/DoS risks.
     */
    public static final String ESSENTIAL_SECURITY_RULE_PATTERN =
        "(?=.*\\w+\\.setValidation\\s*\\(\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\))(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\))|(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\))))(?:(?=.*\\w+\\.setEntityResolver\\s*\\(\\s*null\\s*\\))|(?=.*\\w+\\.setEntityResolver\\s*\\(\\s*new\\s+\\w+EntityResolver\\s*\\(\\s*\\)\\s*\\)))";
    
    /**
     * Anti-pattern: Unsafe SAXReader usage
     * Detects SAXReader creation WITHOUT proper security configuration
     * This pattern should NOT match in secure code - indicates XXE vulnerability
     */
    public static final String UNSAFE_SAX_READER_PATTERN = 
        "new\\s+SAXReader\\s*\\(\\s*\\)(?!.*(?:setFeature.*(?:disallow-doctype-decl|external-general-entities)|setValidation|setEntityResolver))";
    
    /**
     * Anti-pattern: Validation enabled
     * Matches: reader.setValidation(true)
     * Enabling validation can enable DTD processing - potentially unsafe
     */
    public static final String VALIDATION_ENABLED_PATTERN = 
        "\\w+\\.setValidation\\s*\\(\\s*true\\s*\\)";
    
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
        patterns.add(Pattern.compile(VALIDATION_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ENTITY_RESOLVER_NULL_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(ENTITY_RESOLVER_CUSTOM_PATTERN, Pattern.DOTALL));
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
     * Get the minimal secure pattern with EntityResolver
     * Alternative minimal security using EntityResolver
     * @return Compiled Pattern for minimal security check with EntityResolver
     */
    public static Pattern getMinimalSecureWithResolverPattern() {
        return Pattern.compile(MINIMAL_SECURE_WITH_RESOLVER_PATTERN, Pattern.DOTALL);
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
        return Pattern.compile(UNSAFE_SAX_READER_PATTERN, Pattern.DOTALL);
    }
    
    /**
     * Get the validation enabled pattern (potential vulnerability)
     * Detects when validation is explicitly enabled
     * @return Compiled Pattern for detecting potentially unsafe validation
     */
    public static Pattern getValidationEnabledPattern() {
        return Pattern.compile(VALIDATION_ENABLED_PATTERN, Pattern.DOTALL);
    }
}
