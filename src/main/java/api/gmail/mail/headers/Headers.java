package api.gmail.mail.headers;

import com.google.api.services.gmail.model.MessagePartHeader;

import java.util.List;

public class Headers {
    List<MessagePartHeader> headers;

    public Headers(List<MessagePartHeader> headers) {
        this.headers = headers;
    }
}
