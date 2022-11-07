package ru.akirakozov.sd.refactoring.servlet;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import ru.akirakozov.sd.refactoring.database.ProductDatabase;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletTest {
    private static final ProductDatabase productDatabase = new ProductDatabase("jdbc:sqlite:test.db");

    private static final AddProductServlet addProductServlet = new AddProductServlet(productDatabase);
    private static final GetProductsServlet getProductsServlet = new GetProductsServlet(productDatabase);
    private static final QueryServlet queryServlet = new QueryServlet(productDatabase);

    @BeforeClass
    public static void initDB() {
        try {
            productDatabase.initDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void cleanDB() {
        try {
            productDatabase.cleanDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getProductsEmpty() {
        assertGetProducts(makeHTML());
    }

    @Test
    public void addOneProduct() {
        assertAddProduct("MacBook Air", "99000");
        assertGetProducts(makeHTML(makeRow("MacBook Air", "99000")));
    }

    @Test
    public void addManyProducts() {
        int testCount = 100;
        List<String> result = new ArrayList<>();

        for (int i = 0; i < testCount; ++i) {
            String string = String.valueOf(i);
            assertAddProduct(string, string);
            result.add(makeRow(string, string));
        }

        assertGetProducts(makeHTML(result));
    }

    @Test
    public void addOverlapping() {
        assertAddProduct("Belhaven Black", "165");
        assertAddProduct("Belhaven Black", "180");

        assertGetProducts(makeHTML(
                makeRow("Belhaven Black", "165"),
                makeRow("Belhaven Black", "180")
        ));
    }

    private void productsList() {
        assertAddProduct("Shawarma ot Shera", "200");
        assertAddProduct("Baltika 9", "110");
        assertAddProduct("Barcelo Imperial Onyx", "1980");
        assertAddProduct("Barcelo Anejo Dark", "1399");
        assertAddProduct("Ford Kuga", "1600000");
    }

    @Test
    public void aggregateMax() {
        productsList();
        assertQuery("max", makeHTML(
                "<h1>Product with max price: </h1>",
                makeRow("Ford Kuga", "1600000")
        ));
    }

    @Test
    public void aggregateMin() {
        productsList();
        assertQuery("min", makeHTML(
                "<h1>Product with min price: </h1>",
                makeRow("Baltika 9", "110")
        ));
    }

    @Test
    public void aggregateSum() {
        productsList();
        assertQuery("sum", makeHTML(
                "Summary price: ",
                "1603689"
        ));
    }

    @Test
    public void aggregateCount() {
        productsList();
        assertQuery("count", makeHTML(
                "Number of products: ",
                "5"
        ));
    }

    @Test
    public void aggregateUnexpected() {
        productsList();
        assertQuery("bruh bro", "Unknown command: bruh bro\n");
    }

    private static void execQuery(String sql) {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeHTML(String ...elements) {
        return makeHTML(List.of(elements));
    }
    private String makeHTML(List<String> elements) {
        return elements.size() == 0 ? 
            String.join("\n", "<html><body>", "</body></html>", "") : 
            String.join("\n", "<html><body>", String.join("\n", elements), "</body></html>", "");
    }

    private String makeRow(String key, String val) {
        return key + "\t" + val + "</br>";
    }

    private void assertAddProduct(String name, String price) {
        assertServlet(addProductServlet, "GET", "OK\n", Map.of("name", name, "price", price));
    }

    private void assertGetProducts(String expected) {
        assertServlet(getProductsServlet, "GET", expected, Collections.emptyMap());
    }

    private void assertQuery(String command, String expected) {
        assertServlet(queryServlet, "GET", expected, Collections.singletonMap("command", command));
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertServlet(HttpServlet servlet, String method, String expected, Map<String, String> parameters) {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            parameters.forEach((key, value) ->
                    when(request.getParameter(key)).thenReturn(value)
            );

            when(request.getMethod()).thenReturn(method);
            when(response.getWriter()).thenReturn(printWriter);

            servlet.service(request, response);

            assertThat(stringWriter).hasToString(expected);
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
