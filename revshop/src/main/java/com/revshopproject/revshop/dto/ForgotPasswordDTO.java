package com.revshopproject.revshop.dto;

public class ForgotPasswordDTO {
    private String email;
    private Long securityQuestionId;
    private String securityAnswer;
    private String newPassword;

    private String confirmPassword;

    public ForgotPasswordDTO() {
    }

    public ForgotPasswordDTO(String email, Long securityQuestionId, String securityAnswer, String newPassword, String confirmPassword) {
        this.email = email;
        this.securityQuestionId = securityQuestionId;
        this.securityAnswer = securityAnswer;
        this.newPassword = newPassword;
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getSecurityQuestionId() {
        return securityQuestionId;
    }

    public void setSecurityQuestionId(Long securityQuestionId) {
        this.securityQuestionId = securityQuestionId;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
