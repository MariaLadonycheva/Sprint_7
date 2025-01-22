
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class CourierTest {

    private final Courier courier;
    private Integer courierId;
    private final String testCase;
    private final String expectedMessage;
    private final int expectedStatusCode;
    private Gson gson = new GsonBuilder().create();


    public CourierTest(String testCase, Courier courier, String expectedMessage, int expectedStatusCode) {
        this.testCase = testCase;
        this.courier = courier;
        this.expectedMessage = expectedMessage;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"Check create a new courier with all fields", new Courier("mlcourier048", "qwerty", "Mary"), null, 201},
                {"Check create a new courier with required fields", new Courier("mlcourier177", "qwerty", null), null, 201},
                {"Check creating a courier with a repeated login", new Courier("mlcourier08", "qwerty", "Mary"), "Этот логин уже используется. Попробуйте другой.", 409},
                {"Creating a courier without a login", new Courier(null, "qwerty", "Mary"), "Недостаточно данных для создания учетной записи", 400},
                {"Creating a courier without a password", new Courier("mlcourier10", null, "Mary"), "Недостаточно данных для создания учетной записи", 400}
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Create Courier test")
    public void createCourierTest() {
        createCourier();
        if (expectedStatusCode == 201) {
            loginAndGetCourierId();
        }
    }


    @Step("Создание курьера")
    private void createCourier() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(gson.toJson(courier))
                .when()
                .post("/api/v1/courier");
        response.then()
                .assertThat()
                .statusCode(expectedStatusCode);
        if (expectedStatusCode == 201) {
            response.then()
                    .assertThat()
                    .body("ok", equalTo(true));
        } else {
            response.then()
                    .assertThat()
                    .body("message", equalTo(expectedMessage));
        }
    }

    @Step("Логин курьера и получение id")
    private void loginAndGetCourierId() {
        if (expectedStatusCode == 201) {
            courierId =
                    given()
                            .header("Content-type", "application/json")
                            .body(gson.toJson(courier))
                            .when()
                            .post("/api/v1/courier/login")
                            .then().extract().body().path("id");
        }
    }

    @After

    public void tearDown() {
        deleteCourier();
    }

    @Step("Удаление курьера")
    private void deleteCourier() {
        if (courierId != null) {
            given()
                    .header("Content-type", "application/json")
                    .when()
                    .delete("/api/v1/courier/{id}", courierId)
                    .then().assertThat().statusCode(200);
        }
    }

}
