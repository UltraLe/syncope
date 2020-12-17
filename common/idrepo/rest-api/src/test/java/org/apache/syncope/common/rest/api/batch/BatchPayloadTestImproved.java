package org.apache.syncope.common.rest.api.batch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BatchPayloadTestImproved {

    @Mock
    private static MediaType mockMediaType = Mockito.mock(MediaType.class);

    @Mock
    private Map<String, String> map = Mockito.mock(Map.class);

    @Before
    public void initMock(){
        when(mockMediaType.getParameters()).thenReturn(map);
        when(map.get("boundary")).thenReturn(BatchPayloadParserTest.BATCH_BOUNDARY);
    }

    /*
        Test added to cover the functional statement nr. 7 as described in the pdf.
     */
    @Test
    public void parserTestImprovingStatementCoverage() throws IOException {

        if(!UtilTestClass.improved){
            return;
        }

        //This is a VALID batch request with a single operation
        String validBatch =
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\r\n" +
                        "PATCH /users/24eb15aebatch@syncope.apache.org HTTP/1.1 \n" +
                        "Accept: application/json\n" +
                        "Content-Length: 362\n" +
                        "Content-Type: application/json\n" +
                        "Prefer: return-no-content\n" +
                        "\r\n" +
                        "{\"@class\":\"org.apache.syncope.common.lib.patch.UserPatch\",\"key\":\"24eb15aebatch@syncope.apache.org\"}\n" +
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

        //This request should not be parsed
        String invalidBatch =
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "Content-Type: pippo\n" +
                        "Content-Transfer-Encoding: NON-binary\n" +
                        "\r\n" +
                        "PATCH /users/24eb15aebatch@syncope.apache.org HTTP/1.1 \n" +
                        "Accept: application/json\n" +
                        "Content-Length: 362\n" +
                        "Content-Type: application/json\n" +
                        "Prefer: return-no-content\n" +
                        "\r\n" +
                        "{\"@class\":\"org.apache.syncope.common.lib.patch.UserPatch\",\"key\":\"24eb15aebatch@syncope.apache.org\"}\n" +
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

        List<BatchItem> validBatchItems =  BatchPayloadParser.parse(
                new ByteArrayInputStream(validBatch.getBytes()),
                mockMediaType,
                new BatchRequestItem());


        //TODO remove
        for(int i = 0; i < validBatchItems.size(); ++i) {
            System.out.println("Response parsed:"+i+"\n");
            System.out.println(validBatchItems.get(i));
        }

        checkParsedBatchItems(validBatchItems, 1);

        List<BatchItem> invalidBatchItems = BatchPayloadParser.parse(
                new ByteArrayInputStream(invalidBatch.getBytes()),
                mockMediaType,
                new BatchRequestItem());


        //TODO remove
        for(int i = 0; i < invalidBatchItems.size(); ++i) {
            System.out.println("Response invalid parsed:"+i+"\n");
            System.out.println(invalidBatchItems.get(i));
        }

        checkParsedBatchItems(invalidBatchItems,1);

    }


    /*
        Test added to cover the functional statement nr. 6 as described in the pdf.
        Crafting batch requests that are not compliant to the RFC2046
     */
    @Test
    public void parserTestImprovingStatementCoverage2() {

        if(!UtilTestClass.improved){
            return;
        }

        //This is an INVALID batch request with a single operation
        String invalidBatch =
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\r\n" +
                        "PATCH /users/24eb15aebatch@syncope.apache.org HTTP/1.1 \n" +
                        "Accept: application/json\n" +
                        "Content-Length: 362\n" +
                        "Content-Type: application/json\n" +
                        "Prefer: return-no-content\n" +
                        "\r\n" +
                        "{\"@class\":\"org.apache.syncope.common.lib.patch.UserPatch\",\"key\":\"24eb15aebatch@syncope.apache.org\"}\n" +
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n";
                        //MISSING '--'


        List<BatchItem> invalidBatchItems = null;
        try {
            invalidBatchItems = BatchPayloadParser.parse(
                    new ByteArrayInputStream(invalidBatch.getBytes()),
                    mockMediaType,
                    new BatchRequestItem());
        } catch (Exception e) {
            Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getCanonicalName());
            return;
        }

        //UNREACHED CODE
        checkParsedBatchItems(invalidBatchItems, 1);

    }

    private void checkParsedBatchItems(List<BatchItem> listBatchItems, int expectedNum){

        assertEquals(expectedNum, listBatchItems.size());

        for(BatchItem b : listBatchItems){

            Assert.assertNotNull(b.getHeaders());

            Assert.assertNotNull(b.getContent());

        }
    }

    //TODO cosa succede se parso una risposta ma come contenuto metto una richiesta ?
    //      inviare anche al server
}
