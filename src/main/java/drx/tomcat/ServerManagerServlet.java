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
import org.apache.catalina.manager.ManagerServlet;
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
     * To reduce the amount re-writes and refactoring, we are essentially making 
     * this class single-threaded. i.e. unpredictable results if you try to manipulate
     * contexts in two different hosts simultaneously. Simultaneous calls for the
     * same host would be safe, but not typical usuage.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
      
      Host originalHost = host;
      String hostName = request.getParameter("host");
      if (hostName!=null) {
        Engine engine = (Engine)host.getParent();
        Host aHost = (Host)engine.findChild(hostName);
        if (aHost!=null) host = aHost;
      }
      
      super.doGet(request, response);
      
      host = originalHost;
    }    
    
    
}
