package org.apache.syncope.common.rest.api.batch;

import java.util.ArrayList;
import java.util.List;

public class UtilTestClass {

    public static List<List<Object>> multidimensionalTestCases(List<List<Object>> inputLists) {
        List<List<Object>> cartesianProducts = new ArrayList<>();
        if (inputLists != null && inputLists.size() > 0) {
            // separating the list at 0th index
            List<Object> initialList = inputLists.get(0);
            // recursive call
            List<List<Object>> remainingLists = multidimensionalTestCases(inputLists.subList(1, inputLists.size()));
            // calculating the cartesian product
            initialList.forEach(element -> {
                remainingLists.forEach(remainingList -> {
                    ArrayList<Object> cartesianProduct = new ArrayList<>();
                    cartesianProduct.add(element);
                    cartesianProduct.addAll(remainingList);
                    cartesianProducts.add(cartesianProduct);
                });
            });
        } else {
            // Base Condition for Recursion (returning empty List as only element)
            cartesianProducts.add(new ArrayList<>());
        }
        return cartesianProducts;
    }

}
