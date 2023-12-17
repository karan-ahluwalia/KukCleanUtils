import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.script.Script;
import com.google.api.services.script.model.*;
import com.google.common.collect.Lists;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class KukCleanOrderClient {
    private static final String APPLICATION_NAME = "KukCleanOrderClient";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES =
            Lists.newArrayList("https://www.googleapis.com/auth/script.projects",
                    "https://www.googleapis.com/auth/spreadsheets",
                     "https://www.googleapis.com/auth/script.deployments"
                    , "https://www.googleapis.com/auth/script.deployments.readonly"
                    , "https://www.googleapis.com/auth/script.metrics"
                    , "https://www.googleapis.com/auth/script.processes"
                    , "https://www.googleapis.com/auth/script.projects"
                    , "https://www.googleapis.com/auth/script.projects.readonly"
                    );
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = KukCleanOrderClient.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("karan.ahluwalia1991@gmail.com");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Script service =
                new Script.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

        String scriptId = "AKfycbwd4n8Hvz19pR4tZpp9nJkO6JA6dX8ps_eIkyeCndljR2fvPEsuq8AZM0q2MTlqmng7";
        ExecutionRequest request = new ExecutionRequest()
                .setFunction("doGet");

        Operation op =
                service.scripts().run(scriptId, request).execute();

        if(op.getError() != null){
            System.out.println(getScriptError(op));
        }else{
            System.out.println(op.getResponse());
        }


        /*Script.Projects projects = service.projects();

        // Creates a new script project.
        Project createOp = projects.create(new CreateProjectRequest().setTitle("My Script")).execute();

        // Uploads two files to the project.
        File file1 = new File()
                .setName("hello")
                .setType("SERVER_JS")
                .setSource("function helloWorld() {\n  console.log(\"Hello, world!\");\n}");
        File file2 = new File()
                .setName("appsscript")
                .setType("JSON")
                .setSource("{\"timeZone\":\"America/New_York\",\"exceptionLogging\":\"CLOUD\"}");
        Content content = new Content().setFiles(Arrays.asList(file1, file2));
        Content updatedContent = projects.updateContent(createOp.getScriptId(), content).execute();

        // Logs the project URL.
        System.out.printf("https://script.google.com/d/%s/edit\n", updatedContent.getScriptId());*/
    }

    public static String getScriptError(Operation op) {
        if (op.getError() == null) {
            return null;
        }

        // Extract the first (and only) set of error details and cast as a Map.
        // The values of this map are the script's 'errorMessage' and
        // 'errorType', and an array of stack trace elements (which also need to
        // be cast as Maps).
        Map<String, Object> detail = op.getError().getDetails().get(0);
        List<Map<String, Object>> stacktrace =
                (List<Map<String, Object>>) detail.get("scriptStackTraceElements");

        java.lang.StringBuilder sb =
                new StringBuilder("\nScript error message: ");
        sb.append(detail.get("errorMessage"));
        sb.append("\nScript error type: ");
        sb.append(detail.get("errorType"));

        if (stacktrace != null) {
            // There may not be a stacktrace if the script didn't start
            // executing.
            sb.append("\nScript error stacktrace:");
            for (Map<String, Object> elem : stacktrace) {
                sb.append("\n  ");
                sb.append(elem.get("function"));
                sb.append(":");
                sb.append(elem.get("lineNumber"));
            }
        }
        sb.append("\n");
        return sb.toString();
    }
}
