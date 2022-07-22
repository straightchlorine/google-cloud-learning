package api.gmail.mail.body;

import com.google.api.services.gmail.model.MessagePart;

import java.util.List;

/**
 * Part of Mail class.
 * <p>
 * Meant to contain and later process the body of the mail.
 */
public class Body {
    /**
     * Contains snippet of the message(possibly will be deleted).
     */
    private final String snippet;

    /**
     * Contains type of the body(possibly will be resolved differently).
     */
    private String type;

    /**
     * Contains the body.
     */
    private final String body;

    /**
     * Contains the MessageParts at the bottom of the payload.
     */
    private final List<MessagePart> contents;

    /**
     * Constructor, simple assignment.
     *
     * @param snippet  String
     * @param body     String
     * @param contents List of MessageParts
     */
    public Body(String snippet, String body, List<MessagePart> contents) {
        this.snippet = snippet;
        this.body = body;
        this.contents = contents;
    }
}