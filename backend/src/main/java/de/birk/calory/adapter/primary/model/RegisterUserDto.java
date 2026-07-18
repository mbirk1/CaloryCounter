package de.birk.calory.adapter.primary.model;

/**
 * The DTO that is used for the registration of a new user.
 *
 * @author Marius Birk
 */
public class RegisterUserDto {

  private String email;
  private String password;

  public RegisterUserDto() {
  }

  /**
   * Constructor that takes the credentials needed to register a new user.
   *
   * @param email the email address, used as the login name
   * @param password the raw password, chosen by the user
   */
  public RegisterUserDto(String email, String password) {
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
