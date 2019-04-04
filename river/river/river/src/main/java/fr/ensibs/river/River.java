package fr.ensibs.river;

import com.sun.jini.start.ServiceStarter;

import java.io.File;
import java.io.PrintStream;
import java.rmi.RMISecurityManager;

/**
* Class used to start the River JavaSpace service and allow a client
* application to look for this service
*
* @author launay
*/
public class River extends Thread
{

  public static String HOST; // the server host name
  public static int PORT;    // the server port number

  private static final String DEFAULT_HOST = "localhost"; // default server host
  private static final int DEFAULT_PORT = 8065; // default server port

  /**
  * Print a usage message and exit
  */
  private static void usage()
  {
    System.out.println("Usage: java River [options]");
    System.out.println("with options:");
    System.out.println("  -h host   the local host name (default: " + DEFAULT_HOST + ")");
    System.out.println("  -p port   the server's port number (default: " + DEFAULT_PORT + ")");
    System.exit(-1);
  }

  /**
  * To use this class as a standalone program
  *
  * @param args see usage
  */
  public static void main(String[] args) throws Exception
  {
    if (args.length % 2 != 0) {
      usage();
    }

    String host = DEFAULT_HOST;
    int port = DEFAULT_PORT;
    int idx = 0;
    while (idx < args.length - 1) {
      switch (args[idx]) {
        case "-h":
        host = args[idx + 1];
        break;
        case "-p":
        try {
          port = Integer.parseInt(args[idx + 1]);
        } catch (NumberFormatException e) {
          System.err.println(e.getClass().getName() + ": " + e.getMessage());
          usage();
        }
        break;
        default:
        usage();
      }
      idx += 2;
    }

    River river = new River(host, port);
    river.run();
  }

  /**
  * Constructor
  *
  * @param host the local host name
  * @param port the server's port number
  */
  public River(String host, int port)
  {
    River.HOST = host;
    River.PORT = port;
    System.setSecurityManager(new RMISecurityManager());
  }

  /**
  * Start the JavaSpace service
  */
  @Override
  public void run()
  {
    if (!new File(RiverConfiguration.POLICY).exists()) {
      System.err.println("Unable to start the River services: no \"" + RiverConfiguration.POLICY + "\" file");
    } else {
      try {
        System.setProperty("java.security.manager", "true");
        System.setProperty("java.security.policy", RiverConfiguration.POLICY);
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        System.setProperty("java.rmi.server.hostname", River.HOST);

        System.out.println("Error stream assigned to \"river.err\"");
        System.setErr(new PrintStream("river.err"));

        RiverConfiguration config = new RiverConfiguration(HOST, PORT);
        ServiceStarter.main(config);
        System.out.println("River services successfully started at " + HOST + ":" + PORT);
      } catch (Exception ex) {
        System.err.println("Unable to start the River services");
        ex.printStackTrace();
      }
    }
  }
}
