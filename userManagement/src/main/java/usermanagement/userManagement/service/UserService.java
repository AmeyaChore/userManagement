package usermanagement.userManagement.service;

import io.github.resilience4j.retry.annotation.Retry;
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

    @Autowired
    private ValidateUser validateUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Value("${spring.redis.hashKey}")
    private String userKey;

    @Retryable(
            retryFor = {Exception.class, RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public User createUser(User user) throws UserAlreadyExistsException, ValidationException {
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
                //Ignore Exception
            }
        });
        return savedUser;
    }

    @Retryable(
            retryFor = {Exception.class, InternalServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    public User getUserById(Long id) throws UserNotFoundException {
        User savedUser = null;
        try {
            Object userInCache = userInCache = redisService.hGet(userKey, id.toString());
            if (Objects.isNull(userInCache)) {
                Optional<User> optionalUser = userRepository.findById(id);
                if (optionalUser.isEmpty()) {
                    throw new UserNotFoundException("User does not exists with userId:- " + id);
                }
                savedUser = optionalUser.get();
                final User finalUser = savedUser;
                redisService.hSet(userKey, id.toString(), finalUser);
            } else {
                savedUser = (User) userInCache;
            }
        }catch (RedisCustomeException e) {
        throw new InternalServerException("Internal Server Exception");
    }
        return savedUser;
    }


    @Retryable(
            retryFor = {Exception.class, InternalServerException.class},
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
            retryFor = {Exception.class, InternalServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000))
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteUser(Long id) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.getUserById(id);
        if(optionalUser.isEmpty()){
            throw new UserNotFoundException("User does not exists with userId:- "+ id );
        }
        try{
            redisService.hDelete(userKey, id.toString());
        } catch (RedisCustomeException e) {
            throw new InternalServerException("Internal Server Exception");
        }
        userRepository.deleteById(id);
    }
}

