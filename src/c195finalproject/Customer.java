/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;

/**
 *
 * @author Mesa
 */
public abstract class Customer implements Comparable<Customer>{
    private Integer custID;
    private String custFirstName;
    private String custLastName;
    private String address1;
    private String address2;
    private String postalCode;
    private String phone;
    
    public Customer(){
        
    }
    
    @Override
    public int compareTo(Customer cust){
        return custID.compareTo(cust.custID);
    }
    @Override
    public String toString(){
        return this.custLastName + " " + this.custFirstName;
    }
}
