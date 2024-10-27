package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.hc.core5.http.HttpStatus;
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

    @Before
    public void setUp() {
        RestAssured.baseURI = Endpoints.BASE_URI;
    }

    @Test
    @DisplayName("Создание курьера")
    @Description("Проверка, что курьер может быть успешно создан")
    public void createCourier() {
        Courier courier = new Courier(Courier.generateRandomLogin(), Courier.generateRandomPassword(), Courier.generateRandomFirstName());
        createCourierRequest(courier).then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("ok", equalTo(true));
        String courierId = loginAndGetId(courier.getLogin(), courier.getPassword());
        deleteCourier(courierId);
    }

    @Test
    @DisplayName("Создание двух одинаковых курьеров")
    @Description("Проверка, что создание дубликата курьера возвращает ошибку")
    public void createDuplicateCourier() {
        Courier courier = new Courier(Courier.generateRandomLogin(), Courier.generateRandomPassword(), Courier.generateRandomFirstName());
        // Создаем первого курьера и логинимся, чтобы получить его ID
        createCourierRequest(courier);
        String courierId = loginAndGetId(courier.getLogin(), courier.getPassword());
        // Создаем второго курьера с теми же данными
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(HttpStatus.SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется"));
        // Удаляем первого курьера
        deleteCourier(courierId);
    }

    @Test
    @DisplayName("Создание курьера с пустым логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithEmptyLogin() {
        Courier courier = new Courier("", Courier.generateRandomPassword(), Courier.generateRandomFirstName());
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с пустым паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithEmptyPassword() {
        Courier courier = new Courier(Courier.generateRandomLogin(), "", Courier.generateRandomFirstName());
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим логином")
    @Description("Проверка, что отсутствие логина возвращает ошибку")
    public void createCourierWithMissingLogin() {
        Courier courier = new Courier(null, Courier.generateRandomPassword(), Courier.generateRandomFirstName());
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим паролем")
    @Description("Проверка, что отсутствие пароля возвращает ошибку")
    public void createCourierWithMissingPassword() {
        Courier courier = new Courier(Courier.generateRandomLogin(), null, Courier.generateRandomFirstName());
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @DisplayName("Создание курьера с отсутствующим именем")
    @Description("Проверка, что отсутствие имени возвращает ошибку")
    public void createCourierWithMissingFirstName() {
        Courier courier = new Courier(Courier.generateRandomLogin(), Courier.generateRandomPassword(), null);
        Response response = createCourierRequest(courier);
        response.then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Создание курьера")
    private Response createCourierRequest(Courier courier) {
        return given()
                .header("Content-Type", "application/json")
                .body(courier)
                .when()
                .post(Endpoints.COURIER)
                .then()
                .extract().response();
    }

    @Step("Авторизация и получение ID курьера")
    private String loginAndGetId(String login, String password) {
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(new LoginRequest(login, password))
                .when()
                .post(Endpoints.COURIER_LOGIN)
                .then()
                .extract().response();

        loginResponse.then().statusCode(HttpStatus.SC_OK);
        return loginResponse.jsonPath().getString("id");
    }

    @Step("Удаление курьера")
    private void deleteCourier(String id) {
        Response response = given()
                .header("Content-Type", "application/json")
                .when()
                .delete(Endpoints.COURIER + "/" + id)
                .then()
                .extract().response();

        response.then().statusCode(HttpStatus.SC_OK);
        response.then().body("ok", equalTo(true));
    }
}