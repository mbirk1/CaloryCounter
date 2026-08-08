package de.birk.calory.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import de.birk.calory.exception.FoodInUseException;

public class ExceptionHandlerImplTest {

  private final ExceptionHandlerImpl handler = new ExceptionHandlerImpl();
  private final WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

  @Test
  public void mapsTheFoodNotFoundMessageToAGermanTextTest() {
    ResponseEntity<Object> response =
        this.handler.handleNotFound(new NoSuchElementException("Food not found"), this.request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isEqualTo("Dieses Lebensmittel wurde bereits gelöscht.");
  }

  @Test
  public void keepsTheGenericMessageForOtherNoSuchElementExceptionsTest() {
    ResponseEntity<Object> response =
        this.handler.handleNotFound(new NoSuchElementException(), this.request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isEqualTo("Requested element not found.");
  }

  @Test
  public void mapsFoodInUseExceptionToAConflictResponseTest() {
    ResponseEntity<Object> response =
        this.handler.handleFoodInUse(new FoodInUseException(), this.request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody())
        .isEqualTo("Dieses Lebensmittel ist Teil eines Rezepts und kann nicht gelöscht werden.");
  }
}
