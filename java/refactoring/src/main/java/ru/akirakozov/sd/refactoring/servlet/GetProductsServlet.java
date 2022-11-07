package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.database.ProductDatabase;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {
    private final ProductDatabase productDatabase;

    public GetProductsServlet(ProductDatabase productDatabase) {
        this.productDatabase = productDatabase;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Map.Entry<String, Long>> resultList = productDatabase.getProducts();
            PrintWriter writer = response.getWriter();

            writer.println("<html><body>");
            resultList.forEach(entry -> writer.println(entry.getKey() + "\t" + entry.getValue().toString() + "</br>"));
            writer.println("</body></html>");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
