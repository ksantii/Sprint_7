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

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Создание курьера")
    @Description("Проверка, что курьер может быть успешно создан")
    public void createCourier() {
        Courier courier = new Courier("nnnninja", "1234", "saske");
        // Создаем курьера и проверяем ответ
        createCourierRequest(courier).then()
                .statusCode(201)
                .body("ok", equalTo(true));
        // Логинимся и удаляем курьера
        String courierId = loginAndGetId(new LoginRequest(courier.getLogin(), courier.getPassword()));
        deleteCourier(courierId);
    }

    @Test
    @DisplayName("Создание двух одинаковых курьеров")
    @Description("Проверка, что создание дубликата курьера возвращает ошибку")
    public void createDuplicateCourier() {
        Courier courier = new Courier("kobu", "1234", "saske");
        // Создаем первого курьера и логинимся, чтобы получить его ID
        createCourierRequest(courier);
        LoginRequest loginRequest = new LoginRequest(courier.getLogin(), courier.getPassword());
        String courierId = loginAndGetId(loginRequest);
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
        Courier courier = new Courier("", "1234", "saske");
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с пустым паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithEmptyPassword() {
        Courier courier = new Courier("nnnnfinja", "", "saske");
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithMissingLogin() {
        Courier courier = new Courier(null, "1234", "saske");
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithMissingPassword() {
        Courier courier = new Courier("nnnnffinja", null, "saske");
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим именем")
    @Description("Проверка, что отсутствие имени возвращает ошибку")
    public void createCourierWithMissingFirstName() {
        Courier courier = new Courier("nnnnffinja", "1234", null);
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
    private String loginAndGetId(LoginRequest loginRequest) {
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(loginRequest)
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