package com.rrm.tdterminal;

/**
 * Created by Ryan-A-Laptop on 2016-11-25.
 */

public class ARMessageRequest {
    String Vendor;
    String CardNumber;
    double Amount;
    public ARMessageRequest(String vendor, String cardNumber, double amount)
    {
        this.Vendor = vendor;
        this.CardNumber = cardNumber;
        this.Amount = amount;
    }
}
