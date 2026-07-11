package uk.gov.moj.cp.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RootControllerTest {

    private final RootController controller = new RootController();

    @Test
    void shouldReturnWelcomeMessage() {
        ResponseEntity<String> response = controller.welcome();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Welcome to stg-vsp-subscription-service");
    }
}
