package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.database.ProductDatabase;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
            return "<h1>Product with max price: </h1>\n" +
                result.getKey() + "\t" + result.getValue().toString() + "</br>";
        },
        "min", result -> {
            return "<h1>Product with min price: </h1>\n" +
                result.getKey() + "\t" + result.getValue().toString() + "</br>";
        },
        "sum", result -> {
            return "Summary price: \n" + result.getValue().toString();
        },
        "count", result -> {
            return "Number of products: \n" + result.getValue().toString();
        }
    );
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");
        PrintWriter writer = response.getWriter();

        if (!commandResultHandlers.containsKey(command)) {
            writer.println("Unknown command: " + command);
        } else {
            writer.println("<html><body>");
            try {
                writer.println(commandResultHandlers.get(command).apply(productDatabase.calc(command)));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            writer.println("</body></html>");
        }
 
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
