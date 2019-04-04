package fr.ensibs.river;

import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import com.sun.jini.start.ServiceDescriptor;
import com.sun.jini.start.NonActivatableServiceDescriptor;
import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;
import net.jini.config.NoSuchEntryException;

/**
* Used to initialize the River services configurations. Relies on a
* configuration file copied in the user working directory.
*/
public class RiverConfiguration implements Configuration
{

  public static String GROUP = "river-group";

  public static String CONFIG_FILE = "river.config";
  public static String JAR_FILE = "river-1.0.jar";
  public static String POLICY = System.getProperty("user.home") + File.separator + ".java.policy";

  private Map<String, Map<String, Object>> entries;

  public RiverConfiguration(String host, int port) throws Exception
  {
    this.entries = new HashMap<>();

    File configFile = new File(CONFIG_FILE);
    configFile.deleteOnExit();
    copy(RiverConfiguration.class.getResourceAsStream("/" + CONFIG_FILE), new FileOutputStream(configFile));

    ServiceDescriptor[] serviceDescriptors = makeServiceDescriptors(host, port);
    put("com.sun.jini.start", "serviceDescriptors", serviceDescriptors);
  }

  @Override
  public Object getEntry(String component, String name, Class type) throws ConfigurationException
  {
    if (component == null || name == null || type == null) {
      throw new NullPointerException(component + " " + name);
    }
    Object entry = get(component, name);
    if (entry != null) {
      if (type.isAssignableFrom(entry.getClass())) {
        return entry;
      } else {
        throw new ConfigurationException (component + " " + name);
      }
    } else {
      return null;
    }
  }

  @Override
  public Object getEntry(String component, String name, Class type, Object defaultValue) throws NoSuchEntryException, ConfigurationException
  {
    if (defaultValue == null) {
      return getEntry(component, name, type);
    }
    Object entry = getEntry(component, name, type);
    if (entry != null) {
      return entry;
    }
    if (defaultValue != Configuration.NO_DEFAULT) {
      return defaultValue;
    }
    throw new NoSuchEntryException (component + " " + name);
  }

  @Override
  public Object getEntry(String component, String name, Class type, Object defaultValue, Object data) throws NoSuchEntryException, ConfigurationException
  {
    // System.out.println("RiverConfiguration [" + component + "," + name + "," + type.getName() + "," + defaultValue + "," + data + "]");
    Object entry = getEntry(component, name, type, defaultValue);
    // System.out.println("     " + entry);
    return entry;
  }

  @Override
  public String toString()
  {
    return entries.toString();
  }

  private ServiceDescriptor[] makeServiceDescriptors(String host, int port) throws Exception
  {
    String base = getBaseDir();
    return new ServiceDescriptor[] {
      new NonActivatableServiceDescriptor("", POLICY, base + "lib/tools-2.2.3.jar", "com.sun.jini.tool.ClassServer", new String[]{ "-port", Integer.toString(port), "-dir", base + "lib", "-verbose"}),
      new NonActivatableServiceDescriptor("",  POLICY, base + "lib/reggie-2.2.3.jar", "com.sun.jini.reggie.TransientRegistrarImpl", new String[] { CONFIG_FILE }),
      // new RiverConfiguration("com.sun.jini.reggie", host), null, null),
      new NonActivatableServiceDescriptor ("", POLICY, base + "lib/outrigger-2.2.3.jar", "com.sun.jini.outrigger.TransientOutriggerImpl", new String[] { CONFIG_FILE }),
      // new RiverConfiguration("com.sun.jini.outrigger", host), null, null)
      new NonActivatableServiceDescriptor ("", POLICY, base + "lib/mahalo-2.2.3.jar", "com.sun.jini.mahalo.TransientMahaloImpl", new String[] { CONFIG_FILE })
    };
  }

  private String getBaseDir()
  {
    int configLength = JAR_FILE.length() + 2 + CONFIG_FILE.length();
    URL url = RiverConfiguration.class.getResource("/" + CONFIG_FILE);
    if (url != null) {
      String base = url.getFile();
      base = base.substring(0, base.length() - configLength);
      if (base.startsWith("file:")) {
        base = base.substring(5);
      }
      if (base.endsWith("lib/")) {
        base = base.substring(0, base.length() - 4);
      }
      System.out.println("getBaseDir:\n   url=" + url + "\n   base=" + base);
      return base;
    }
    return "target/";
  }

  private Object get(String component, String name)
  {
    Map<String, Object> map = entries.get(component);
    if (map != null) {
      return map.get(name);
    }
    return null;
  }

  private void put(String component, String name, Object entry)
  {
    Map<String, Object> map = entries.get(component);
    if (map == null) {
      map = new HashMap<>();
      entries.put(component, map);
    }
    map.put(name, entry);
  }

  private void copy(InputStream in, OutputStream out) throws IOException
  {
    byte[] buffer = new byte[1024];
    int l = in.read(buffer);
    while (l > 0) {
      out.write(buffer, 0, l);
      l = in.read(buffer);
    }
    in.close();
    out.close();
  }
}
