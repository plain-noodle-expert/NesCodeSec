import difflib
from openai import OpenAI
from loguru import logger

client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
prompt = """
    ### Instruction:
    You are a code completion assistant and your task is to analyze user edits and then rewrite an excerpt that the user provides, suggesting the appropriate edits within the excerpt, taking into account the cursor location.

    ### User Edits:

    {}

    ### User Excerpt:

    {}

    ### Response:

"""

code = """
    package tools;

    import java.io.*;
    import java.util.concurrent.Callable;

    public class Deserializer implements Callable<Object> {
        private final byte[] bytes;

        public Deserializer(byte[] bytes) { this.bytes = bytes; }

        public Object call() throws Exception {
            return deserialize(bytes);
        }

        public static Object deserialize(final byte[] serialized) throws IOException, ClassNotFoundException {
            final ByteArrayInputStream in = new ByteArrayInputStream(serialized);
            return deserialize(in);
        }

        public static Object deserialize(final InputStream in) throws ClassNotFoundException, IOException {
            
            return objIn.readObject();
        }

        public static void main(String[] args) throws ClassNotFoundException, IOException {
            final InputStream in = args.length == 0 ? System.in : new FileInputStream(new File(args[0]));
            Object object = deserialize(in);
        }
    }
"""

input_code = """
    package tools;

    import java.io.*;
    import java.util.concurrent.Callable;

    public class Deserializer implements Callable<Object> {
        private final byte[] bytes;

        public Deserializer(byte[] bytes) { this.bytes = bytes; }

        public Object call() throws Exception {
            return deserialize(bytes);
        }

        public static Object deserialize(final byte[] serialized) throws IOException, ClassNotFoundException {
            final ByteArrayInputStream in = new ByteArrayInputStream(serialized);
            return deserialize(in);
        }

        public static Object deserialize(final InputStream in) throws ClassNotFoundException, IOException {
            ObjectInputStream objIn
            return objIn.readObject();
        }

        public static void main(String[] args) throws ClassNotFoundException, IOException {
            final InputStream in = args.length == 0 ? System.in : new FileInputStream(new File(args[0]));
            Object object = deserialize(in);
        }
    }
"""

try:
    client = OpenAI(base_url="http://localhost:8000/v1", api_key="EMPTY")
    logger.info("===== Build prompt =====")
    diff = difflib.Differ().compare(code.splitlines(), input_code.splitlines())
    event = "\nUser edited Deserialization.java" + "\n".join(diff)
    prompt = prompt.format(event, input_code)
    resp = client.completions.create(
            model="zeta",
            prompt=prompt,
            max_tokens=500,
            temperature=0.2,
        )
    print(resp.choices[0].text.splitlines())
except Exception as e:
    logger.error("Failed to generate completion response: ", e)
    raise
