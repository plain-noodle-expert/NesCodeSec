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
User edited "models/customer.rb":

```diff
@@ -2,5 +2,5 @@
class Customer
def initialize
- @name = name
+ @name = name.capitalize
@email = email
@phone = phone
```
"""

input = """
```models/customer.rb
def initialize
<|editable_region_start|>
@name = name.capitalize<|user_cursor_is_here|>
@email = email
@phone = phone
@address = address
end

def to_s
@name
end

<|editable_region_end|>
private

def validate_email
@email.include?('@')
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