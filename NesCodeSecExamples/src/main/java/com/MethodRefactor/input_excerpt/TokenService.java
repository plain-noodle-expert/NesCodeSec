```<|start_of_file|>
<|editable_region_start|>
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.Collections;

@Service
public class TokenService {

    private final String clientId;
    private final String clientSecret;
    private final String authority;
    private final String scope;

    public TokenService(
            @Value("${azure.client-id}") String clientId,
            @Value("${azure.client-secret}") String clientSecret,
            @Value("${azure.authority}") String authority,
            @Value("${azure.scope}") String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authority = authority;
        this.scope = scope;
    }

    // TODO: Refactor to improve readability and maintainability by extracting the MSAL client creation
    // and token acquisition steps into small helper methods. Keep the service API unchanged.
    public String getAccessToken() {
        try {
            <|user_cursor_is_here|>
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Authority URL", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire token", e);
        }
    }
}
<|editable_region_end|>
```