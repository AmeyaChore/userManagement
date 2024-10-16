package usermanagement.userManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import usermanagement.userManagement.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "select * from usermanagement.user u" +
            "where u.id = ?1;", nativeQuery = true)
    public Optional<User> getUserById(Long id);

    @Query(value = "select * from usermanagement.user u" +
            "where u.email like ?1 or u.username like ?2 " +
            "limit 1;", nativeQuery = true)
    public Optional<User> getUserByUserNameOrEmail(String email, String username);

}

