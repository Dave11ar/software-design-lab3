package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.database.ProductDatabase;
import ru.akirakozov.sd.refactoring.utils.HTMLResponseMaker;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
            StringBuilder src = new StringBuilder();
            productDatabase.getProducts().forEach(entry -> src.append(
                HTMLResponseMaker.makeRow(entry.getKey(), entry.getValue().toString())).append("\n"));

            response.getWriter().println(HTMLResponseMaker.withHTMLWrapper(src.toString()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
