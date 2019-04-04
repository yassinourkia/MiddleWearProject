package fr.ensibs.river;

import java.io.PrintStream;
import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.space.JavaSpace;

/**
* Class used to a client application to look for the River JavaSpace service
*
* @author launay
*/
public class RiverLookup extends Thread
{

  private static String GROUP = "river-group";
  private static final long DEFAULT_TIMEOUT = 3000; // default lookup timeout

  public RiverLookup() throws Exception
  {
      // System.out.println("Error stream assigned to \"river-lookup.err\"");
    System.setErr(new PrintStream("river-lookup.err"));
  }

  /**
  * Look for the JavaSpace service
  *
  * @param host the server host name
  * @param port the server's port number
  * @param serviceClass the class of the service (e.g. JavaSpace.class, TransactionManager.class)
  * @return the JavaSpace service or null if no service is found after the
  * default timeout
  */
  public <T> T lookup(String host, int port, Class<T> serviceClass) throws Exception
  {
    return lookup(host, port, serviceClass, DEFAULT_TIMEOUT);
  }

  /**
  * Look for the JavaSpace service (JavaSpace.class)
  *
  * @param host the server host name
  * @param port the server's port number
  * @param serviceClass the class of the service (e.g. JavaSpace.class, TransactionManager.class)
  * @param timeout the maximum time in ms to wait for the service. A negative
  * value means no timeout
  * @return the JavaSpace service or null if no service is found after the given
  * timeout (if timeout > 0)
  */
  public <T> T lookup(String host, int port, Class<T> serviceClass, long timeout) throws Exception
  {
    T service = null;

    // create a lookup discovery manager
    String[] groups = new String[]{ GROUP };
    LookupLocator[] locators = { new LookupLocator("jini://" + host + ":" + port) };
    LookupDiscoveryManager discoveryManager = new LookupDiscoveryManager(groups, locators, null);

    // create a service discovery manager
    ServiceDiscoveryManager serviceDiscoveryManager = new ServiceDiscoveryManager(discoveryManager, null);

    // template for the type of service requested (i.e. serviceClass)
    Class[] serviceTypes = new Class[]{ serviceClass };
    ServiceTemplate template = new ServiceTemplate(null, serviceTypes, new Entry[0]);

    // lookup for the JavaSpace service
    ServiceItem item = serviceDiscoveryManager.lookup(template, null, timeout);
    if (item != null && item.service != null) {
      service = (T) item.service;
    }

    // try to terminate all lookup threads
    discoveryManager.terminate();
    serviceDiscoveryManager.terminate();
    return service;
  }
}
