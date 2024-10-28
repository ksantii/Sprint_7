package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import pojo.Courier;
import pojo.LoginRequest;
import pojo.Endpoints;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@DisplayName("POST /api/v1/courier/login Логин курьера")
public class CourierLoginTests {
    private Response response;
    private Courier courier;
    private LoginRequest loginRequest;

    @Before
    public void setUp() {
        RestAssured.baseURI = Endpoints.BASE_URI;
        String login = Courier.generateRandomLogin();
        String password = Courier.generateRandomPassword();
        courier = new Courier(login, password, Courier.generateRandomFirstName());
        loginRequest = new LoginRequest(login, password);

        given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(Endpoints.COURIER)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @DisplayName("Курьер может войти в систему")
    @Description("Проверка, что курьер может успешно войти в систему")
    public void courierCanLogIn() {
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("Авторизация с пустым логином")
    @Description("Проверка, что при пустом логине возвращается ошибка")
    public void authorizationRequiresEmptyLogin() {
        loginRequest.setLogin("");
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация с пустым паролем")
    @Description("Проверка, что при пустом пароле возвращается ошибка")
    public void authorizationRequiresEmptyPassword() {
        loginRequest.setPassword("");
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Возврат ошибки для неправильного логина с валидным паролем")
    @Description("Проверка, что система возвращает ошибку для неправильного логина")
    public void returnErrorForIncorrectLoginWithValidPassword() {
        loginRequest.setLogin("11111111");
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Возврат ошибки для неправильного пароля с валидным логином")
    @Description("Проверка, что система возвращает ошибку для неправильного пароля")
    public void returnErrorForIncorrectPasswordWithValidLogin() {
        loginRequest.setPassword("aaaaa");
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация под несуществующем курьером")
    @Description("Проверка, что система возвращает ошибку для несуществующего курьером")
    public void returnErrorForNonExistentUser() {
        loginRequest.setLogin(Courier.generateRandomLogin());
        loginRequest.setPassword(Courier.generateRandomPassword());
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация без логина")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void authorizationRequiresMissingLogin() {
        loginRequest.setLogin(null);
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void authorizationRequiresMissingPassword() {
        loginRequest.setPassword(null);
        response = loginCourier(loginRequest);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @After
    public void tearDown() {
        String id = response.jsonPath().getString("id");
        String deleteBody = String.format("{\"id\":\"%s\"}", id);
        given()
                .header("Content-type", "application/json")
                .body(deleteBody)
                .when()
                .delete(String.format(Endpoints.COURIER + "/" + id));
    }

    @Step("Авторизация курьера")
    private Response loginCourier(LoginRequest loginRequest) {
        return given()
                .header("Content-Type", "application/json")
                .body(loginRequest)
                .when() // Исправлено: добавлено .when() перед post
                .post(Endpoints.COURIER_LOGIN)
                .then()
                .extract().response();
    }
}