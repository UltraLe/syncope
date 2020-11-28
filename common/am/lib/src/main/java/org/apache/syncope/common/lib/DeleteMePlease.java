package org.apache.syncope.common.lib;

public class DeleteMePlease {

    boolean a;

    public DeleteMePlease(boolean a){
        this.a = a;
    }

    public String getSomething(){
        if(!a){
            return "notA";
        }
        return "A";
    }

}
