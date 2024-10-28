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


@DisplayName("POST /api/v1/courier Создание курьера")
public class CourierTests {

    private Courier courier;
    private String login;
    private String password;

    @Before
    public void setUp() {
        RestAssured.baseURI = Endpoints.BASE_URI;
        login = Courier.generateRandomLogin();
        password = Courier.generateRandomPassword();
        courier = new Courier(login, password, Courier.generateRandomFirstName());
    }

    @After
    public void tearDown() {
        // Авторизация и получение ID курьера
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(new LoginRequest(login, password))
                .when()
                .post(Endpoints.COURIER_LOGIN);

        String courierId = loginResponse.jsonPath().getString("id");

        // Удаление курьера
        given()
                .header("Content-Type", "application/json")
                .when()
                .delete(Endpoints.COURIER + "/" + courierId);
    }

    @Test
    @DisplayName("Создание курьера")
    @Description("Проверка, что курьер может быть успешно создан")
    public void createCourier() {
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("ok", equalTo(true));
    }

    @Test
    @DisplayName("Создание двух одинаковых курьеров")
    @Description("Проверка, что создание дубликата курьера возвращает ошибку")
    public void createDuplicateCourier() {
        createCourierRequest(courier);
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Test
    @DisplayName("Создание курьера с пустым логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithEmptyLogin() {
        courier.setLogin("");
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с пустым паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithEmptyPassword() {
        courier.setPassword("");
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с пустым именем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithEmptyFirstName() {
        courier.setFirstName("");
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithMissingLogin() {
        courier.setLogin(null);
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithMissingPassword() {
        courier.setPassword(null);
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим именем")
    @Description("Проверка, что отсутствие имени возвращает ошибку")
    public void createCourierWithMissingFirstName() {
        courier.setFirstName(null);
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Создание курьера")
    @DisplayName("POST /api/v1/courier")
    public Response createCourierRequest(Courier courier) {
        return given()
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(Endpoints.COURIER);
    }
}