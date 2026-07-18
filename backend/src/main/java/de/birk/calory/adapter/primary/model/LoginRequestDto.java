package de.birk.calory.adapter.primary.model;

/**
 * The DTO that is used for the login of an existing user.
 *
 * @author Marius Birk
 */
public class LoginRequestDto {

  private String email;
  private String password;

  public LoginRequestDto() {
  }

  /**
   * Constructor that takes the credentials needed to log in an existing user.
   *
   * @param email the email address, used as the login name
   * @param password the raw password
   */
  public LoginRequestDto(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }
}
