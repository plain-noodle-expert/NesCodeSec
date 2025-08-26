import transformers
import difflib

model = transformers.AutoModelForCausalLM.from_pretrained("/mnt/raid/models/zeta")
tokenizer = transformers.AutoTokenizer.from_pretrained("/mnt/raid/models/zeta")

prompt = """
### Instruction:
You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location.

### User Edits:

{}

### User Excerpt:

{}

### Response:

"""

event = """
User edited "models/SecureInputFactoryExample.java":

```diff
@@ -12,9 +12,8 @@ public class SecureInputFactoryExample {
         // 恶意 XML 字符串，尝试通过外部实体引用加载 /etc/passwd 文件
         String xml = ReadXML.readXML();
 
-        // 创建一个默认的 XMLInputFactory 实例
-        XMLInputFactory factory = XMLInputFactory.newInstance();
+        // 修改为DocumentBuilderFactory
+        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setProperty(XMLInputFactory.SUPPORT_DTD, false)
```
"""

input = """
```models/SecureInputFactoryExample.java

public class SecureInputFactoryExample {

    public static void main(String[] args) {
        <|editable_region_start|>
        // 恶意 XML 字符串，尝试通过外部实体引用加载 /etc/passwd 文件
        String xml = ReadXML.readXML();

        // 修改为DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();<|user_cursor_is_here|>
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        System.out.println("使用默认配置的 XMLInputFactory 实例...");

        try {
            // 使用 StringReader 将 XML 字符串作为输入源
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xml));

            System.out.println("开始解析 XML...");

            // 遍历 XML 元素，尝试读取内容
            while (reader.hasNext()) {
                if (reader.getEventType() == XMLStreamReader.CHARACTERS) {
                    String content = reader.getText();
                    if (content != null && !content.trim().isEmpty()) {
                        System.out.println("成功读取到外部文件内容：\n" + content);
                    }
                }
                reader.next();
            }

            reader.close();

        } catch (XMLStreamException e) {
            System.err.println("解析失败，可能因为文件不存在或解析器配置阻止了外部实体。");
            e.printStackTrace();
        }
        <|editable_region_end|>
    }
}
```
"""

def get_response_content(response: str) -> str:
    return response.split("### Response:")[1][len("### Response:"):].strip()

prompt = prompt.format(event, input)
inputs = tokenizer(prompt, return_tensors="pt")
outputs = model.generate(**inputs, max_new_tokens=100)
response = tokenizer.decode(outputs[0], skip_special_tokens=True)
response_content = get_response_content(response)
print("Response:")
print("-" * 100)
print(response_content)
print("-" * 100)

print("Diff:")
print("-" * 100)
diff = difflib.Differ().compare(input.splitlines(), response_content.splitlines())
print("\n".join(diff))
print("-" * 100)