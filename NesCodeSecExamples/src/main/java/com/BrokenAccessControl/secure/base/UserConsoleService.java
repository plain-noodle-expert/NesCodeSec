package main.services;

import lombok.AllArgsConstructor;
import main.entity.RoleType;
import main.entity.User;
import main.security.SecurityService;
import main.web.model.auth.RefreshTokenRequest;
import main.web.model.user.UpdateUserRequest;
import main.web.model.user.UserInfoResponse;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class UserConsoleService {
    private final SecurityService securityService;

    public UserInfoResponse getInfoByRefreshToken(RefreshTokenRequest request){
        User user = securityService.getUserByRefreshToken(request.getRefreshToken());
        return UserInfoResponse.builder()
                .login(user.getLogin())
                .email(user.getEmail())
                .roles(user.getRoles().stream().map(RoleType::name)
                        .map((role) -> role.replaceAll("ROLE_", "")).toList())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .age(user.getAge())
                .sex(user.getSex())
                .icon(user.getIconType())
                .build();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String updateUserData(UpdateUserRequest request) {
        User user = securityService.getCurrentUser();
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getAge() != null) {
            user.setAge(request.getAge());
        }
        if (request.getSex() != null) {
            user.setSex(request.getSex());
        }
        if (request.getIcon() != null) {
            user.setIconType(request.getIcon());
        }
        securityService.updateUser(user);
        return "User data updated successfully";
    }
    
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String logoutUser(){
        securityService.logout();
        return "See you later";
    }
}
