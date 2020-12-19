package org.apache.syncope.common.rest.api.batch;

import org.apache.syncope.common.rest.api.RESTHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class BatchPayloadParserTestImproved {

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
        Test added to cover the functional statement nr. 6 as described in the pdf.
        Crafting batch requests that are not compliant to the RFC2046
     */
    @Test
    public void parserTestImprovingStatementCoverage2() {

        if(!UtilTestClass.improved){
            return;
        }

        //These are INVALID batch requests with a single operation
        String invalidBatch1 =
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

        String invalidBatch2 =
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
                        "--";
                        //MISSING last batch boundary
        String invalidBatch3 =
                        //Missing the first batch boundary
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

        List<String> items = new ArrayList<>();
        items.add(invalidBatch1);
        items.add(invalidBatch2);
        items.add(invalidBatch3);

        for(String invalidBatch : items){
            try {
                BatchPayloadParser.parse(
                        new ByteArrayInputStream(invalidBatch.getBytes()),
                        mockMediaType,
                        new BatchRequestItem());
            } catch (Exception e) {
                Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getCanonicalName());
                return;
            }

            //If here fail
            Assert.fail();
        }
    }


    @Test
    public void blockCoverageIncreasingForSplitTest() {

        // Increasing Block coverage foe the
        // "for (BatchPayloadLine line : lines) {" Block, line 81

        //parse not tested for empty lines
        if(!UtilTestClass.improved){
            return;
        }

        //the BatchPayloadreader will return an empty array on batch payload line, which means that we will not go through
        //the for cycle commented above.
        //This will also increase block coverage for
        // "for (Iterator<BatchPayloadLine> itor = bodyPart.iterator(); itor.hasNext() && isHeader;) {", line 116
        String voidBatchRequest =
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--";

        String emptyLine = "";


        List<String> items = new ArrayList<>();
        items.add(voidBatchRequest);
        items.add(emptyLine);

        for(String lineToParse : items){

            try {
                List<BatchItem> lb = BatchPayloadParser.parse(
                        new ByteArrayInputStream(lineToParse.getBytes()),
                        mockMediaType,
                        new BatchRequestItem());
            } catch (Exception e) {
                //emptyLine has to be handled by raising this type of exception
                Assert.assertEquals("java.lang.IllegalArgumentException", e.getClass().getCanonicalName());
            }
        }

    }

    @Test
    public void increasingConditionCoverageConsumeHeadersTest() throws IOException {

        if(!UtilTestClass.improved){
            return;
        }

        //These are INVALID batch requests with a single operation
        //covering if (target.length > 1) {, line 140
        String multiTargetBatch =
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\r\n" +
                        "PATCH target1?target2?target3 HTTP/1.1 \n"+
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

        String batchWithInvalidMethod =
                "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                        "Content-Type: application/http\n" +
                        "Content-Transfer-Encoding: binary\n" +
                        "\r\n" +
                        "PIPPO paperino HTTP/1.1 \n"+
                        "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

        List<String> items = new ArrayList<>();
        items.add(multiTargetBatch);
        items.add(batchWithInvalidMethod);

        for(String lineToParse : items){

            List<BatchItem> lb = BatchPayloadParser.parse(
                    new ByteArrayInputStream(lineToParse.getBytes()),
                    mockMediaType,
                    new BatchResponseItem());

            List<BatchItem> lb2 = BatchPayloadParser.parse(
                    new ByteArrayInputStream(lineToParse.getBytes()),
                    mockMediaType,
                    new BatchRequestItem());

            checkParsedBatchItems(lb, 1);
            checkParsedBatchItems(lb2, 1);
        }
    }
    

    private void checkParsedBatchItems(List<BatchItem> listBatchItems, int expectedNum){

        assertEquals(expectedNum, listBatchItems.size());

        for(BatchItem b : listBatchItems){

            Assert.assertNotNull(b.getHeaders());

            Assert.assertNotNull(b.getContent());

        }
    }
}
