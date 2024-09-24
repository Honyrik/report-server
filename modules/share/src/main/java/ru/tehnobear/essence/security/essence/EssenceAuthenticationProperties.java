package ru.tehnobear.essence.security.essence;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "app.security.essence")
public class EssenceAuthenticationProperties {
    /*
      url: http://localhost:9020/api
      cookieKey: essence.sid
      sessionKey: session
      mapRole:
        - action: 515
          role: ROLE_ADMIN
        - action: 516
          role: ROLE_ADMIN
     */
    private String url;
    private String cookieKey = "essence.sid";
    private String sessionKey = "session";
    private String userKey = "ck_id";
    private String loginQuery = "Login";
    private String sessionQuery = "GetSessionData";
    private List<MapRole> mapRole;

    @Data
    public static class MapRole {
        private Integer action;
        private String role;
    }
}
