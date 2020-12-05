package org.apache.syncope.common.rest.api.batch;


import org.junit.Assert;
import org.junit.Test;

public class PitTest {

    //LOCALMENTE, solo un modulo
    //mvn -pl .,common/idrepo/rest-api -U -T 1C test -Dtest=org.apache.syncope.common.rest.api.batch.*Test -Dinvoker.streamLogs=true -Dmodernizer.skip=true -Dianal.skip=true -Drat.skip=true -Dcheckstyle.skip=true -Dsass.skip=true -DfailIfNoTests=false
    //ONLINE
    //see travis

    @Test
    public void simpleTest(){

        DeleteMePlease dmp1 = new DeleteMePlease(false);
        DeleteMePlease dmp2 = new DeleteMePlease(true);


        Assert.assertEquals(dmp1.getSomething(), "notA");
        Assert.assertEquals(dmp2.getSomething(), "A");
    }
}
