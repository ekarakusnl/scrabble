package com.gamecity.scrabble.servlet;

import javax.servlet.Servlet;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

import org.glassfish.jersey.servlet.ServletContainer;

/**
 * {@link Servlet} used to inject spring dependencies to jersey resources. By default, jersey resources
 * do have a seperate context and spring beans do have a seperate context as well hence the autowired
 * spring dependencies are not initialized when jersey resources are called. This servlet intercepts when
 * a jersey resource is called and then injects the beans to the resource just before the resource method
 * is invoked.
 * 
 * @author ekarakus
 */
@WebServlet(name = "jerseyServlet", loadOnStartup = 1, urlPatterns = "/rest/*", initParams = {
        @WebInitParam(name = "jersey.config.server.provider.packages", value = "com.gamecity.scrabble.resource") })
public class JerseyServlet extends ServletContainer {

    private static final long serialVersionUID = 5416924967970280440L;

}
