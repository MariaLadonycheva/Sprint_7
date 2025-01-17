import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class OrderTest {
    private final Order order;
    private Gson gson = new GsonBuilder().create();
    private Integer track;

    public OrderTest(Order order) {
        this.order = order;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {new Order("Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 1, "2024-09-06", "Saske, come back to Konoha", List.of("BLACK"))},
                {new Order("Sakura", "Haruno", "Konoha, 143 apt.", 5, "+7 800 355 35 36", 3, "2024-06-07", "I'm very strong", List.of("GREY"))},
                {new Order("Kakashi", "Hatake", "Konoha, 144 apt.", 6, "+7 800 355 35 37", 4, "2024-06-08", "I like reading books", List.of("BLACK", "GREY"))},
                {new Order("Hinata", "Hyuga", "Konoha, 145 apt.", 7, "+7 800 355 35 38", 8, "2024-06-09", "Naruto is my everything", null)}
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Check create a new order")
    public void createOrderTest() {
        createOrder();
    }

    @Step("Создание заказа")
    private void createOrder() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(gson.toJson(order))
                .when()
                .post("/api/v1/orders");
        response.then()
                .assertThat()
                .statusCode(201)
                .body("track", notNullValue());
        track = response.path("track");
    }

    @After
    public void tearDown() {
        cancelOrder();
    }

    @Step("Отмена заказа")
    private void cancelOrder() {
        if (track != null) {
            given()
                    .queryParam("track", track)
                    .when()
                    .put("/api/v1/orders/cancel")
                    .then()
                    .assertThat()
                    .statusCode(200);
        }
    }
}

