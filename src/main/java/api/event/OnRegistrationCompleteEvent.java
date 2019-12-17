package api.event;

import java.util.Locale;

import org.springframework.context.ApplicationEvent;

import api.model.User;

public class OnRegistrationCompleteEvent extends ApplicationEvent {
    

    private static final long serialVersionUID = -8919810540022992778L;
    public Locale locale;
    public User user;

    public OnRegistrationCompleteEvent(User user, Locale locale) {
        super(user);
        this.user = user;
        this.locale = locale == null ? Locale.UK : locale;
    }
}