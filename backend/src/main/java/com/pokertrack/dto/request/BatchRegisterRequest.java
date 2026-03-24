package com.pokertrack.dto.request;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class BatchRegisterRequest {

    public static class UserEntry {
        private String email;
        private String displayName;
        private String password; // used when mode = "individual"

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @NotEmpty
    private List<UserEntry> users;

    private String sharedPassword; // if set, all users get this password

    public List<UserEntry> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntry> users) {
        this.users = users;
    }

    public String getSharedPassword() {
        return sharedPassword;
    }

    public void setSharedPassword(String sharedPassword) {
        this.sharedPassword = sharedPassword;
    }
}
