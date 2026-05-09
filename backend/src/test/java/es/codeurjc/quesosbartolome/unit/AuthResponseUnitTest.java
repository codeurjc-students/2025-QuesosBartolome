package es.codeurjc.quesosbartolome.unit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import es.codeurjc.quesosbartolome.security.jwt.AuthResponse;

class AuthResponseUnitTest {

    @Test
    void constructorsSetFieldsAndToStringReflectsState() {
        AuthResponse response = new AuthResponse(AuthResponse.Status.SUCCESS, "ok", null);

        assertThat(response.getStatus()).isEqualTo(AuthResponse.Status.SUCCESS);
        assertThat(response.getMessage()).isEqualTo("ok");
        assertThat(response.getError()).isNull();
        assertThat(response.toString()).contains("status=SUCCESS", "message=ok");
    }

    @Test
    void settersUpdateFields() {
        AuthResponse response = new AuthResponse();

        response.setStatus(AuthResponse.Status.FAILURE);
        response.setMessage("bad");
        response.setError("invalid");

        assertThat(response.getStatus()).isEqualTo(AuthResponse.Status.FAILURE);
        assertThat(response.getMessage()).isEqualTo("bad");
        assertThat(response.getError()).isEqualTo("invalid");
    }
}