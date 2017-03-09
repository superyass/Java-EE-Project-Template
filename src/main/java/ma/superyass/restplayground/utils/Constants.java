package ma.superyass.restplayground.utils;

/**
 * Application constants.
 */
public final class Constants {

    public final static String AUTHORIZATION_HEADER = "Authorization";
    //Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_'.@A-Za-z0-9-]*$";
    //Regex for acceptable emails
    public static final String EMAIL_REGEX = "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?";
    public static final String EMAIL_REGEX_MESSAGE = "{invalid.email}";

    private Constants() {
    }
}
