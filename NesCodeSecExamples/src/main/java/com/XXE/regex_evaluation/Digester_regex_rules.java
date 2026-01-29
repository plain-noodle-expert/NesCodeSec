/**
 * XXE Security Configuration Regex Rules for Digester (Apache Commons)
 * 
 * This file contains regex patterns to detect secure configurations that prevent XXE attacks
 * when using org.apache.commons.digester.Digester from Apache Commons Digester library.
 * 
 * XXE (XML External Entity) Attack Background:
 * - Apache Commons Digester is a rule-based XML to Java object mapping library
 * - Built on top of SAX parser
 * - Default configuration is vulnerable to XXE attacks
 * - Widely used in older Java applications and frameworks
 * - Version 3.0+ has some security improvements but still needs explicit configuration
 * 
 * Security Requirements for Digester:
 * Digester must be configured with specific features to prevent XXE:
 * 
 * 1. Disallow DOCTYPE declarations (STRONGEST PROTECTION)
 *    - Completely disables DTD processing
 *    - Pattern: digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
 * 
 * 2. Disable external general entities
 *    - Prevents loading external general entities
 *    - Pattern: digester.setFeature("http://xml.org/sax/features/external-general-entities", false)
 *    - Note: Can also use Apache prefix: http://apache.org/xml/features/external-general-entities
 * 
 * 3. Disable external parameter entities
 *    - Prevents loading external parameter entities
 *    - Pattern: digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
 * 
 * 4. FEATURE_SECURE_PROCESSING
 *    - Enables secure processing mode
 *    - Pattern: digester.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
 * 
 * 5. Disable loading external DTD
 *    - Pattern: digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
 * 
 * 6. Set validation to false
 *    - Pattern: digester.setValidating(false)
 *    - Digester-specific method
 * 
 * 7. Use custom EntityResolver
 *    - Pattern: digester.setEntityResolver(new EmptyEntityResolver())
 *    - Prevents entity resolution entirely
 * 
 * Important Notes for Digester:
 * - Digester wraps SAX parser, so SAX features apply
 * - setFeature() method throws SAXException if feature not supported
 * - setValidating(false) is Digester-specific convenience method
 * - EmptyEntityResolver is a common pattern to block all entity resolution
 * - Digester may use internal XMLReader which also needs configuration
 * 
 * Best Practice for Digester:
 * - Use disallow-doctype-decl if DTD is not needed
 * - Set validating to false
 * - Disable both external entity types
 * - Use EmptyEntityResolver or similar secure implementation
 * - Consider upgrading to Digester 3.0+ for better security defaults
 */

package com.XXE.regex_evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Digester_regex_rules {
    
    /**
     * Regex pattern to detect FEATURE_SECURE_PROCESSING configuration
     * Matches: digester.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
     * Enables secure processing with resource limits
     */
    public static final String FEATURE_SECURE_PROCESSING_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect disallow-doctype-decl configuration
     * Matches: digester.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
     * STRONGEST PROTECTION - Completely prevents DTD processing
     */
    public static final String DISALLOW_DOCTYPE_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled (SAX version)
     * Matches: digester.setFeature("http://xml.org/sax/features/external-general-entities", false)
     * Standard SAX feature URI
     */
    public static final String SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect external-general-entities disabled (Apache version)
     * Matches: digester.setFeature("http://apache.org/xml/features/external-general-entities", false)
     * Apache-specific feature URI (also works with Digester)
     */
    public static final String APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/external-general-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Combined pattern for external-general-entities (either SAX or Apache)
     * Accepts both feature URI formats
     */
    public static final String EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN = 
        "(" + SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + "|" + 
        APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ")";
    
    /**
     * Regex pattern to detect external-parameter-entities disabled
     * Matches: digester.setFeature("http://xml.org/sax/features/external-parameter-entities", false)
     * Prevents external parameter entities in DTD
     */
    public static final String EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-external-dtd disabled
     * Matches: digester.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
     * Prevents loading external DTD files
     */
    public static final String LOAD_EXTERNAL_DTD_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect load-dtd-grammar disabled
     * Matches: digester.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
     * Prevents DTD grammar processing
     */
    public static final String LOAD_DTD_GRAMMAR_DISABLED_PATTERN = 
        "\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-dtd-grammar[\"']\\s*,\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect validation disabled
     * Matches: digester.setValidating(false)
     * Digester-specific method to disable DTD validation
     */
    public static final String VALIDATING_DISABLED_PATTERN = 
        "\\w+\\.setValidating\\s*\\(\\s*false\\s*\\)";
    
    /**
     * Regex pattern to detect EmptyEntityResolver
     * Matches: digester.setEntityResolver(new EmptyEntityResolver())
     * Common pattern to block all entity resolution
     */
    public static final String EMPTY_ENTITY_RESOLVER_PATTERN = 
        "\\w+\\.setEntityResolver\\s*\\(\\s*new\\s+EmptyEntityResolver\\s*\\(\\s*\\)\\s*\\)";
    
    /**
     * Regex pattern to detect null EntityResolver
     * Matches: digester.setEntityResolver(null)
     * Prevents any entity resolution
     */
    public static final String NULL_ENTITY_RESOLVER_PATTERN = 
        "\\w+\\.setEntityResolver\\s*\\(\\s*null\\s*\\)";
    
    /**
     * Regex pattern to detect custom secure EntityResolver
     * Matches: digester.setEntityResolver(new SecureEntityResolver())
     * Allows controlled entity resolution with custom security logic
     */
    public static final String CUSTOM_ENTITY_RESOLVER_PATTERN = 
        "\\w+\\.setEntityResolver\\s*\\(\\s*new\\s+\\w+EntityResolver\\s*\\(\\s*\\)\\s*\\)";
    
    /**
     * Combined EntityResolver pattern
     * Matches any EntityResolver configuration (Empty, null, or custom)
     */
    public static final String ENTITY_RESOLVER_PATTERN = 
        "(" + EMPTY_ENTITY_RESOLVER_PATTERN + "|" + NULL_ENTITY_RESOLVER_PATTERN + "|" + 
        CUSTOM_ENTITY_RESOLVER_PATTERN + ")";
    
    /**
     * Regex pattern to detect Digester instantiation
     * Matches: new Digester() or Digester digester = new Digester()
     * Used to identify code blocks that need security configuration
     */
    public static final String DIGESTER_CREATION_PATTERN = 
        "new\\s+Digester\\s*\\(\\s*\\)";
    
    /**
     * Regex pattern to detect Digester.parse() method calls
     * Matches: digester.parse(input)
     * Identifies where XML parsing actually occurs
     */
    public static final String DIGESTER_PARSE_PATTERN = 
        "\\w+\\.parse\\s*\\([^)]+\\)";
    
    /**
     * Composite pattern for MINIMUM secure configuration
     * A Digester is considered minimally secure if it has:
     * - Validating disabled (setValidating(false))
     * - AND either:
     *   - disallow-doctype-decl enabled (STRONGEST)
     *   - OR both external entities disabled (general + parameter)
     */
    public static final String MINIMAL_SECURE_CONFIG_PATTERN = 
        "(" + VALIDATING_DISABLED_PATTERN + ").*(" +
        "(" + DISALLOW_DOCTYPE_PATTERN + ")|" +
        "((?:" + SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + "|" + 
        APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ").*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + "))";
    
    /**
     * Alternative minimal secure pattern with EntityResolver
     * Uses EntityResolver as primary defense
     * - Validating disabled
     * - AND EntityResolver configured (Empty, null, or custom)
     */
    public static final String MINIMAL_SECURE_WITH_RESOLVER_PATTERN = 
        "(" + VALIDATING_DISABLED_PATTERN + ").*(" + ENTITY_RESOLVER_PATTERN + ")";
    
    /**
     * Comprehensive secure configuration pattern (BEST PRACTICE)
     * Includes all recommended security features:
     * - Validating disabled
     * - FEATURE_SECURE_PROCESSING
     * - disallow-doctype-decl OR (external-general-entities + external-parameter-entities)
     * - load-external-dtd disabled
     * - EntityResolver configured
     * 
     * This represents DEFENSE IN DEPTH for Digester
     */
    public static final String COMPREHENSIVE_SECURE_CONFIG_PATTERN = 
        "(" + VALIDATING_DISABLED_PATTERN + ").*" +
        "(" + FEATURE_SECURE_PROCESSING_PATTERN + ").*(" + 
        DISALLOW_DOCTYPE_PATTERN + "|" +
        "((?:" + SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + "|" + 
        APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN + ").*" + 
        EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN + ")).*" +
        "(" + ENTITY_RESOLVER_PATTERN + ")";

    /**
     * Regex rule requiring Digester to disable validation, enable secure processing,
     * combine disallow-doctype (or both entity toggles), and either disable external DTD loading
     * or enforce a safe EntityResolver to mitigate XXE plus XML bomb DoS risks.
     */
    public static final String ESSENTIAL_SECURITY_RULE_PATTERN =
        "(?=.*\\w+\\.setValidating\\s*\\(\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*XMLConstants\\.FEATURE_SECURE_PROCESSING\\s*,\\s*true\\s*\\))(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/nonvalidating/load-external-dtd[\"']\\s*,\\s*false\\s*\\))|(?:(?=.*\\w+\\.setEntityResolver\\s*\\(\\s*null\\s*\\))|(?=.*\\w+\\.setEntityResolver\\s*\\(\\s*new\\s+\\w+EntityResolver\\s*\\(\\s*\\)\\s*\\)))))(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://apache\\.org/xml/features/disallow-doctype-decl[\"']\\s*,\\s*true\\s*\\))|(?:(?=.*\\w+\\.setFeature\\s*\\(\\s*(?:[\"']http://xml\\.org/sax/features/external-general-entities[\"']|[\"']http://apache\\.org/xml/features/external-general-entities[\"'])\\s*,\\s*false\\s*\\))(?=.*\\w+\\.setFeature\\s*\\(\\s*[\"']http://xml\\.org/sax/features/external-parameter-entities[\"']\\s*,\\s*false\\s*\\))))";
    
    /**
     * Anti-pattern: Unsafe Digester usage
     * Detects Digester creation WITHOUT proper security configuration
     * This pattern should NOT match in secure code - indicates XXE vulnerability
     */
    public static final String UNSAFE_DIGESTER_PATTERN = 
        "new\\s+Digester\\s*\\(\\s*\\)(?!.*(?:setFeature.*(?:disallow-doctype-decl|external-general-entities)|setValidating|setEntityResolver))";
    
    /**
     * Anti-pattern: Validating enabled
     * Matches: digester.setValidating(true)
     * Enabling validation can enable DTD processing - potentially unsafe
     */
    public static final String VALIDATING_ENABLED_PATTERN = 
        "\\w+\\.setValidating\\s*\\(\\s*true\\s*\\)";
    
    /**
     * Get all compiled patterns for secure configurations
     * @return List of compiled Pattern objects for security validation
     */
    public static List<Pattern> getSecureConfigPatterns() {
        List<Pattern> patterns = new ArrayList<>();
        patterns.add(Pattern.compile(FEATURE_SECURE_PROCESSING_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(DISALLOW_DOCTYPE_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_EXTERNAL_DTD_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(LOAD_DTD_GRAMMAR_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(VALIDATING_DISABLED_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(EMPTY_ENTITY_RESOLVER_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(NULL_ENTITY_RESOLVER_PATTERN, Pattern.DOTALL));
        patterns.add(Pattern.compile(CUSTOM_ENTITY_RESOLVER_PATTERN, Pattern.DOTALL));
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
        return Pattern.compile(UNSAFE_DIGESTER_PATTERN, Pattern.DOTALL);
    }
    
    /**
     * Get the validating enabled pattern (potential vulnerability)
     * Detects when validation is explicitly enabled
     * @return Compiled Pattern for detecting potentially unsafe validation
     */
    public static Pattern getValidatingEnabledPattern() {
        return Pattern.compile(VALIDATING_ENABLED_PATTERN, Pattern.DOTALL);
    }
}
