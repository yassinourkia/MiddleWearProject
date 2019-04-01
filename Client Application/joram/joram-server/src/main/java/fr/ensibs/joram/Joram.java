package fr.ensibs.joram;

import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.Context;

import fr.dyade.aaa.agent.AgentServer;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
* Class that allows to start a JORAM server. Can be used as a standalone
* application or by creating an instance and invoking its start or run method
* to start the server asynchronousely or synchronousely
*/
public class Joram extends Thread
{

  /**
  * The JORAM server id
  */
  private static final int SERVER_ID = 0;

  /**
  * The name of the ConnectionFactory object in the JNDI repository
  */
  private static final String FACTORY_NAME = "ConnectionFactory";

  /**
  * the local host name
  */
  private String host;

  /**
  * the jndi port number. The server port number is port+1
  */
  private int port;

  /**
  * Print a usage message and exit
  */
  private static void usage()
  {
    System.out.println("Usage: Joram <port>");
    System.out.println("   Start the JORAM server");
    System.exit(-1);
  }

  /**
  * Standalone application entry point
  *
  * @param args see usage
  */
  public static void main(String[] args) throws IOException
  {
    if (args.length != 1 || args[0].equals("-h")) {
      usage();
    }

    try {
      int port = Integer.parseInt(args[0]);
      Joram joram = new Joram(port);
      joram.run();
    } catch (NumberFormatException e) {
      usage();
    }
  }

  /**
  * Constructor
  *
  * @param port the JNDI server port number
  */
  public Joram(int port) throws IOException
  {
    this.host = InetAddress.getLocalHost().getHostName();
    this.port = port;
    makeConfig();
  }

  /**
  * Start the JORAM server
  */
  @Override
  public void run()
  {
    try {
      File directory = new File(System.getProperty("user.home"), ".joram");
      String[] serverArgs = {Integer.toString(SERVER_ID), directory.getAbsolutePath()};

      AgentServer.main(serverArgs);
      Thread.sleep(1000);
      makeConnectionFactory();

      System.out.println("JORAM server running on " + host + ":" + port);

    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void close()
  {
    AgentServer.stop();
  }

  /**
  * Create a ConnectionFactory instance and register it to the JNDI repository
  */
  private void makeConnectionFactory() throws Exception
  {
    Context context = null;
    try {
      // connect to the admin module and create an anonymous user
      AdminModule.connect("localhost", port+1, "root", "root");
      User.create("anonymous", "anonymous", SERVER_ID);

      // create a ConnectionFactory object and register it
      ConnectionFactory factory = TcpConnectionFactory.create(host, port+1);
      System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      System.setProperty("java.naming.factory.host", "localhost");
      System.setProperty("java.naming.factory.port", Integer.toString(port));
      context = new InitialContext();
      context.rebind(FACTORY_NAME, factory);

    } finally {
      // close all resources
      if (context != null) {
        try { context.close(); } catch (Exception e) { }
      }
      AdminModule.disconnect();
    }
  }

  /**
  * Make the config file in the user working directory
  */
  private void makeConfig() throws IOException
  {
    File dir = new File(System.getProperty("user.dir"));
    File configFile = new File(dir, "a3servers.xml");
    configFile.deleteOnExit();

    String[] lines = {
      "<?xml version=\"1.0\"?>",
      "<config>",
      "  <property name=\"Transaction\" value=\"fr.dyade.aaa.util.NTransaction\" />",
      "  <server id=\"" + SERVER_ID + "\" name=\"joram\" hostname=\"" + host + "\">",
      "    <service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\"",
      "	     args=\"root root\" />",
      "    <service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\"",
      "	     args=\"" + (port+1) + "\" />",
      "    <service class=\"fr.dyade.aaa.jndi2.server.JndiServer\"",
      "      args=\"" + port + "\" />",
      "  </server>",
      "</config>"
    };

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
      for (String line : lines) {
        writer.write(line + "\n");
      }
    }
  }

}
