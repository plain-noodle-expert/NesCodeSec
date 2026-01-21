# Validation – Demonstrating Ineffective `@PreAuthorize` on Private Methods

This minimal Spring Boot project proves that annotating a **private** method with `@PreAuthorize` does **not** enforce authorization checks, because Spring creates proxies only for public methods.

## Project layout

- `src/main/java/com/example/validation/ValidationApplication.java` – entry point with `@EnableMethodSecurity`.
- `config/SecurityConfig.java` – enables HTTP Basic auth with two in-memory users:
  - `user:userpass` → role `USER`
  - `admin:adminpass` → role `ADMIN`
- `service/DemoValidationService.java`
  - `triggerSensitiveOperation()` (public) calls a private method annotated with `@PreAuthorize`.
  - `properlySecuredOperation()` is a public method with the same annotation for comparison.
- `controller/DemoController.java` – exposes two endpoints:
  - `GET /api/public` → invokes the private, supposedly protected method.
  - `GET /api/protected` → invokes the correctly secured public method.

## Running the MVP

```bash
cd Validation
mvn spring-boot:run
```

Then exercise the endpoints:

1. **Unauthenticated call succeeds** despite the private method demanding `ROLE_ADMIN`:
   ```bash
   curl http://localhost:9090/api/public
   ```
   Response contains `"Sensitive data..."`, proving that `@PreAuthorize` on private methods is ignored.

2. **Properly secured endpoint is blocked without ADMIN**:
   ```bash
   curl -u user:userpass http://localhost:9090/api/protected   # HTTP 403
   curl -u admin:adminpass http://localhost:9090/api/protected # HTTP 200
   ```

The contrast between the two endpoints validates that private visibility prevents Spring Security from enforcing `@PreAuthorize`, while the same annotation works as expected on public methods.
