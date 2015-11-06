package drx.tomcat;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.manager.Constants;
import org.apache.catalina.manager.ManagerServlet;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.RequestUtil;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;

/**
 * Extends ManagerServlet to enable remote management of all web 
 * applications installed within the entire Tomcat server. 
 *
 * @author David Rabb
 */
public class ServerManagerServlet extends ManagerServlet {

    /**
     * Render a list of the currently active Contexts in all virtual hosts.
     *
     * @param writer Writer to render to
     */
    @Override
    protected void list(PrintWriter writer, StringManager smClient) {

        if (debug >= 1)
            log("list: Listing contexts for virtual host '" +
                host.getName() + "'");

        
        Engine engine = (Engine)host.getParent();
        Container[] hosts = engine.findChildren();
        
        writer.println(smClient.getString("managerServlet.listed", host.getName()));
        
        
        for (int j = 0; j < hosts.length; j++) {
          if (!(hosts[j] instanceof Host)) continue;
          Host aHost = (Host)hosts[j];
          
          Container[] contexts = aHost.findChildren();
          for (int i = 0; i < contexts.length; i++) {
              Context context = (Context) contexts[i];
              if (context != null ) {
                  String displayPath = context.getPath();
                  if( displayPath.equals("") )
                      displayPath = "/";
                  if (context.getState().isAvailable()) {
                      writer.println(
                          aHost.getName() +
                          ":" + displayPath +
                          ":running" +
                          ":" + context.getManager().findSessions().length +
                          ":" + context.getDocBase()
                      );
                  } else {
                      writer.println(
                          aHost.getName() +
                          ":" + displayPath +
                          ":stopped" +
                          ":0" + 
                          ":" + context.getDocBase()
                      );
                  }
              }
          }
        }
    }

    /**
     * Reload the web application at the specified context path and host
     *
     * @param writer Writer to render to
     * @param cn Name of the application to be restarted
     */
    protected void reload(PrintWriter writer, Host aHost, ContextName cn,
            StringManager smClient) {

        if (debug >= 1)
            log("restart: Reloading web application '" + cn + "'");

        if (!validateContextName(cn, writer, smClient)) {
            return;
        }

        try {
            Context context = (Context) aHost.findChild(cn.getName());
            if (context == null) {
                writer.println(smClient.getString("managerServlet.noContext",
                        RequestUtil.filter(cn.getDisplayName())));
                return;
            }
            // It isn't possible for the manager to reload itself
            if (context.getName().equals(this.context.getName())) {
                writer.println(smClient.getString("managerServlet.noSelf"));
                return;
            }
            context.reload();
            writer.println(smClient.getString("managerServlet.reloaded",
                    aHost.getName()+":"+cn.getDisplayName()));
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            log("ManagerServlet.reload[" + cn.getDisplayName() + "]", t);
            writer.println(smClient.getString("managerServlet.exception",
                    t.toString()));
        }
    }    
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
      
      StringManager smClient = StringManager.getManager(
        Constants.Package, request.getLocales());
      
      String command = request.getPathInfo();
      if (command == null) command = request.getServletPath();
      
      String hostName = request.getParameter("host");
      
      if (command!=null && hostName!=null && command.equals("/reload")) {
        
        Engine engine = (Engine)host.getParent();
        Host aHost = (Host)engine.findChild(hostName);
        
        String path = request.getParameter("path");
        ContextName cn = null;
        if (path != null) {
            cn = new ContextName(path, request.getParameter("version"));
        }
        response.setContentType("text/plain; charset=" + Constants.CHARSET);
        PrintWriter writer = response.getWriter();
        reload(writer, aHost, cn, smClient);
        writer.flush();
        writer.close();
        
      } else {
        super.doGet(request, response);
      }
    }    
}
