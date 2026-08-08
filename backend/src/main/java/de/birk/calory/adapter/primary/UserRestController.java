package de.birk.calory.adapter.primary;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.birk.calory.adapter.primary.annotations.GetRequest;
import de.birk.calory.adapter.primary.annotations.PutRequest;
import de.birk.calory.adapter.primary.model.UserDetailsDto;
import de.birk.calory.adapter.primary.model.UserProfileDto;
import de.birk.calory.usecase.auth.TokenPrincipal;
import de.birk.calory.usecase.user.FindUserUsecase;
import de.birk.calory.usecase.user.UpdateUserProfileUsecase;

/**
 * RestController for requests about the currently authenticated user.
 *
 * @author Marius Birk
 */
@RestController
@RequestMapping("/api/users")
//TODO Marius Should be outsourced to env variable
@CrossOrigin(origins = "http://localhost:4200")
public class UserRestController {

  private final FindUserUsecase findUserUsecase;
  private final UpdateUserProfileUsecase updateUserProfileUsecase;

  /**
   * Every necessary usecase needed to complete the incoming requests.
   *
   * @param findUserUsecase Find User Usecase
   * @param updateUserProfileUsecase Update User Profile Usecase
   */
  public UserRestController(
      FindUserUsecase findUserUsecase,
      UpdateUserProfileUsecase updateUserProfileUsecase) {
    this.findUserUsecase = findUserUsecase;
    this.updateUserProfileUsecase = updateUserProfileUsecase;
  }

  @GetRequest("/me")
  public UserDetailsDto getCurrentUser(Authentication authentication) {
    TokenPrincipal principal = (TokenPrincipal) authentication.getPrincipal();
    return this.findUserUsecase.findById(principal.getId());
  }

  @PutRequest("/me/profile")
  public UserDetailsDto updateProfile(
      Authentication authentication,
      @RequestBody UserProfileDto userProfileDto) {
    TokenPrincipal principal = (TokenPrincipal) authentication.getPrincipal();
    return this.updateUserProfileUsecase.updateProfile(principal.getId(), userProfileDto);
  }
}
