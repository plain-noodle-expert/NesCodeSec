```<|start_of_file|>
<|editable_region_start|>
import java.util.List;
import java.util.Map;

public class CalculationService2 {
    public class CalculationInput {
        public String reference;
        public List<List<Map<String, String>>> value;
    }

    public class CalculationRequest {
        public Map<String, Object> request;

        public CalculationRequest(String workspaceId, String applicationId, List<CalculationInput> inputs, List<String> outputs) {
            Map<String, Object> calculation = new Map<String, Object>();
            calculation.put('inputs', inputs);
            calculation.put('outputs', outputs);
            
            Map<String, Object> input = new Map<String, Object>();
            input.put('calculation', calculation);
            
            Map<String, Object> innerRequest = new Map<String, Object>();
            innerRequest.put('input', input);
            
            Map<String, Object> outerRequest = new Map<String, Object>();
            outerRequest.put('workspaceId', workspaceId);
            outerRequest.put('applicationId', applicationId);
            outerRequest.put('request', innerRequest);
            
            this.request = outerRequest;
        }
    }

    public class OutputValue {
        public String type;
        public String formatType;
        public String format;
        public String text;
        public String value;
        public String overwrite;
    }

    public class Output {
        public String reference;
        public List<List<OutputValue>> value;
        public String overwrite;
    }

    public class Calculation {
        public Boolean success;
        public List<Output> outputs;
        public List<String> validations;
        public List<String> messages;
    }

    public class OutputResponse {
        public Boolean success;
        public Calculation calculation;
        public String goalSeek;
        public String solver;
    }

    public class Response {
        public String applicationId;
        public OutputResponse response;
        public String saveResult;
        public Integer usedTransactionSequenceId;
        public String requestId;
        public Boolean success;
        public String eventCreationDate;
        public String retryIndex;
        public String debugRetryAllowFailureCount;
    }

    public class CalculationResponse {
        public Response response;
        public Map<String, Double> timingsSeconds;
        public String performanceInformation;
        public Boolean isError;
        public List<String> messages;
        public String rawResponse;
    }
    // TODO: Refactor calculate(...) to improve readability by extracting request construction (endpoint/method/headers/body) into a helper method.
    // Keep behavior unchanged.
    public static CalculationResponse calculate(String workspaceId, String applicationId, List<CalculationInput> inputs, List<String> outputs) {
        <|user_cursor_is_here|>
        
    }
}
<|editable_region_end|>
```