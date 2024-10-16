package usermanagement.userManagement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import usermanagement.userManagement.exception.RedisCustomeException;
import usermanagement.userManagement.exception.UserAlreadyExistsException;
import usermanagement.userManagement.model.User;
import usermanagement.userManagement.repository.InternalServerException;
import usermanagement.userManagement.repository.UserNotFoundException;
import usermanagement.userManagement.repository.UserRepository;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Value("${redis.hashKey}")
    private String userKey;

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public User createUser(User user) throws UserAlreadyExistsException {
        Optional<User> alreadyPresentOrNot = userRepository.getUserByUserNameOrEmail(user.getEmail(), user.getUsername());
        if (alreadyPresentOrNot.isPresent()) {
            throw new UserAlreadyExistsException("User already exists with emailId:- " + user.getEmail() + " or username:- " + user.getUsername());
        }
        final User savedUser = userRepository.save(user);
        CompletableFuture.runAsync(()-> {
            try {
                redisService.hSet(userKey, user.getId().toString(), savedUser);
            } catch (RedisCustomeException e) {
                //Ignore Exception
            }
        });
        return savedUser;
    }


    public User getUserById(Long id) throws UserNotFoundException {
        User savedUser = null;
        Object userInCache = null;
        try{
            userInCache = redisService.hGet(userKey, id.toString()) ;
        } catch (RedisCustomeException e) {
            //ignore Exception
        }
        if(Objects.isNull(userInCache)){
            Optional<User> optionalUser = userRepository.findById(id);
            if(optionalUser.isEmpty()){
                throw new UserNotFoundException("User does not exists with userId:- "+ id );
            }
            savedUser = optionalUser.get();
            final User finalUser = savedUser;
            CompletableFuture.runAsync(()-> {
                try{
                    redisService.hSet(userKey, id.toString(), finalUser);
                } catch (RedisCustomeException e) {
                    throw new InternalServerException("Internal Server Error");
                }
            });
        } else{
            savedUser = (User) userInCache;
        }
        return savedUser;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public User updateUser(Long id, User userDetails) throws UserNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);
        if(optionalUser.isEmpty()){
            throw new UserNotFoundException("User does not exists with userId:- "+ id );
        }
        User savedUser = optionalUser.get();
        savedUser.setUsername(userDetails.getUsername());
        savedUser.setEmail(userDetails.getEmail());
        savedUser.setAge(userDetails.getAge());

        CompletableFuture.runAsync(()-> {
            try{
                redisService.hSet(userKey, id.toString(), savedUser);
            } catch (RedisCustomeException e) {
                throw new InternalServerException("Internal Server Error");
            }
        });
        return userRepository.save(savedUser);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteUser(Long id) throws UserNotFoundException, InternalServerException {
        Optional<User> optionalUser = userRepository.getUserById(id);
        if(optionalUser.isEmpty()){
            throw new UserNotFoundException("User does not exists with userId:- "+ id );
        }
        try{
            redisService.hDelete(userKey, id.toString());
        } catch (RedisCustomeException e) {
            throw new InternalServerException("Internal Server Error");
        }
        userRepository.deleteById(id);
    }
}

