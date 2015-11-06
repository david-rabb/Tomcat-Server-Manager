# Tomcat-Server-Manager
A simple extension to Apache Tomcat 8 to allow the ManagerServlet to manage webapps in any virtual host on the server.

The manager webapp included with Apache Tomcat lets you manage the other webapps installed in the same virtual host. It provides webapp functions for listing, reloading, deploying, etc., but the manager webapp has to be installed in each virtual host, causing both runtime and configuration overhead for servers with many virtual hosts.

This is about 10 years overdue, but the text-based ManagerServlet has been extended to allow management of Contexts in any Host contained in the Catalina Server.

To add this functionality to your Tomcat 8 server

1. Build the source or download dr-server-manager.jar
2. Edit manager/WEB-INF/web.xml, changing the classname of org.apache.catalina.manager.ManagerServlet to dr.catalina.manager.ManagerServlet
3. Invoke the manager servlet according to your config, e.g. 

   > curl -u user:pass http://localhost:8443/tomcat/text/reload?path=/contextname&host=github.com
