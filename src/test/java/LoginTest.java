import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class LoginTest {

    private final Courier courier;
    private final String testCase;
    private final String expectedMessage;
    private final int expectedStatusCode;
    private Gson gson = new GsonBuilder().create();


    public LoginTest(String testCase, Courier courier, String expectedMessage, int expectedStatusCode) {
        this.testCase = testCase;
        this.courier = courier;
        this.expectedMessage = expectedMessage;
        this.expectedStatusCode = expectedStatusCode;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"Check login a new courier", new Courier("mlcourier16", "qwerty", null), null, 200},
                {"Check login a courier with an incorrect password", new Courier("mlcourier16", "qwerty2", null), "Учетная запись не найдена", 404},
                {"Check login a courier with an incorrect login", new Courier("mlcourier000", "qwerty", null), "Учетная запись не найдена", 404},
                {"Check login a courier without a login", new Courier(null, "qwerty", null), "Недостаточно данных для входа", 400},
                {"Check login a courier without a password", new Courier("mlcourier16", "", null), "Недостаточно данных для входа", 400},
                {"Check login a courier without a password and login", new Courier("", "", null), "Недостаточно данных для входа", 400}
        });
    }

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Login Courier test")
    public void loginCourierTest() {
        loginCourier();
    }

    @Step("Логин курьера")
    private void loginCourier() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(gson.toJson(courier))
                .when()
                .post("/api/v1/courier/login");
        response.then()
                .assertThat()
                .statusCode(expectedStatusCode);
        if (expectedStatusCode == 200) {
            response.then().assertThat().body("id", notNullValue());
        } else {
            response.then()
                    .assertThat()
                    .body("message", equalTo(expectedMessage));
        }
    }
}