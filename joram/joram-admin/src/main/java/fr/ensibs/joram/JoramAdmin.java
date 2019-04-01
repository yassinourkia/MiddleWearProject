package fr.ensibs.joram;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;

/**
* JORAM administration tool: connecting to JORAM server, and creating a topic or queue.
* Can be used as a standalone application or from another class
*/
public class JoramAdmin implements Closeable {

  /**
  * The help message for the standalone application
  */
  private static final String HELP_MESSAGE = "Available commands: "
  + "\n  queue <queue_name>   create a queue with the given name"
  + "\n  topic <topic_name>   create a topic with the given name"
  + "\n  list queues          list the existing queues"
  + "\n  list topics          list the existing topics"
  + "\n  delete <name>        delete the destination (queue or topic) with the given name"
  + "\n  help                 display this help message"
  + "\n  quit                 quit this application";

  /**
  * the JORAM server id
  */
  private static final int SERVER_ID = 0;

  /**
  * the server host name
  */
  private String host;

  /**
  * the jndi port number. The server port number is port+1
  */
  private int port;

  /**
  * the JNDI context
  */
  private Context context;

  /**
  * Administration tool standalone application entry point
  *
  * @param args [host,port]
  */
  public static void main(String args[]) throws Exception
  {

    if (args.length < 2) {
      usage();
    }

    try {
      String host = args[0];
      int port = Integer.parseInt(args[1]);
      processCommands(host, port);
    } catch (NumberFormatException e) {
      usage();
    }
  }

  /**
  * Constructor. Connects to the JORAM server and initialize the JNDI context
  *
  * @param host the JORAM server host name
  * @param port the jndi port number. The JORAM server port number is port+1
  */
  public JoramAdmin(String host, int port) throws Exception
  {
    this.host = host;
    this.port = port;

    // Connecting to JORAM server:
    AdminModule.connect(host, port+1, "root", "root");

    // Initialize the JNDI context
    System.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    System.setProperty("java.naming.factory.host", host);
    System.setProperty("java.naming.factory.port", Integer.toString(port));
    this.context = new InitialContext();
  }

  /**
  * Create a queue with the given name
  *
  * @param queueName the name of the queue to be created
  */
  public void createQueue(String queueName) throws Exception
  {
    Queue queue = Queue.create(SERVER_ID);
    this.registerDestination(queue, queueName);
  }

  /**
  * Create a topic with the given name
  *
  * @param topicName the name of the topic to be created
  */
  public void createTopic(String topicName) throws Exception
  {
    Topic topic = Topic.create(SERVER_ID);
    this.registerDestination(topic, topicName);
  }

  /**
  * Delete a destination (queue or topic)
  *
  * @param destName the name of the destination
  */
  public void deleteDestination(String destName) throws Exception
  {
    this.context.unbind(destName);
  }

  /**
  * Give a list of the registered queues names
  *
  * @return the queues names
  */
  public List<String> getQueues() throws Exception
  {
    return listNames(Queue.class.getName());
  }

  /**
  * Give a list of the registered topics names
  *
  * @return the topics names
  */
  public List<String> getTopics() throws Exception
  {
    return listNames(Topic.class.getName());
  }

  /**
  * Close the JORAM administration tool
  */
  @Override
  public void close()
  {
    try { this.context.close(); } catch (Exception e) { }
    AdminModule.disconnect();
  }

  //-----------------------------------------------------------------
  // Private methods
  //-----------------------------------------------------------------
  private void registerDestination(Destination destination, String destName) throws Exception
  {
    destination.setFreeReading();
    destination.setFreeWriting();
    this.context.rebind(destName, destination);
  }

  private List<String> listNames(String className) throws Exception
  {
    List<String> names = new ArrayList<>();
    for (NamingEnumeration<NameClassPair> e = this.context.list(""); e.hasMoreElements();) {
      NameClassPair pair = e.next();
      if (pair.getClassName().equals(className)) {
        names.add(pair.getName());
      }
    }
    return names;
  }

  private static void usage()
  {
    System.out.println("usage: JoramAdmin <host> <port>");
    System.exit(-1);
  }

  private static void processCommands(String host, int port) throws Exception
  {
    try (JoramAdmin admin = new JoramAdmin(host, port)) {
      System.out.println(HELP_MESSAGE);
      Scanner scanner = new Scanner(System.in);
      String line = scanner.nextLine();
      while (!line.equals("quit")) {
        String[] tokens = line.split(" +");
        if (tokens.length > 1) {
          switch (tokens[0]) {
            case "list":
            switch (tokens[1]) {
              case "queue":
              case "queues":
              System.out.println(admin.getQueues());
              break;
              case "topic":
              case "topics":
              System.out.println(admin.getTopics());
              break;
              default:
              System.out.println(HELP_MESSAGE);
            }
            break;
            case "queue":
            admin.createQueue(tokens[1]);
            System.out.println("Queue \"" + tokens[1] + "\" created");
            break;
            case "topic":
            admin.createTopic(tokens[1]);
            System.out.println("Topic \"" + tokens[1] + "\" created");
            break;
            case "delete":
            admin.deleteDestination(tokens[1]);
            break;
            default:
            System.out.println(HELP_MESSAGE);
          }
        } else if (tokens.length > 0) {
          System.out.println(HELP_MESSAGE);
        }
        line = scanner.nextLine();
      }
    }
  }
}
