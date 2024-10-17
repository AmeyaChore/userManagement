package usermanagement.userManagement.validation;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import usermanagement.userManagement.exception.ValidationException;
import usermanagement.userManagement.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ValidateUser {

    public void validateUser(User user) throws ValidationException {
        log.info("Validate user {}", user);
        List<String> errorMessages = new ArrayList<>();
        if(Strings.isEmpty(user.getUsername())){
            errorMessages.add("Please provide UserName");
        }

        if(Strings.isEmpty(user.getEmail())){
            errorMessages.add("Please provide user emailId");
        }

        if(Objects.isNull(user.getAge()) || (user.getAge()<=0 || user.getAge()>150 )){
            errorMessages.add("Please provide User age between 9 to 75");
        }

        if(!CollectionUtils.isEmpty(errorMessages)){
            String message = String.join(",", errorMessages);
            throw new ValidationException(message);
        }

    }
}
