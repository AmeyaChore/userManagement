package usermanagement.userManagement.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import usermanagement.userManagement.exception.InternalServerException;
import usermanagement.userManagement.exception.UserAlreadyExistsException;
import usermanagement.userManagement.exception.UserNotFoundException;
import usermanagement.userManagement.exception.ValidationException;
import usermanagement.userManagement.model.Response;
import usermanagement.userManagement.model.User;
import usermanagement.userManagement.service.UserService;
import usermanagement.userManagement.utils.Utils;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping( produces = MediaType.APPLICATION_JSON_VALUE)
    @RateLimiter(name = "createUserRateLimiter")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        User createdUser = null;
        Response response = null;
        try {
            log.info("Create User Controller");
            createdUser = userService.createUser(user);
        } catch (UserAlreadyExistsException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 400);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(400));
        } catch (ValidationException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 400);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(400));
        } catch (InternalServerException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        } catch (Exception e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        }
        response = Utils.generateResponse("Success", createdUser, 200);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @RateLimiter(name = "getUserRateLimiter")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = null;
        Response response = null;
        try {
            user = userService.getUserById(id);
        } catch (UserNotFoundException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 404);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(404));
        } catch (InternalServerException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        } catch (Exception e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        }
        response = Utils.generateResponse("Success", user, 200);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @RateLimiter(name = "updateUserRateLimiter")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = null;
        Response response = null;
        try {
            updatedUser = userService.updateUser(id, userDetails);
        } catch (UserNotFoundException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 404);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(404));
        } catch (ValidationException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 400);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(400));
        } catch (InternalServerException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        } catch (Exception e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        }
        response = Utils.generateResponse("Success", updatedUser, 200);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @RateLimiter(name = "deleteUserRateLimiter")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Response response = null;
        try {
            userService.deleteUser(id);
        } catch (UserNotFoundException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 404);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(404));
        } catch (InternalServerException e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        } catch (Exception e) {
            response = Utils.generateResponse("Failed", e.getMessage(), 500);
            return new ResponseEntity<>(response, HttpStatusCode.valueOf(500));
        }
        response = Utils.generateResponse("Success", "Deleted Successfully", 200);
        return ResponseEntity.ok(response);
    }
}

