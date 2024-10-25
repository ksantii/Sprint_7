package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import pojo.Courier;
import pojo.LoginRequest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("POST /api/v1/courier/login Логин курьера")
public class CourierLoginTests {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Курьер может войти в систему")
    @Description("Проверка, что курьер может успешно войти в систему")
    public void courierCanLogIn() {
        Courier courier = new Courier("nnngww", "1234", "saske");
        createCourier(courier).then().statusCode(201);
        // Авторизация и получение ID
        Response loginResponse = loginCourier(new LoginRequest(courier.getLogin(), courier.getPassword()));
        loginResponse.then().statusCode(200);
        Integer courierId = loginResponse.path("id");
        // Удаляем курьера после теста
        deleteCourier(courierId.toString());
    }

    @Test
    @DisplayName("Авторизация с пустым логином")
    @Description("Проверка, что при пустом логине возвращается ошибка")
    public void authorizationRequiresEmptyLogin() {
        loginCourier(new LoginRequest("", "1234"))
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация с пустым паролем")
    @Description("Проверка, что при пустом пароле возвращается ошибка")
    public void authorizationRequiresEmptyPassword() {
        loginCourier(new LoginRequest("nnngww", ""))
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Возврат ошибки для неправильного логина с валидным паролем")
    @Description("Проверка, что система возвращает ошибку для неправильного логина")
    public void returnErrorForIncorrectLoginWithValidPassword() {
        loginCourier(new LoginRequest("11111111", "1234"))
                .then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Возврат ошибки для неправильного пароля с валидным логином")
    @Description("Проверка, что система возвращает ошибку для неправильного пароля")
    public void returnErrorForIncorrectPasswordWithValidLogin() {
        loginCourier(new LoginRequest("kobu", "aaaaa"))
                .then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация под несуществующем курьером")
    @Description("Проверка, что система возвращает ошибку для несуществующего курьером")
    public void returnErrorForNonExistentUser() {
        loginCourier(new LoginRequest("usekkkkr", "2222"))
                .then().statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Авторизация без логина")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void authorizationRequiresMissingLogin() {
        loginCourier(new LoginRequest(null, "1234"))
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @DisplayName("Авторизация без пароля")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void authorizationRequiresMissingPassword() {
        loginCourier(new LoginRequest("nnngww", null))
                .then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-Type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .extract().response();
    }

    @Step("Авторизация курьера")
    private Response loginCourier(LoginRequest loginRequest) {
        return given()
                .header("Content-Type", "application/json")
                .body(loginRequest)
                .post("/api/v1/courier/login")
                .then()
                .extract().response();
    }

    @Step("Удаление курьера")
    private void deleteCourier(String id) {
        given()
                .header("Content-Type", "application/json")
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }
}