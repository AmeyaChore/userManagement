package usermanagement.userManagement.model;

import lombok.*;

@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private int statusCode;
    private String message;
    private Object data;
}
