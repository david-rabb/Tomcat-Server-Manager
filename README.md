# Tomcat Server Manager
A simple extension to Apache Tomcat 8 to allow the ManagerServlet to manage webapps in any virtual host on the server.

The manager webapp included with Apache Tomcat lets you manage the other webapps installed in the same virtual host. It provides webapp functions for listing, reloading, deploying, etc., but the manager webapp has to be installed in each virtual host, causing both runtime and configuration overhead for servers with many virtual hosts. 

In addition, while you can add username/password authentication and a RemoteIpValve to restrict access, it would be better to not have the manager web app exposed in any public vhosts and running for localhost only. Manager is a privileged application and it's safer if the internet isn't aware you are running it. 

This is about 10 years overdue, but the text-based ManagerServlet has been extended to allow management of Contexts in any Host contained in the Catalina Server.

## Deployment
To add this functionality to your Tomcat 8 server:

1. Build the source or download [drx-server-manager.jar](https://github.com/david-rabb/Tomcat-Server-Manager/releases/download/R1/drx-server-manager-8.0.jar)
2. Copy drx-server-manager.jar to tomcat8/lib/
3. Edit manager/WEB-INF/web.xml, changing the classname of org.apache.catalina.manager.ManagerServlet to __drx.tomcat.ServerManagerServlet__
4. Add Tomcat Manager webapp to one of the virtual hosts on your server, e.g. localhost
5. Invoke the manager servlet according to your config, e.g. 

   > curl -u user:pass "http://localhost:8443/manager/text/reload?path=/contextname&host=mydomain.com"

## Requirements
* JDK 8
* Tomcat 8
