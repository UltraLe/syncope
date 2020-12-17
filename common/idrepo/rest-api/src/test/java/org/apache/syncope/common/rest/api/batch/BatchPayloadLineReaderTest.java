package org.apache.syncope.common.rest.api.batch;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@RunWith(value= Parameterized.class)
public class BatchPayloadLineReaderTest {

    private InputStream in;
    private MediaType mediaType;

    public BatchPayloadLineReaderTest(List<Object> parameters){

        MediaType mediaType = (MediaType)parameters.get(0);
        String httpPayload = (String)parameters.get(1);

        this.in = new ByteArrayInputStream(httpPayload.getBytes());
        this.mediaType = mediaType;
    }

    @Parameterized.Parameters
    public static Collection<List<Object>> getParameters(){

        List<Object> httpPayload = new ArrayList<>();
        List<Object> mediaTypes = new ArrayList<>();

        //Valid
        mediaTypes.add(MediaType.APPLICATION_JSON_TYPE);
        //Invalid
        mediaTypes.add(new MediaType("pippo984/*", "uygH"));
        //Valid
        httpPayload.add(
                "POST /cgi-bin/process.cgi HTTP/1.1\n" +
                "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\n" +
                "Host: www.blabla.com\n" +
                "Content-Type: text/xml; charset=utf-8\n" +
                "Content-Length: length\n" +
                "Accept-Language: en-us\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Connection: Keep-Alive\n" +
                "\n" +
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<string xmlns=\"http://clearforest.com/\">string</string>"
                );
        //Invalid
        httpPayload.add(
                "POOOST /cgi-bin/process.cgi HTTP/199.9\n" +
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<string xmlns=\"http://clearforest.com/\">string</string>"
        );

        List<List<Object>> parameters = new ArrayList<>();
        parameters.add(mediaTypes);
        parameters.add(httpPayload);


        if(UtilTestClass.improved){
            return UtilTestClass.multidimensionalTestCases(parameters);
        }
        return UtilTestClass.nonMultidimensionalTestCases(parameters);
    }


    @Test
    public void readTest() throws IOException {

        BatchPayloadLineReader reader = new BatchPayloadLineReader(in, mediaType);
        List<BatchPayloadLine> lines = reader.read();

        int i = 0;
        int numLines;
        for(BatchPayloadLine line : lines){
            Assert.assertTrue(line.getLineNumber() > 0);
            Assert.assertTrue(line.getLineNumber() >= i++);
            numLines = line.toString().split("\n").length;
            Assert.assertTrue(numLines == 0 || numLines == 1);
        }
    }
}
