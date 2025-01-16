
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@RunWith(Parameterized.class)
public class ListOrdersTest {

    private final Order order;
    private Gson gson = new GsonBuilder().create();
    private Integer track;

    public ListOrdersTest(Order order) {
        this.order = order;
    }

    @Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {new Order("Naruto", "Uchiha", "Konoha, 142 apt.", 4, "+7 800 355 35 35", 6, "2020-06-01", "Saske, come back to Konoha", List.of("BLACK"))},
                {new Order("Sakura", "Haruno", "Konoha, 143 apt.", 5, "+7 800 355 35 36", 2, "2020-06-01", "I'm very strong", List.of("GREY"))},
                {new Order("Kakashi", "Hatake", "Konoha, 144 apt.", 6, "+7 800 355 35 37", 3, "2020-06-01", "I like reading books", List.of("BLACK", "GREY"))},
                {new Order("Hinata", "Hyuga", "Konoha, 145 apt.", 7, "+7 800 355 35 38", 1, "2020-06-01", "Naruto is my everything", null)}
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }


    @Test
    public void createOrderAndGetListTest() {
        createOrder();
        verifyOrderList();
        verifyOrderListByCourierIdAndNearestStation();
        cancelOrder();
    }


    @Step("Создание заказа")
    private void createOrder() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(order))
                .when()
                .post("/api/v1/orders");

        response.then()
                .assertThat()
                .statusCode(201)
                .body("track", notNullValue());

        track = response.path("track");
    }

    @Step("Проверка, что в тело ответа возвращается список заказов")
    private void verifyOrderList() {
        given()
                .when()
                .get("/api/v1/orders")
                .then()
                .assertThat()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("orders", instanceOf(List.class))
        ;

    }

    @Step("Проверка, что возвращаются заказы курьера на указанных станциях")
    private void verifyOrderListByCourierIdAndNearestStation() {
        given()
                .queryParam("courierId", 1)
                .queryParam("nearestStation", "[\"1\", \"2\"]")
                .when()
                .get("/api/v1/orders")
                .then()
                .assertThat()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("orders", instanceOf(List.class));

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
