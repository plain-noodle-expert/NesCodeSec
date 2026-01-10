```<|start_of_file|>
package src.filter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import src.exception.apiCallException;
import src.exception.userNotFound;
import src.model.user;
import src.repository.userRepository;
import src.service.tokenService;
import src.view.respond;

<|editable_region_start|>
@Component
public class jwtFilter extends HttpFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    userRepository userRepository;

    @Autowired
    private tokenService tokenService;

    // TODO: Refactor the bypass-path rule into a dedicated helper method to improve maintainability.
    @Override
    protected void doFilter(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws IOException, ServletException {
        String path = request.getRequestURI();

        <|user_cursor_is_here|>

        String authHeaderToken = request.getHeader("Authorization");

        if(authHeaderToken == null) {
            respond<String> errorResponse = new respond<>(
                "Unauthorized",
                    401,
                "Missing or Invalid token"

            );

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        user user;

         try {
             String sub = tokenService.validateToken(authHeaderToken);

             try {
                 user = getUser(sub).orElseThrow(
                         () -> new userNotFound(sub)
                 );
             } catch (userNotFound e) {
                 user = new user(
                         sub,
                         sub.split("@")[0],
                         List.of("user")
                 );
                 userRepository.save(user);
             }

             List<SimpleGrantedAuthority> authorities = user.getRole().stream()
                     .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                     .toList();

             UsernamePasswordAuthenticationToken authentication =
                     new UsernamePasswordAuthenticationToken(sub, null, authorities);

             SecurityContextHolder.getContext().setAuthentication(authentication);

             request.setAttribute("email", sub);

             chain.doFilter(request, response);
         }  catch (apiCallException e) {
             respond<String> errResponse = new respond<String>(
                 "Unauthorized",
                     e.getCode(),
                 e.getMessage()
             );

             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
             response.setContentType("application/json");
             response.getWriter().write(objectMapper.writeValueAsString(errResponse));
         }
    }

    private Optional<user> getUser(String sub) throws userNotFound {
        return  userRepository.findById(sub);
    }
}
<|editable_region_end|>
```