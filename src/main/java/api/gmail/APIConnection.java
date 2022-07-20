package api.gmail;

import api.gmail.exceptions.APIConnectionRuntimeException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.StringUtils;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import org.apache.commons.codec.binary.Base64;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class APIConnection {

    /**
     * Class-wide logger.
     */
    static Logger logger = Logger.getLogger(APIConnection.class.getName());

    /**
     * Global HttpTransport object
     */
    static final NetHttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new APIConnectionRuntimeException(e);
        }
    }

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "api connection";

    /**
     * JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    /**
     * Directory storing authentication tokens.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global user name.
     */
    private static final String USER = "me";

    public static void main(String... args) {
        List<Message> inbox = new ArrayList<>();
        // requests all the mails from the api
        getInbox(inbox);

        // just testing
        for (Message message : inbox) {
            logger.info(() -> "headers\n" + getBodyMimeType(getMessageHandle(message)));
            logger.info(() -> "message id: " + inbox.indexOf(message));
            logger.info(() -> "snippet" + getSnippet(getMessageHandle(message)));
            logger.info(() -> "body" + getBody(getMessageHandle(message)));
            logger.info(() -> "post");
        }
    }

    /**
     * Helper method.
     * <p>
     * Extracts body of the message from the handle.
     *
     * @param handle handle to the contents
     * @return String containing the body of the message
     */
    private static String getBody(Message handle) {
        String body = decode(handle.getPayload().getBody().getData());
        if (body != null) {
            return body;
        } else {
            return decode(handle.getPayload().getParts().get(0).getBody().getData());
        }
    }

    /**
     * Helper method to extract the mime type of the body
     *
     * @param handle handle to the contents of the message
     * @return String containing the mime type (eg. text/html)
     */
    private static String getBodyMimeType(Message handle) {
        return handle.getPayload().getParts().get(0).getMimeType();
    }

    /**
     * Helper method.
     * <p>
     * Decodes the MessagePartBody data into string.
     *
     * @param bodyPart coded body part
     * @return decoded string of the body part
     */
    private static String decode(String bodyPart) {
        return StringUtils.newStringUtf8(Base64.decodeBase64(bodyPart));
    }

    /**
     * Helper method.
     * <p>
     * Extracts snippet of the message from the handle.
     *
     * @param handle handle to the contents
     * @return String containing the snippet of the message
     */
    private static String getSnippet(Message handle) {
        return handle.getSnippet();
    }

    /**
     * Helper method
     * <p>
     * Retrieve contents of the email from the api based on the ID of the particular message.
     *
     * @param message basic Message type object (id, thread_id) JSON object
     * @return handle on the contents, so extraction of each part of the message is easier
     */
    private static Message getMessageHandle(Message message) {
        try {
            return getService()
                    .users()
                    .messages()
                    .get(USER, message.getId())
                    .setFormat("full")
                    .execute();
        } catch (IOException e) {
            throw new APIConnectionRuntimeException(e);
        }
    }

    /**
     * Method works with Gmail API to retrieve all the messages.
     * <p>
     * It iterates over the whole inbox, going page by page and adds every single new message
     * into the list given as the first parameter.
     *
     * @param inbox empty List<Message> will hold all the data
     * @return a list of all the messages
     */
    private static List<Message> getInbox(List<Message> inbox) {
        try {
            Gmail service = getService();
            Gmail.Users.Messages.List request = service.users().messages().list(USER);
            ListMessagesResponse response = request.execute();

            // runs until all messages are available locally
            while (response.getMessages() != null) {
                inbox.addAll(response.getMessages());
                if (response.getNextPageToken() != null) {
                    request.setPageToken(response.getNextPageToken());
                    response = request.execute();
                } else {
                    break;
                }
            }
            return inbox;
        } catch (IOException e) {
            throw new APIConnectionRuntimeException(e);
        }
    }

    /**
     * Gmail API service object creation
     */
    private static Gmail getService() throws IOException {
        // new authorized api client service
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Authentication method
     */
    private static Credential getCredentials() throws IOException {
        // loading the credentials to the program
        logger.log(Level.INFO, ">> loading credentials");
        InputStream in = APIConnection.class.getResourceAsStream("/credentials.json");
        if (in == null)
            throw new FileNotFoundException("Resource not found: " + "/credentials.json");
        // loading the credentials into client secrets
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        logger.log(Level.INFO, ">> loaded credentials into secrets");
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                APIConnection.HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
