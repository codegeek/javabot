package javabot.commands;

import java.util.ArrayList;
import java.util.List;

import com.antwerkz.maven.SPI;
import javabot.BotEvent;
import javabot.Javabot;
import javabot.Message;
import javabot.dao.ChannelDao;
import javabot.model.Channel;
import javabot.operations.BotOperation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created Dec 17, 2008
 *
 * @author <a href="mailto:jlee@antwerkz.com">Justin Lee</a>
 */
@SPI({BotOperation.class, AdminCommand.class})
public class AddChannel extends AdminCommand {
    @Autowired
    private ChannelDao dao;
    @Param
    String channel;
    @Param(defaultValue = "true", required = false)
    String logged;
    @Param(defaultValue = "", required = false)
    String password;

    @Override
    public List<Message> execute(final Javabot bot, final BotEvent event) {
        final List<Message> responses = new ArrayList<Message>();
        if (channel.startsWith("#")) {
            Channel chan = dao.get(channel);
            final Boolean isLogged = Boolean.valueOf(logged);
            if (chan == null) {
                chan = dao.create(channel, isLogged, password);
            } else {
                chan.setLogged(isLogged);
                dao.save(chan);
            }
            responses.add(new Message(event.getChannel(), event, "Now joining " + channel +
                (isLogged ? " and logging it" : "")));
            chan.join(bot);
            responses.add(new Message(channel, event, "I was asked to join this channel by " + event.getSender()));
        } else {
            responses.add(new Message(event.getChannel(), event, "Channel names must start with #, " + event.getSender()));
        }
        return responses;
    }
}
