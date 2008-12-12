package javabot.operations;

import java.util.ArrayList;
import java.util.List;

import javabot.BotEvent;
import javabot.Message;
import javabot.dao.KarmaDao;
import javabot.model.Karma;

public class KarmaReadOperation implements BotOperation {
    private KarmaDao karmaDao;

    public KarmaReadOperation(KarmaDao dao) {
        karmaDao = dao;
    }

    public List<Message> handleMessage(BotEvent event) {
        List<Message> messages = new ArrayList<Message>();
        String message = event.getMessage();
        String channel = event.getChannel();
        String sender = event.getSender();
        if(!message.startsWith("karma ")) {
            return messages;
        }
        String nick = message.substring("karma ".length());
        nick = nick.toLowerCase();
        if(nick.contains(" ")) {
            messages.add(new Message(channel, "I've never Seen a nick with a space " + "in, " + sender, false));
            return messages;
        }
        Karma karma = karmaDao.find(nick);
        if(karma != null) {
            if(nick.equals(sender)) {
                messages.add(new Message(channel, sender + ", you have a karma level of " + karma.getValue() + ".", false));
            } else {
                messages.add(new Message(channel, nick + " has a karma level of " + karma.getValue() + ", " + sender, false));
            }
        } else {
            messages.add(new Message(channel, nick + " has no karma, " + sender, false));
        }
        return messages;
    }

    public List<Message> handleChannelMessage(BotEvent event) {
        return new ArrayList<Message>();
    }
}