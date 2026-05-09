package es.codeurjc.quesosbartolome.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import es.codeurjc.quesosbartolome.controller.SpaController;

class SpaControllerTest {

    private final SpaController spaController = new SpaController();

    @Test
    void forwardAlwaysReturnsIndexForward() {
        assertThat(spaController.forward(null)).isEqualTo("forward:/index.html");
        assertThat(spaController.forward("cheeses")).isEqualTo("forward:/index.html");
    }
}