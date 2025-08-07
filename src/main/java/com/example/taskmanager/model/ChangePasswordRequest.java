package com.example.taskmanager.model;

/**
 * This class represents a request object for changing a user's password.
 * It contains the old password for verification and the new password to be set.
 */
public class ChangePasswordRequest {

    // The user's current password, used for authentication
    private String oldPassword;

    // The new password that the user wants to set
    private String newPassword;

    // Getter for oldPassword
    public String getOldPassword() {
        return oldPassword;
    }

    // Setter for oldPassword
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    // Getter for newPassword
    public String getNewPassword() {
        return newPassword;
    }

    // Setter for newPassword
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
