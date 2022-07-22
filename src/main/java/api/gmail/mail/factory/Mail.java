package api.gmail.mail.factory;

import api.gmail.mail.body.Body;
import api.gmail.mail.headers.Headers;

/**
 * Data structure holding the necessary information about the email.
 */
public class Mail {
    /**
     * Message id
     */
    String id;

    /**
     * Object holding the headers list.
     */
    Headers headers;

    /**
     * Object holding mainly body contents.
     */
    Body body;

    /**
     * Constructor, simple assignment.
     *
     * @param id      String
     * @param headers Header type object
     * @param body    Body type object
     */
    public Mail(String id, Headers headers, Body body) {
        this.headers = headers;
        this.body = body;
        this.id = id;
    }
}
