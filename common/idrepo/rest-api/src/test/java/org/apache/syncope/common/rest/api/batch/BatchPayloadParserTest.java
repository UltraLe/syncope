package org.apache.syncope.common.rest.api.batch;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.syncope.common.rest.api.Preference;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(value= Parameterized.class)
public class BatchPayloadParserTest {
    /*
        Classe usata per parsare in formato json la risposta del server (al path syncope/rest) ad una richiesta
        di un utente (usanro le REST api).

        Aspettative: indipendentemente dal tipo di richiesta, la risposta deve essere parsata in modo corretto,
        ovvero in modo da poter essere riconoscibile ed usabile, attraverso la classe BatchResponseItem.
        Vale a dire che dopo aver inviato una richiesta, dovremmo essere in grado di accedere ad ogni campo
        della classe BatchResponseItem, i cui campi saranno crati sulla base del metodo parse(). (giusto ?)

        Test: testeremo tale classe inviando:
        -richieste lecite;
        -richieste non lecite;
        -stringhe totalmente casuali;
        -stringa vuota;
        -richiesta non lecita di grandi dimensioni;

     */

    private static String BATCH_BOUNDARY = "batch_61bfef8d-0a00-41aa-b775-7b6efff37652";

    private static final String SAMPLE_BATCH_REQ_VALID =
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "POST /users HTTP/1.1 \n" +
                                            "Accept: application/json\n" +
                                            "Content-Length: 1157\n" +
                                            "Content-Type: application/json\n" +
                                            "\r\n" +
                                            "{\"@class\":\"org.apache.syncope.common.lib.to.UserTO\",\"key\":null,\"type\":\"USER\",\"realm\":\"/\"}\n" +
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\r\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "POST /groups HTTP/1.1 \n" +
                                            "Accept: application/xml\n" +
                                            "Content-Length: 628\n" +
                                            "Content-Type: application/xml\n" +
                                            "\r\n" +
                                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><syncope21:group xmlns:syncope21=\"http://syncope.apache.org/2.1\">\n" +
                                            "</syncope21:group>\n" +
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
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "DELETE /groups/287ede7c-98eb-44e8-979d-8777fa077e12 HTTP/1.1 \n" +
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

    private static final String SAMPLE_BATCH_RESP_VALID =
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "HTTP/1.1 201 Created \n" +
                                            "Content-Type: application/json\n" +
                                            "Date: Thu, 09 Aug 2018 09:55:46 GMT\n" +
                                            "ETag: \"1533808545975\"\n" +
                                            "Location: http://localhost:9080/syncope/rest/users/d399ba84-12e3-43d0-99ba-8412e303d083\n" +
                                            "X-Syncope-Domain: Master\n" +
                                            "X-Syncope-Key: d399ba84-12e3-43d0-99ba-8412e303d083\n" +
                                            "\r\n" +
                                            "{\"entity\":{\"@class\":\"org.apache.syncope.common.lib.to.UserTO\"}\n" +
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "HTTP/1.1 201 Created \n" +
                                            "Content-Type: application/xml\n" +
                                            "Date: Thu, 09 Aug 2018 09:55:46 GMT\n" +
                                            "ETag: \"1533808546342\"\n" +
                                            "Location: http://localhost:9080/syncope/rest/groups/843b2fc3-b8a8-4a8b-bb2f-c3b8a87a8b2e\n" +
                                            "X-Syncope-Domain: Master\n" +
                                            "X-Syncope-Key: 843b2fc3-b8a8-4a8b-bb2f-c3b8a87a8b2e\n" +
                                            "\r\n" +
                                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                            "<syncope21:provisioningResult xmlns:syncope21=\"http://syncope.apache.org/2.1\"></syncope21:provisioningResult>\n" +
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "HTTP/1.1 204 No Content \n" +
                                            "Content-Length: 0\n" +
                                            "Date: Thu, 09 Aug 2018 09:55:47 GMT\n" +
                                            "Preference-Applied: return-no-content\n" +
                                            "X-Syncope-Domain: Master\n" +
                                            "\r\n" +
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652\n" +
                                            "Content-Type: application/http\n" +
                                            "Content-Transfer-Encoding: binary\n" +
                                            "\r\n" +
                                            "HTTP/1.1 200 OK \n" +
                                            "Content-Type: application/json\n" +
                                            "Date: Thu, 09 Aug 2018 09:55:47 GMT\n" +
                                            "X-Syncope-Domain: Master\n" +
                                            "\r\n" +
                                            "{\"entity\":{\"@class\":\"org.apache.syncope.common.lib.to.GroupTO\"}\n" +
                                            "--batch_61bfef8d-0a00-41aa-b775-7b6efff37652--\n";

    //Removing the first batch boundary making invalid request/response
    private static final String SAMPLE_BATCH_REQ_INV = SAMPLE_BATCH_REQ_VALID.substring(10);
    private static final String SAMPLE_BATCH_RESP_INV = SAMPLE_BATCH_RESP_VALID.substring(10);

    private static int itemsNum;

    @Mock
    private static MediaType mockMediaType = Mockito.mock(MediaType.class);

    @Mock
    private Map<String, String> map = Mockito.mock(Map.class);

    private MediaType mediaType;
    private BatchItem batchItem;
    private String batchPayload;
    private List<BatchItem> listBatchItems;

    //using demo Virtual Machine
    //TODO chiamare il server solo se si ha trovato un bug e si vuole fare un proof of work.
    private static final String ADDRESS = "https://syncope-vm.apache.org/syncope/rest";

    @Before
    public void initMock(){
        when(mockMediaType.getParameters()).thenReturn(map);
        when(map.get("boundary")).thenReturn(BATCH_BOUNDARY);
    }

    @Parameterized.Parameters
    public static Collection<List<Object>> getParameters() {

        List<Object> batchItems = new ArrayList<>();
        List<Object> mediaTypes = new ArrayList<>();
        List<Object> batchItemsType = new ArrayList<>();

        itemsNum = 4;

        //test 1 and 2 shoud work fine
        batchItems.add(SAMPLE_BATCH_REQ_VALID);
        mediaTypes.add(mockMediaType);
        batchItemsType.add(new BatchRequestItem());

        batchItems.add(SAMPLE_BATCH_RESP_VALID);
        mediaTypes.add(mockMediaType);
        batchItemsType.add(new BatchResponseItem());

        //test 3 and 4 should not work
        batchItems.add(SAMPLE_BATCH_REQ_VALID);
        mediaTypes.add(new MediaType());
        batchItemsType.add(new BatchRequestItem());

        batchItems.add(SAMPLE_BATCH_RESP_VALID);
        mediaTypes.add(new MediaType());
        batchItemsType.add(new BatchResponseItem());

        //test 5 and 6 should not work
        batchItems.add(SAMPLE_BATCH_REQ_INV);
        mediaTypes.add(mockMediaType);
        batchItemsType.add(new BatchRequestItem());

        batchItems.add(SAMPLE_BATCH_RESP_INV);
        mediaTypes.add(mockMediaType);
        batchItemsType.add(new BatchRequestItem());


        List<List<Object>> parameters = new ArrayList<>();
        parameters.add(batchItems);
        parameters.add(mediaTypes);
        parameters.add(batchItemsType);

        if(!UtilTestClass.improved){
            return UtilTestClass.nonMultidimensionalTestCases(parameters);
        }
        return UtilTestClass.multidimensionalTestCases(parameters);
    }

    public BatchPayloadParserTest(List<Object> parameters){
        this.batchPayload = (String)parameters.get(0);
        this.mediaType = (MediaType)parameters.get(1);
        this.batchItem = (BatchItem)parameters.get(2);
    }

    @Test
    public void parseBatchRequestResponseTest() throws IOException {

        try {
            listBatchItems = BatchPayloadParser.parse(
                    new ByteArrayInputStream(batchPayload.getBytes()),
                    mediaType,
                    (BatchRequestItem) batchItem);
        }catch(ClassCastException e){
            //If there is an exception as below, ignore it.
            //It is generated by incompatible parameter product.
            Assert.assertEquals("java.lang.ClassCastException", e.getClass().getCanonicalName());
            return;
        }catch(NullPointerException np){
            if(Arrays.toString(np.getStackTrace()).contains("BatchPayloadParser.split") &&
                    (mediaType.getParameters().get("boundary") == null)  ){
                //ignore
                return;
            }
        }

        int expected = itemsNum;
        if(batchPayload.equals(SAMPLE_BATCH_REQ_INV) || batchPayload.equals(SAMPLE_BATCH_RESP_INV)){
            expected--;
        }

        checkParsedBatchItems(expected);
    }


    private void checkParsedBatchItems(int expectedNum){

        assertEquals(expectedNum, listBatchItems.size());

        for(BatchItem b : listBatchItems){

            Assert.assertNotNull(b.getHeaders());

            Assert.assertNotNull(b.getContent());

        }
    }

    //TODO, usare questo nei test migliorativi, mettendo generaazione randomica di batch item
    public void testTest() throws IOException {
        String boundary = "--batch_" + UUID.randomUUID().toString();

        WebClient request = WebClient.create(ADDRESS).path("batch").
                header(HttpHeaders.AUTHORIZATION, "Bearer ").
                type(RESTHeaders.multipartMixedWith(boundary.substring(2)));

        Response response = request.post(requestBody(boundary));
        //Response response = request.post(SAMPLE_BATCH_REQ_VALID);

        String body = IOUtils.toString((InputStream) response.getEntity(), String.valueOf(StandardCharsets.UTF_8));
        System.out.println("MEDIA TYPE:\n"+ response.getMediaType().getParameters().get("boundary"));
        System.out.println("Batch response body:\n"+ body);

        List<BatchResponseItem> lines =  BatchPayloadParser.parse(
                new ByteArrayInputStream(body.getBytes()),
                response.getMediaType(),
                new BatchResponseItem());


        for(int i = 0; i < lines.size(); ++i) {
            System.out.println("Response parsed:"+i+"\n");
            System.out.println(lines.get(i));
        }
    }

    private static String requestBody(final String boundary) {
        List<BatchRequestItem> reqItems = new ArrayList<>();

        BatchRequestItem createUser = new BatchRequestItem();
        createUser.setMethod(HttpMethod.POST);
        createUser.setRequestURI("/users");
        createUser.setHeaders(new HashMap<>());
        createUser.getHeaders().put(HttpHeaders.ACCEPT, List.of(RESTHeaders.APPLICATION_YAML));
        createUser.getHeaders().put(HttpHeaders.CONTENT_TYPE, List.of(RESTHeaders.APPLICATION_YAML));
        createUser.getHeaders().put(HttpHeaders.CONTENT_LENGTH, List.of(2));
        createUser.setContent("ok");
        reqItems.add(createUser);


        BatchRequestItem createGroup = new BatchRequestItem();
        createGroup.setMethod(HttpMethod.POST);
        createGroup.setRequestURI("/groups");
        createGroup.setHeaders(new HashMap<>());
        createGroup.getHeaders().put(HttpHeaders.ACCEPT, List.of(MediaType.APPLICATION_XML));
        createGroup.getHeaders().put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_XML));
        createGroup.getHeaders().put(HttpHeaders.CONTENT_LENGTH, List.of(2));
        createGroup.setContent("ok");
        reqItems.add(createGroup);

        BatchRequestItem updateUser = new BatchRequestItem();
        updateUser.setMethod(HttpMethod.PATCH);
        updateUser.setRequestURI("/users/" + "rossini");
        updateUser.setHeaders(new HashMap<>());
        updateUser.getHeaders().put(RESTHeaders.PREFER, List.of(Preference.RETURN_NO_CONTENT.toString()));
        updateUser.getHeaders().put(HttpHeaders.ACCEPT, List.of(MediaType.APPLICATION_JSON));
        updateUser.getHeaders().put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON));
        updateUser.getHeaders().put(HttpHeaders.CONTENT_LENGTH, List.of(2));
        updateUser.setContent("updateUserPayload");
        reqItems.add(updateUser);


        // 4. attempt to invoke an unexisting endpoint
        BatchRequestItem endpointNotFound = new BatchRequestItem();
        endpointNotFound.setMethod(HttpMethod.PATCH);
        endpointNotFound.setRequestURI("/missing");
        reqItems.add(endpointNotFound);

        // 5. attempt to delete an unexisting group
        BatchRequestItem groupNotFound = new BatchRequestItem();
        groupNotFound.setMethod(HttpMethod.DELETE);
        groupNotFound.setRequestURI("/groups/" + UUID.randomUUID());
        reqItems.add(groupNotFound);

        // 6, delete the group created above, expect deleted group as JSON
        BatchRequestItem deleteGroup = new BatchRequestItem();
        deleteGroup.setMethod(HttpMethod.DELETE);
        deleteGroup.setRequestURI("/groups/" + "pippo");
        reqItems.add(deleteGroup);

        String body = BatchPayloadGenerator.generate(reqItems, boundary);

        System.out.println("REQUEST BODY:"+body);

        return body;
    }
}
