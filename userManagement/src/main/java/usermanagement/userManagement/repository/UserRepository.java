package usermanagement.userManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usermanagement.userManagement.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM usermanagement.user u \n" +
            "WHERE u.id = ?1;", nativeQuery = true)
    public Optional<User> getUserById(Long id);

    @Query(value = "SELECT * FROM usermanagement.user u \n" +
            "WHERE u.email LIKE ?1 OR u.username LIKE ?2 LIMIT 1;", nativeQuery = true)
    public Optional<User> getUserByUserNameOrEmail(String email, String username);

}

