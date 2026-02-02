# XXE Security Rule Refactoring

## Overview

为了减少代码冗余，XXE安全规则已经从`zeta_xxe.py`中重构到独立的模块和配置文件。

## 架构

### 1. 规则定义 (src/xxe_rule_loader.py)

所有XXE安全规则现在定义在`src/xxe_rule_loader.py`中，包括：

- **SECURITY_RULE_GROUPS**: 每个parser的规则组定义
  - 规则a：禁用DOCTYPE (推荐)
  - 规则b：禁用外部实体 + 外部参数实体 + 外部DTD

### 2. 规则组逻辑

**关键概念**: 每个parser只要满足**任意一个完整的规则组**，就被认为是安全的。

例如，DocumentBuilder可以通过以下任一方式达到安全标准：
- **规则a**: 只需设置 `disallow-doctype-decl = true`
- **规则b**: 同时设置三个配置：
  - `external-general-entities = false`
  - `external-parameter-entities = false`
  - `load-external-dtd = false`

### 3. Parser规则文件

每个parser在`NesCodeSecExamples/src/main/java/com/V9-XXE/regex_evaluation_rule/`目录下都有对应的Java文件，包含详细的pattern定义：

- `DocumentBuilder_regex_rules.java`
- `SAXParser_regex_rules.java`
- `SAXBuilder_regex_rules.java`
- `SAXReader_regex_rules.java`
- `InputFactory_regex_rules.java`
- `Digester_regex_rules.java`

这些文件包含：
- 详细的安全配置说明
- 各种pattern常量定义
- 编译好的Pattern对象获取方法

### 4. 使用方式

在`zeta_xxe.py`中：

```python
from xxe_rule_loader import get_security_rule_groups

# 加载所有规则组
SECURITY_RULE_GROUPS = get_security_rule_groups()

# 使用规则进行评估
rule_group_results = _check_security_rule_groups(parser, text, parser_vars)
```

## 规则组详细定义

### DocumentBuilder / SAXParser / SAXBuilder / SAXReader / Digester

**规则a - 禁用DOCTYPE (推荐)**:
```
DisallowDOCTYPE: setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
```

**规则b - 禁用外部实体组合**:
```
DisableExternalGeneral: setFeature("http://xml.org/sax/features/external-general-entities", false)
DisableExternalParameter: setFeature("http://xml.org/sax/features/external-parameter-entities", false)
DisableLoadExternalDTD: setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
```

### InputFactory (特殊情况)

**规则a - 禁用DTD支持 (推荐)**:
```
DisableDTDSupport: setProperty(XMLInputFactory.SUPPORT_DTD, false)
                   或 setProperty("javax.xml.stream.supportDTD", false)
```

**规则b - 禁用外部实体**:
```
DisableExternalEntities: setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false)
                         或 setProperty("javax.xml.stream.isSupportingExternalEntities", false)
```

## 评估逻辑

### ParserScanResult类

```python
@dataclass
class ParserScanResult:
    path: Path
    parser: str
    satisfied: Dict[str, bool]  # 向后兼容
    satisfied_rule_groups: Dict[str, Dict[str, bool]] = None  # 新增

    @property
    def is_secure(self) -> bool:
        """只要有任意一个规则组的所有要求都满足，就返回True"""
        if self.satisfied_rule_groups:
            for rule_group_name, requirements in self.satisfied_rule_groups.items():
                if all(requirements.values()):
                    return True
            return False
        return not self.missing

    @property
    def satisfied_rules(self) -> List[str]:
        """返回所有完全满足的规则组列表"""
```

### 检查函数

```python
def _check_security_rule_groups(parser: str, text: str, var_names: List[str]) -> Dict[str, Dict[str, bool]]:
    """
    检查安全规则组
    返回: {rule_group_name: {requirement: satisfied}}
    """
```

### 扫描流程

```python
def _scan_java_files(scan_root: Path):
    # 1. 检测parser类型
    parser = _parser_from_path(rel_path) or _detect_parser(text)
    
    # 2. 查找parser变量
    parser_vars = _find_parser_variables(parser, text)
    
    # 3. 检查规则组
    rule_group_results = _check_security_rule_groups(parser, text, parser_vars)
    
    # 4. 创建结果对象
    result = ParserScanResult(rel_path, parser, per_requirement, rule_group_results)
    
    # 5. 判断是否安全 (只要任意规则组完全满足)
    if result.is_secure:
        # 安全
```

## 优势

1. **代码复用**: 规则定义集中在一处，避免重复
2. **可维护性**: 更新规则只需修改xxe_rule_loader.py
3. **清晰度**: zeta_xxe.py更专注于评估逻辑
4. **灵活性**: 可以轻松添加新的parser或规则组
5. **文档化**: Java文件包含详细的安全配置说明

## 向后兼容

- 保留了`SECURITY_REQUIREMENTS`结构（已废弃）
- `ParserScanResult.satisfied`属性仍然存在
- 旧的评估逻辑在`_check_security_requirements`函数中保留

## 扩展指南

### 添加新parser

1. 在`xxe_rule_loader.py`的`SECURITY_RULE_GROUPS`中添加parser定义
2. 创建对应的`{Parser}_regex_rules.java`文件
3. 在`PARSER_VAR_PATTERNS`中添加变量匹配规则

### 修改现有规则

1. 编辑`xxe_rule_loader.py`中对应parser的规则组
2. 更新对应的Java文件中的pattern定义（如需要）

## 测试

```bash
# 测试规则加载
python3 src/xxe_rule_loader.py

# 测试规则评估
python3 src/zeta_xxe.py
```
