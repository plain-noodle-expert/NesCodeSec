# XXE Security Configuration Regex Pattern Summary

## Overview

All six XML parsers have complete regex pattern rule files configured to detect secure configurations.

## Parser List and Their Regex Rule Files

### ✅ 1. DocumentBuilder (javax.xml.parsers.DocumentBuilderFactory)
**File**: `DocumentBuilder_regex_rules.java`

**Required security configurations**:
- `DISALLOW_DOCTYPE_PATTERN` - Disable DTD declarations (strongest protection)
- `EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - Disable external parameter entities
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - Disable external DTD loading
- `EXPAND_ENTITY_REFERENCES_DISABLED_PATTERN` - Disable entity reference expansion
- `FEATURE_SECURE_PROCESSING_PATTERN` - Enable secure processing mode

**Additional patterns**: 13 definitions, including combined patterns and insecure-pattern detection

---

### ✅ 2. SAXParser (javax.xml.parsers.SAXParserFactory)
**File**: `SAXParser_regex_rules.java`

**Required security configurations**:
- `DISALLOW_DOCTYPE_PATTERN` - Disable DTD declarations
- `EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - Disable external parameter entities
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - Disable external DTD loading
- `XINCLUDE_DISABLED_PATTERN` - Disable XInclude processing
- `FEATURE_SECURE_PROCESSING_PATTERN` - Enable secure processing mode

**Additional patterns**: 14 definitions

---

### ✅ 3. SAXBuilder (org.jdom2.input.SAXBuilder - JDOM2)
**File**: `SAXBuilder_regex_rules.java`

**Required security configurations**:
- `DISALLOW_DOCTYPE_PATTERN` - Disable DTD declarations
- `APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities (Apache URI)
- `SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities (SAX URI)
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - Disable external parameter entities
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - Disable external DTD loading
- `FEATURE_SECURE_PROCESSING_PATTERN` - Enable secure processing mode

**Notes**: Supports both Apache and SAX feature URI formats

**Additional patterns**: 15 definitions

---

### ✅ 4. SAXReader (org.dom4j.io.SAXReader - DOM4J)
**File**: `SAXReader_regex_rules.java`

**Required security configurations**:
- `DISALLOW_DOCTYPE_PATTERN` - Disable DTD declarations
- `EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - Disable external parameter entities
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - Disable external DTD loading
- `ENTITY_RESOLVER_NULL_PATTERN` - EntityResolver set to null
- `ENTITY_RESOLVER_CUSTOM_PATTERN` - Custom secure EntityResolver
- `FEATURE_SECURE_PROCESSING_PATTERN` - Enable secure processing mode

**Notes**: Supports EntityResolver configuration as an additional safeguard

**Additional patterns**: 16 definitions

---

### ✅ 5. InputFactory (javax.xml.stream.XMLInputFactory - StAX)
**File**: `InputFactory_regex_rules.java`

**Required security configurations**:
- `SUPPORT_DTD_DISABLED_PATTERN` - Disable DTD support (constant form)
- `SUPPORT_DTD_DISABLED_STRING_PATTERN` - Disable DTD support (string form)
- `IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN` - Disable external entity support (constant)
- `IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN` - Disable external entity support (string)
- `ACCESS_EXTERNAL_DTD_RESTRICTED_PATTERN` - Restrict external DTD access (constant)
- `ACCESS_EXTERNAL_DTD_RESTRICTED_STRING_PATTERN` - Restrict external DTD access (string)

**Notes**: StAX uses properties rather than features, and supports both constant and string configuration forms

**Additional patterns**: 16 definitions

---

### ✅ 6. Digester (org.apache.commons.digester.Digester)
**File**: `Digester_regex_rules.java`

**Required security configurations**:
- `DISALLOW_DOCTYPE_PATTERN` - Disable DTD declarations
- `SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities (SAX URI)
- `APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - Disable external general entities (Apache URI)
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - Disable external parameter entities
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - Disable external DTD loading
- `ENTITY_RESOLVER_PATTERN` - EntityResolver configuration (combined pattern)

**Notes**: Supports multiple EntityResolver implementations (Empty, Null, Custom)

**Additional patterns**: 21 definitions (most)

**Fix**: Corrected a regex syntax error in `ESSENTIAL_SECURITY_RULE_PATTERN`

---

## Validation Tool

**Script**: `validate_regex_patterns.py`

**Functions**:
1. Check that all regex rule files exist
2. Verify each file contains all required patterns
3. Validate each regex pattern's syntax
4. Generate a detailed validation report

**Run**:
```bash
cd NesCodeSecExamples/src/main/java/com/V9-XXE/regex_evaluation
python3 validate_regex_patterns.py
```

**Validation result**: ✅ All patterns for all six parsers passed validation

---

## Usage

These regex patterns are used in the following functions in `zeta_xxe.py`:

1. **`load_regex_rules(parser_name)`** - Load regex rules for a specific parser
2. **`evaluate_file_with_regex()`** - Evaluate a single file with regex
3. **`evaluate_regex_all_parsers()`** - Evaluate all migrations with regex

### Evaluation Flow

```python
# 1. Load rules
rules = load_regex_rules("DocumentBuilder")

# 2. Evaluate file
result = evaluate_file_with_regex(
    file_path=java_file,
    parser_name="DocumentBuilder",
    rules=rules,
    required_groups=REQUIRED_RULE_GROUPS["DocumentBuilder"]
)

# 3. Check result
if result["is_secure"]:
    print("✅ File is secure")
else:
    print(f"❌ Missing: {result['missing_requirements']}")
```

---

## Pattern Naming Conventions

All patterns follow a consistent naming convention:

- `*_PATTERN` - Base detection pattern
- `*_DISABLED_PATTERN` - Detect disabled configurations
- `*_ENABLED_PATTERN` - Detect enabled configurations (typically insecure)
- `UNSAFE_*_PATTERN` - Detect unsafe configurations
- `MINIMAL_SECURE_CONFIG_PATTERN` - Minimal secure configuration combination
- `COMPREHENSIVE_SECURE_CONFIG_PATTERN` - Comprehensive secure configuration combination

---

## Security Recommendations

### Strongest Protection (Recommended)
For all parsers: **disable DOCTYPE declarations** (`DISALLOW_DOCTYPE_PATTERN`)
- Completely blocks DTD processing
- Prevents all XXE attack vectors

### When DTDs Are Required
You must configure all of the following:
1. Disable external general entities
2. Disable external parameter entities
3. Enable secure processing mode
4. Disable external DTD loading

### Defense in Depth
Use `COMPREHENSIVE_SECURE_CONFIG_PATTERN`, which includes all security measures.

---

## Summary

**6 parsers** × **complete regex rules** = **comprehensive XXE protection detection**

All regex pattern files have been validated and are ready for automated security evaluation.
