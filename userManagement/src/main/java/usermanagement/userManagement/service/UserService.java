package usermanagement.userManagement.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import usermanagement.userManagement.exception.*;
import usermanagement.userManagement.model.User;
import usermanagement.userManagement.repository.UserRepository;
import usermanagement.userManagement.validation.ValidateUser;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    private static final Logger log = LogManager.getLogger(UserService.class);
    @Autowired
    private ValidateUser validateUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Value("${spring.redis.hashKey}")
    private String userKey;

    @Retryable(
            value = {Exception.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public User createUser(User user) throws UserAlreadyExistsException, ValidationException {
        log.info("create user {}", user);
        validateUser.validateUser(user);
        Optional<User> alreadyPresentOrNot = userRepository.getUserByUserNameOrEmail(user.getEmail(), user.getUsername());
        if (alreadyPresentOrNot.isPresent()) {
            throw new UserAlreadyExistsException("User already exists with emailId:- " + user.getEmail() + " or username:- " + user.getUsername());
        }
        final User savedUser = userRepository.save(user);
        CompletableFuture.runAsync(() -> {
            try {
                redisService.hSet(userKey, user.getId().toString(), savedUser);
            } catch (RedisCustomeException e) {
                log.error("Exception occured in create user while adding data in redis server");
                //Ignore Exception
            }
        });
        return savedUser;
    }

    @Retryable(
            exclude = {UserNotFoundException.class},
            value = {InternalServerException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    public User getUserById(Long id) throws UserNotFoundException {
        log.info("get User by id:-"+ id);
        User savedUser = null;
        try {
            Object userInCache = redisService.hGet(userKey, id.toString());
            log.info("Data from redis server {}", userInCache);
            if (Objects.isNull(userInCache)) {
                Optional<User> optionalUser = userRepository.getUserById(id);
                if (optionalUser.isEmpty()) {
                    throw new UserNotFoundException("User does not exists with userId:- " + id);
                }
                savedUser = optionalUser.get();
                final User finalUser = savedUser;
                redisService.hSet(userKey, id.toString(), finalUser);
            } else {
                savedUser = (User) userInCache;
            }
        } catch (RedisCustomeException e) {
            throw new InternalServerException("Internal Server Exception");
        }
        return savedUser;
    }


    @Retryable(
            exclude = {UserNotFoundException.class, ValidationException.class},
            value = {InternalServerException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public User updateUser(Long id, User userDetails) throws UserNotFoundException, ValidationException {
        validateUser.validateUser(userDetails);
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            throw new UserNotFoundException("User does not exists with userId:- "+ id );
        }
        User savedUser = optionalUser.get();
        savedUser.setUsername(userDetails.getUsername());
        savedUser.setEmail(userDetails.getEmail());
        savedUser.setAge(userDetails.getAge());
        try {
            redisService.hSet(userKey, id.toString(), savedUser);
        } catch (RedisCustomeException e) {
            throw new InternalServerException("Internal Server Exception");
        }
        return userRepository.save(savedUser);
    }


    @Retryable(
            exclude = {UserNotFoundException.class},
            value = {InternalServerException.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteUser(Long id) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.getUserById(id);
        log.info("delete user details for user id:-"+ id);
        if(optionalUser.isEmpty()){
            throw new UserNotFoundException("User does not exists with userId:- "+ id );
        }
        try{
            redisService.hDelete(userKey, id.toString());
        } catch (RedisCustomeException e) {
            throw new InternalServerException("Internal Server Exception");
        }
        userRepository.delete(optionalUser.get());
    }
}

