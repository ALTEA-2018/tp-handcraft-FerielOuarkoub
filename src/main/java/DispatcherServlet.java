

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@WebServlet(urlPatterns = "/*", loadOnStartup = 1)
public class DispatcherServlet extends HttpServlet {

    private Map<String, Method> uriMappings = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {

        try {
        System.out.println("Getting request for " + req.getRequestURI());
        resp.setContentType("text/html") ;
        PrintWriter out = resp.getWriter() ;
        var uri = req.getRequestURI();
        var method = uriMappings.get(uri);
        var controller = method.getDeclaringClass().getDeclaredConstructor().newInstance();

           var result =  method.invoke(controller);


            out.print(result.toString());


        } catch (IllegalAccessException | NoSuchMethodException | InstantiationException e) {

            resp.sendError(500,"exception when calling method someThrowingMethod : some exception message");
        } catch (InvocationTargetException e) {
            resp.sendError(500,"exception when calling method someThrowingMethod : some exception message");

        } catch (NullPointerException e) {
            resp.sendError(404,"no mapping found for request uri /test");
        }



    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // on enregistre notre controller au démarrage de la servlet
        this.registerController(HelloController.class);
    }

    protected void registerController(Class controllerClass){
        System.out.println("Analysing class " + controllerClass.getName());
        if(controllerClass.getAnnotation(Controller.class) == null) {
            throw new IllegalArgumentException() ;
        }
        else {
            for (Method m : controllerClass.getDeclaredMethods()) {
                registerMethod(m);
            }
        }

    }

    protected void registerMethod(Method method) {
        System.out.println("Registering method " + method.getName());
            // test if method has annotation RequestMappin && que ce n'est pas un type alors, on enregistre le mapping
            RequestMapping a = method.getAnnotation(RequestMapping.class);
            if ((a != null) && (method.getReturnType() != void.class)) {
                uriMappings.put(a.uri() ,method);
            }
        }



    protected Map<String, Method> getMappings(){
        return this.uriMappings;
    }

    protected Method getMappingForUri(String uri){
        return this.uriMappings.get(uri);
    }
}