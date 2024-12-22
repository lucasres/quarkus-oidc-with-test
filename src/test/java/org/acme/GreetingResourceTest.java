package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@QuarkusTest
class GreetingResourceTest {

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String oidcHost;

    @ConfigProperty(name = "quarkus.oidc.client-id")
    String oidcCliId;

    @ConfigProperty(name = "quarkus.oidc.credentials.secret")
    String oidcSecret;

    @Test
    void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

    @Test
    void testPrivateRoute() {
        given()
          .auth().oauth2(getAccessToken()) // Assuming basic auth for simplicity
          .when().get("/hello/p")
          .then()
             .statusCode(200)
             .body(is("foi"));
    }

    @Test
    void testPrivateRouteUnauthorized() {
        given()
          .when().get("/hello/p")
          .then()
             .statusCode(401);
    }

    private String getAccessToken() {
        return RestAssured.given()
                .param("client_id", oidcCliId)
                .param("client_secret", oidcSecret)
                .param("username", "alice")
                .param("password", "alice")
                .param("grant_type", "password")
                .post(oidcHost + "/protocol/openid-connect/token")
                .then()
                .extract()
                .path("access_token");
    }
}