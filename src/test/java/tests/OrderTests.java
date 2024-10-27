package tests;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import pojo.OrderRequest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("POST /api/v1/orders Создание заказа")
@RunWith(Parameterized.class)
public class OrderTests {

    @Parameterized.Parameter
    public String color;

    @Parameterized.Parameters(name = "Тест с цветом: {0}")
    public static Object[] data() {
        return new Object[] { "BLACK", "GREY", "BLACK,GREY", null };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Создание заказа с цветом")
    @Description("Проверка создания заказа с различными цветами")
    public void createOrderWithColor() {
        logColor(color);
        OrderRequest orderRequest = new OrderRequest(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                4,
                "+7 800 355 35 35",
                5,
                "2020-06-06",
                "Saske, come back to Konoha",
                color != null ? color.split(",") : new String[]{}
        );

        given()
                .header("Content-Type", "application/json")
                .body(orderRequest)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Step("Тестируемый цвет: {0}")
    public void logColor(String color) {
    }
}