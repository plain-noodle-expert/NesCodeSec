<|current_file_content|>
package com.novi.app.controller;

import com.novi.app.model.Group;
import com.novi.app.model.MusicInstrument;
import com.novi.app.model.MusicStyle;
import com.novi.app.model.request.CreateUserRequest;
import com.novi.app.model.request.ModifyUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.novi.app.model.User;
import com.novi.app.service.UserService;

import java.util.*;

@RestController
@RequestMapping("/users")
@CrossOrigin
@Tag(
        name = "Пользователи",
        description = "Все методы для работы с пользователями системы"
)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(
            UserController.class
    );

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Получить информацию о пользователях")
    @GetMapping("/all")
    public ResponseEntity<List<User>> index(){
        logger.debug("Getting all users");
        List<User> users = userService.findAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Получить информацию о пользователе по его id")
    // а если он хочет скрыть какую-то инфу, например, др?
    @PreAuthorize("hasRole('USER'+#userId)")
    @GetMapping("/{id}")
    public ResponseEntity<Optional<User>> getUserById(@Parameter(description = "id пользователя")
                                                      @PathVariable("id") Long userId){
        logger.debug("Getting user info with id: {}", userId);
        return new ResponseEntity<>(userService.findUserById(userId), HttpStatus.OK);
    }

    @Operation(summary = "Получить информацию о группах пользователя")
    @GetMapping("/{id}/groups")
    public ResponseEntity<Set<Group>> getUserGroups(@Parameter(description = "id пользователя")
                                                    @PathVariable("id") Long userId){
        return new ResponseEntity<>(userService.getUserGroups(userId), HttpStatus.OK);
    }

    @Operation(summary = "Получить информацию о музыкальных стилях пользователя")
    @GetMapping("/{id}/styles")
    public ResponseEntity<Set<MusicStyle>> getUserMusicStyles(@Parameter(description = "id пользователя")
                                                              @PathVariable("id") Long userId){
        return new ResponseEntity<>(userService.getUserMusicStyles(userId), HttpStatus.OK);
    }

    @Operation(summary = "Получить информацию о музыкальных инструментах пользователя")
    @GetMapping("/{id}/instruments")
    public ResponseEntity<Set<MusicInstrument>> getUserMusicInstruments(@Parameter(description = "id пользователя")
                                                                        @PathVariable("id") Long userId){
        return new ResponseEntity<>(userService.getUserMusicInstruments(userId), HttpStatus.OK);
    }

    // @ExceptionHandler
    @Operation(summary = "Добавить музыкальный стиль пользователю")
    @PostMapping("/{userId}/styles/{styleId}")
    @PreAuthorize("hasRole('USER'+#userId)")
    public ResponseEntity<String> addMusicStyleIntoUserList(@Parameter(description = "id пользователя")
                                                                        @PathVariable("userId") Long userId,
                                                                        @PathVariable("styleId") Integer styleId){
        userService.addMusicStyle(userId, styleId);
        return new ResponseEntity<>("Music style has been added", HttpStatus.OK);
    }

}
<|/current_file_content|>