package com.stcom.smartmealtable.component.creditmessage;

public interface CreditMessageParser {

    boolean checkVendor(String message);

    ExpenditureDto parse(String message);

}
