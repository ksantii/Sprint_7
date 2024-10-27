package tests;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import pojo.OrderRequest;
import pojo.Endpoints;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("POST /api/v1/orders Создание заказа")
@RunWith(Parameterized.class)
public class OrderTests {

    @Parameterized.Parameter
    public List<String> colors;

    @Parameterized.Parameters(name = "Тест с цветами: {0}")
    public static Object[][] data() {
        return new Object[][]{
                {List.of("BLACK")},
                {List.of("GREY")},
                {List.of("BLACK", "GREY")},
                {List.of()}
        };
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = Endpoints.BASE_URI;
    }

    @Test
    @DisplayName("Создание заказа с цветом")
    @Description("Проверка создания заказа с различными цветами")
    public void createOrderWithColor() {
        logColors(colors);
        OrderRequest orderRequest = new OrderRequest(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                4,
                "+7 800 355 35 35",
                5,
                "2020-06-06",
                "Saske, come back to Konoha",
                colors.toArray(new String[0]) // Преобразуем список в массив
        );

        given()
                .header("Content-Type", "application/json")
                .body(orderRequest)
                .when()
                .post(Endpoints.ORDERS)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("track", notNullValue());
    }

    @Step("Тестируемые цвета: {0}")
    public void logColors(List<String> colors) {
        // Логирование цветов для отладки
    }
}