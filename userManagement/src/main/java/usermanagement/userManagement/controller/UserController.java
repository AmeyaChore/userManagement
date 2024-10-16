package usermanagement.userManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usermanagement.userManagement.exception.UserAlreadyExistsException;
import usermanagement.userManagement.model.User;
import usermanagement.userManagement.repository.UserNotFoundException;
import usermanagement.userManagement.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = null;
        try {
            createdUser = userService.createUser(user);
        } catch (UserAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (Exception e){

        }
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = null;
        try {
            user = userService.getUserById(id);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = null;
        try {
            updatedUser = userService.updateUser(id, userDetails);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.noContent().build();
    }
}

