# XXE安全配置Regex Pattern文件总结

## 概述

所有6个XML parser都已配置完整的regex pattern规则文件，用于检测安全配置。

## Parser列表及其Regex规则文件

### ✅ 1. DocumentBuilder (javax.xml.parsers.DocumentBuilderFactory)
**文件**: `DocumentBuilder_regex_rules.java`

**必需安全配置**:
- `DISALLOW_DOCTYPE_PATTERN` - 禁用DTD声明（最强保护）
- `EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - 禁用外部参数实体
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - 禁用外部DTD加载
- `EXPAND_ENTITY_REFERENCES_DISABLED_PATTERN` - 禁用实体引用展开
- `FEATURE_SECURE_PROCESSING_PATTERN` - 启用安全处理模式

**额外patterns**: 13个定义，包括组合模式和不安全模式检测

---

### ✅ 2. SAXParser (javax.xml.parsers.SAXParserFactory)
**文件**: `SAXParser_regex_rules.java`

**必需安全配置**:
- `DISALLOW_DOCTYPE_PATTERN` - 禁用DTD声明
- `EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - 禁用外部参数实体
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - 禁用外部DTD加载
- `XINCLUDE_DISABLED_PATTERN` - 禁用XInclude处理
- `FEATURE_SECURE_PROCESSING_PATTERN` - 启用安全处理模式

**额外patterns**: 14个定义

---

### ✅ 3. SAXBuilder (org.jdom2.input.SAXBuilder - JDOM2)
**文件**: `SAXBuilder_regex_rules.java`

**必需安全配置**:
- `DISALLOW_DOCTYPE_PATTERN` - 禁用DTD声明
- `APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体（Apache版本）
- `SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体（SAX版本）
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - 禁用外部参数实体
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - 禁用外部DTD加载
- `FEATURE_SECURE_PROCESSING_PATTERN` - 启用安全处理模式

**特点**: 支持Apache和SAX两种feature URI格式

**额外patterns**: 15个定义

---

### ✅ 4. SAXReader (org.dom4j.io.SAXReader - DOM4J)
**文件**: `SAXReader_regex_rules.java`

**必需安全配置**:
- `DISALLOW_DOCTYPE_PATTERN` - 禁用DTD声明
- `EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - 禁用外部参数实体
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - 禁用外部DTD加载
- `ENTITY_RESOLVER_NULL_PATTERN` - EntityResolver设置为null
- `ENTITY_RESOLVER_CUSTOM_PATTERN` - 自定义安全EntityResolver
- `FEATURE_SECURE_PROCESSING_PATTERN` - 启用安全处理模式

**特点**: 支持EntityResolver配置作为额外防护

**额外patterns**: 16个定义

---

### ✅ 5. InputFactory (javax.xml.stream.XMLInputFactory - StAX)
**文件**: `InputFactory_regex_rules.java`

**必需安全配置**:
- `SUPPORT_DTD_DISABLED_PATTERN` - 禁用DTD支持（常量形式）
- `SUPPORT_DTD_DISABLED_STRING_PATTERN` - 禁用DTD支持（字符串形式）
- `IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_PATTERN` - 禁用外部实体支持（常量）
- `IS_SUPPORTING_EXTERNAL_ENTITIES_DISABLED_STRING_PATTERN` - 禁用外部实体支持（字符串）
- `ACCESS_EXTERNAL_DTD_RESTRICTED_PATTERN` - 限制外部DTD访问（常量）
- `ACCESS_EXTERNAL_DTD_RESTRICTED_STRING_PATTERN` - 限制外部DTD访问（字符串）

**特点**: StAX使用property而非feature，支持常量和字符串两种配置方式

**额外patterns**: 16个定义

---

### ✅ 6. Digester (org.apache.commons.digester.Digester)
**文件**: `Digester_regex_rules.java`

**必需安全配置**:
- `DISALLOW_DOCTYPE_PATTERN` - 禁用DTD声明
- `SAX_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体（SAX版本）
- `APACHE_EXTERNAL_GENERAL_ENTITIES_DISABLED_PATTERN` - 禁用外部通用实体（Apache版本）
- `EXTERNAL_PARAMETER_ENTITIES_DISABLED_PATTERN` - 禁用外部参数实体
- `LOAD_EXTERNAL_DTD_DISABLED_PATTERN` - 禁用外部DTD加载
- `ENTITY_RESOLVER_PATTERN` - EntityResolver配置（组合pattern）

**特点**: 支持多种EntityResolver实现（Empty、Null、Custom）

**额外patterns**: 21个定义（最多）

**修复**: 修复了`ESSENTIAL_SECURITY_RULE_PATTERN`的正则表达式语法错误

---

## 验证工具

**脚本**: `validate_regex_patterns.py`

**功能**:
1. 检查所有regex规则文件是否存在
2. 验证每个文件是否包含所有必需的pattern
3. 验证每个pattern的正则表达式语法是否正确
4. 生成详细的验证报告

**运行**:
```bash
cd NesCodeSecExamples/src/main/java/com/V9-XXE/regex_evaluation
python3 validate_regex_patterns.py
```

**验证结果**: ✅ 所有6个parser的所有patterns都通过验证

---

## 使用方法

这些regex patterns在`zeta_xxe.py`中的以下函数中使用：

1. **`load_regex_rules(parser_name)`** - 加载指定parser的regex规则
2. **`evaluate_file_with_regex()`** - 使用regex评估单个文件的安全性
3. **`evaluate_regex_all_parsers()`** - 评估所有migration的安全性

### 评估流程

```python
# 1. 加载规则
rules = load_regex_rules("DocumentBuilder")

# 2. 评估文件
result = evaluate_file_with_regex(
    file_path=java_file,
    parser_name="DocumentBuilder",
    rules=rules,
    required_groups=REQUIRED_RULE_GROUPS["DocumentBuilder"]
)

# 3. 检查结果
if result["is_secure"]:
    print("✅ 文件安全")
else:
    print(f"❌ 缺少: {result['missing_requirements']}")
```

---

## Pattern命名规范

所有pattern遵循统一的命名规范：

- `*_PATTERN` - 基础检测pattern
- `*_DISABLED_PATTERN` - 检测禁用配置
- `*_ENABLED_PATTERN` - 检测启用配置（通常是不安全的）
- `UNSAFE_*_PATTERN` - 检测不安全配置
- `MINIMAL_SECURE_CONFIG_PATTERN` - 最小安全配置组合
- `COMPREHENSIVE_SECURE_CONFIG_PATTERN` - 全面安全配置组合

---

## 安全建议

### 最强保护（推荐）
对所有parser: **禁用DOCTYPE声明** (`DISALLOW_DOCTYPE_PATTERN`)
- 完全阻止DTD处理
- 防止所有XXE攻击向量

### 需要DTD时
必须同时配置：
1. 禁用外部通用实体
2. 禁用外部参数实体  
3. 启用安全处理模式
4. 禁用外部DTD加载

### 深度防御
推荐使用`COMPREHENSIVE_SECURE_CONFIG_PATTERN`，包含所有安全措施。

---

## 总结

✅ **6个parser** × **完整regex规则** = **全面XXE防护检测**

所有regex pattern文件已验证通过，可用于自动化安全评估！
