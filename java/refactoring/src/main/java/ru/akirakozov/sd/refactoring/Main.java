package ru.akirakozov.sd.refactoring;

import ru.akirakozov.sd.refactoring.database.ProductDatabase;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

/**
 * @author akirakozov
 */
public class Main {
    public static void main(String[] args) throws Exception {
        final ProductDatabase productDatabase = new ProductDatabase("jdbc:sqlite:test.db");
        productDatabase.initDB();

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(productDatabase)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(productDatabase)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(productDatabase)),"/query");

        server.start();
        server.join();
    }
}
