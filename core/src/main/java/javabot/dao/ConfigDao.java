package javabot.dao;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javabot.model.Config;
import javabot.operations.BotOperation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created Jun 21, 2007
 *
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a>
 */
public class ConfigDao extends BaseDao<Config> {
    String GET_CONFIG = "Config.get";

  //    @Autowired
      private ChannelDao channelDao;
      @Autowired
      private AdminDao adminDao;

      protected ConfigDao() {
          super(Config.class);
      }

      public Config get() {
          return null;//(Config) getEntityManager().createNamedQuery(ConfigDao.GET_CONFIG).getSingleResult();
      }

      public Config create() {
          final Config config = new Config();
          config.setNick(System.getProperty("javabot.nick"));
          config.setPassword(System.getProperty("javabot.password")); // optional
          config.setServer(System.getProperty("javabot.server", "irc.freenode.org"));
          config.setPort(Integer.parseInt(System.getProperty("javabot.port", "6667")));
          config.setTrigger("~");

          final List<BotOperation> it = BotOperation.list();
          final Set<String> list = new TreeSet<String>();
          for (final BotOperation operation : it) {
              list.add(operation.getName());
          }
          config.setOperations(list);
          adminDao.create(getProperty("javabot.admin.nick"));
          save(config);
          return config;
      }

      private String getProperty(String name) {
          final String value = System.getProperty(name);
          if(value == null) {
              throw new RuntimeException("Missing default configuration property: " + name);
          }
          return value;
      }

}
