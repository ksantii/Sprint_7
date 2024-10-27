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

@DisplayName("POST /api/v1/courier Создание курьера")
public class CourierTests {

    private static final String DEFAULT_PASSWORD = "1234";
    private static final String DEFAULT_FIRST_NAME = "test";

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Создание курьера")
    @Description("Проверка, что курьер может быть успешно создан")
    public void createCourier() {
        String randomLogin = Courier.generateRandomLogin();
        Courier courier = new Courier(randomLogin, DEFAULT_PASSWORD, DEFAULT_FIRST_NAME);
        // Создаем курьера и проверяем ответ
        createCourierRequest(courier).then()
                .statusCode(201)
                .body("ok", equalTo(true));
        // Логинимся и удаляем курьера
        String courierId = loginAndGetId(courier.getLogin(), courier.getPassword());
        deleteCourier(courierId);
    }

    @Test
    @DisplayName("Создание двух одинаковых курьеров")
    @Description("Проверка, что создание дубликата курьера возвращает ошибку")
    public void createDuplicateCourier() {
        String randomLogin = Courier.generateRandomLogin();
        Courier courier = new Courier(randomLogin, DEFAULT_PASSWORD, DEFAULT_FIRST_NAME);
        // Создаем первого курьера и логинимся, чтобы получить его ID
        createCourierRequest(courier);
        String courierId = loginAndGetId(courier.getLogin(), courier.getPassword());
        // Создаем второго курьера с теми же данными
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется"));
        // Удаляем первого курьера
        deleteCourier(courierId);
    }

    @Test
    @DisplayName("Создание курьера с пустым логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithEmptyLogin() {
        Courier courier = new Courier("", DEFAULT_PASSWORD, DEFAULT_FIRST_NAME);
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с пустым паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithEmptyPassword() {
        String randomLogin = Courier.generateRandomLogin();
        Courier courier = new Courier(randomLogin, "", DEFAULT_FIRST_NAME);
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithMissingLogin() {
        Courier courier = new Courier(null, DEFAULT_PASSWORD, DEFAULT_FIRST_NAME);
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithMissingPassword() {
        String randomLogin = Courier.generateRandomLogin();
        Courier courier = new Courier(randomLogin, null, DEFAULT_FIRST_NAME);
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим именем")
    @Description("Проверка, что отсутствие имени возвращает ошибку")
    public void createCourierWithMissingFirstName() {
        String randomLogin = Courier.generateRandomLogin();
        Courier courier = new Courier(randomLogin, DEFAULT_PASSWORD, null);
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Создание курьера")
    private Response createCourierRequest(Courier courier) {
        return given()
                .header("Content-Type", "application/json")
                .body(courier)
                .when()
                .post("/api/v1/courier")
                .then()
                .extract().response();
    }

    @Step("Авторизация и получение ID курьера")
    private String loginAndGetId(String login, String password) {
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(new LoginRequest(login, password))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .extract().response();

        loginResponse.then().statusCode(200);
        return loginResponse.jsonPath().getString("id");
    }

    @Step("Удаление курьера")
    private void deleteCourier(String id) {
        Response response = given()
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .extract().response();

        response.then().statusCode(200);
        response.then().body("ok", equalTo(true));
    }
}