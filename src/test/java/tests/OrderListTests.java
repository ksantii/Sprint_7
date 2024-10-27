package tests;

import io.restassured.RestAssured;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import pojo.Endpoints;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("GET /api/v1/orders Список заказов")
public class OrderListTests {

    @Before
    public void setUp() {
        RestAssured.baseURI = Endpoints.BASE_URI;
    }

    @Test
    @DisplayName("Получение списка заказов")
    @Description("Проверка, что в тело ответа возвращается список заказов")
    public void getOrdersList() {
        given()
                .header("Content-Type", "application/json")
                .get(Endpoints.ORDERS)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("orders", is(not(empty())));
    }
}