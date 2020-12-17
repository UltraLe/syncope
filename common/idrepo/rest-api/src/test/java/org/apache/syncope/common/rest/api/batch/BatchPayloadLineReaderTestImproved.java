package org.apache.syncope.common.rest.api.batch;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BatchPayloadLineReaderTestImproved {

    private InputStream in;
    //default value, could be arbitrary
    private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

    @Test
    public void improvedReadTest() throws IOException {

        if(!UtilTestClass.improved){
            return;
        }

        /*
                Covering condition if (currentLine != null) {, line 156...

                This happens if the private method readLine() returns null, and this
                happens if the input reader has no data.
         */

        in = new ByteArrayInputStream("".getBytes());

        BatchPayloadLineReader reader = new BatchPayloadLineReader(in, mediaType);
        List<BatchPayloadLine> lines = reader.read();

        Assert.assertEquals(0, lines.size());
    }

    @Test
    public void readFromBigSourceTest() throws IOException {
        if(!UtilTestClass.improved){
            return;
        }
        /*
            Covering
            if (!innerBuffer.hasRemaining()) {, line 102
         */

        String bigString = getRandomString(8192*10);

        in = new ByteArrayInputStream(bigString.getBytes());

        BatchPayloadLineReader reader = new BatchPayloadLineReader(in, mediaType);
        List<BatchPayloadLine> lines = reader.read();

        in = new ByteArrayInputStream((bigString.concat("\n\n\n\n")).getBytes());

        reader = new BatchPayloadLineReader(in, mediaType);
        lines = reader.read();

        in = new ByteArrayInputStream((bigString.concat("\r\r\r\r\r\r\r")).getBytes());

        reader = new BatchPayloadLineReader(in, mediaType);
        lines = reader.read();

        in = new ByteArrayInputStream((bigString.concat("\r\n\r\n\r\n\n\r\r\n")).getBytes());

        reader = new BatchPayloadLineReader(in, mediaType);
        lines = reader.read();

        Assert.assertFalse(lines.isEmpty());
    }

    protected String getRandomString(int len) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < len) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

























    //TODO maybe later
    public void readBufferOverflowTest() throws Exception {
        if (!UtilTestClass.improved) {
            return;
        }

        //ByteBuffer tmp = ByteBuffer.allocate(innerBuffer.limit() * 2);, line 104
        //Possible buffer overflow with post request
        String GET_ADDRESS = "https://syncope-vm.apache.org/syncope/rest/users";
        String POST_ADDRESS = "https://syncope-vm.apache.org/syncope/rest/batch";
        String response = sendGet(GET_ADDRESS);

        if(response.length() < 100 ){
            //the server is down
            return;
        }

        String probeRequestDataPost =
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\r\n" +
                        "POST /users HTTP/1.1\n" +
                        "Accept: application/json\n" +
                        "Content-Type: application/json\n" +
                        "\r\n" +
                        "{\"name\"=\"pippo\",\"description\"=\"wow\"}\n" +
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

        Random rnd = new Random(123456789);
        int BUFFER_SIZE_MB = 1;
        while (true) {
            byte[] averyHeavyBuffer = new byte[BUFFER_SIZE_MB * 1000000];
            rnd.nextBytes(averyHeavyBuffer);

            response = sendPost(POST_ADDRESS, String.format(probeRequestDataPost, Arrays.toString(averyHeavyBuffer)));

            System.out.println("Batch response body:\n" + response + "\n");

            break;
            //System.out.println("MB sent: " + BUFFER_SIZE_MB + " replyied in: " + (end - start) / 1000 + " sec");
            //BUFFER_SIZE_MB++;
        }
    }

    private static String sendPost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;

        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Content-Type", "multipart/mixed");
            connection.setRequestProperty("Authorization", "Basic YWRtaW46cGFzc3dvcmQ=");
            connection.setRequestProperty("User-Agent", "intellijdiPippo");
            connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String sendGet(String address) throws Exception {

        URL u = new URL(address);

        URLConnection yc = u.openConnection();
        yc.setRequestProperty("Authorization", "Basic YWRtaW46cGFzc3dvcmQ=");
        yc.setRequestProperty("User-Agent", "intellijdiPippo");
        yc.setRequestProperty("Accept", "*/*");

        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine;
        StringBuilder s = new StringBuilder();
        while ((inputLine = in.readLine()) != null){
            s.append(inputLine);
        }
        in.close();

        return s.toString();
    }
}
