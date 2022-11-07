package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.database.ProductDatabase;
import ru.akirakozov.sd.refactoring.utils.HTMLResponseMaker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final ProductDatabase productDatabase;

    public QueryServlet(ProductDatabase productDatabase) {
        this.productDatabase = productDatabase;
    }

    private final Map<String, Function<Map.Entry<String, Long>, String>> commandResultHandlers = Map.of(
        "max", result -> {
            return HTMLResponseMaker.makeHeader("Product with max price: ") + "\n" +
                HTMLResponseMaker.makeRow(result.getKey(), result.getValue().toString()) + "\n";
        },
        "min", result -> {
            return HTMLResponseMaker.makeHeader("Product with min price: ") + "\n" +
                HTMLResponseMaker.makeRow(result.getKey(), result.getValue().toString()) + "\n";
        },
        "sum", result -> {
            return "Summary price: \n" + result.getValue().toString() + "\n";
        },
        "count", result -> {
            return "Number of products: \n" + result.getValue().toString() + "\n";
        }
    );
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if (!commandResultHandlers.containsKey(command)) {
            response.getWriter().println("Unknown command: " + command);
        } else {
            try {
                response.getWriter().println(
                    HTMLResponseMaker.withHTMLWrapper(commandResultHandlers.get(command).apply(productDatabase.calc(command))));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
 
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
