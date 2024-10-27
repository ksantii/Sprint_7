package tests;

import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("GET /api/v1/orders Список заказов")
public class OrderListTests {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка, что в тело ответа возвращается список заказов")
    public void getOrdersList() {
        given()
                .header("Content-Type", "application/json")
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", is(not(empty())));
    }
}