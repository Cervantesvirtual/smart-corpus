package com.cervantesvirtual.corpus;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@SpringBootApplication
public class Application extends SpringBootServletInitializer{
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Override
    public void onStartup(ServletContext container) throws ServletException {
        WebApplicationContext context = getContext();
        ServletRegistration.Dynamic registration = container.addServlet("dispatcher", new DispatcherServlet(context));
        registration.setLoadOnStartup(1);
        registration.addMapping("/*");
        super.onStartup(container);
    }

    private WebApplicationContext getContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(Application.class.getName());
        return context;
    }
}

