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
public class Customer implements Comparable<Customer>{
    private Integer custID;
    private String custFirstName;
    private String custLastName;
    private String address1;
    private String address2;
    private String postalCode;
    private String phone;
    private String city;
    private String country;
    
    public Customer(int custID, String custName, String add1, String add2, String postCode, String phone, String city, String country){
        try{
            this.custID = custID;
            String[] arrName = custName.split(" ");
            this.custFirstName = arrName[0];
            this.custLastName = arrName[1];
            this.address1 = add1;
            this.address2 = add2;
            this.postalCode = postCode;
            this.phone = phone;
            this.city = city;
            this.country = country;
        }
        catch(NullPointerException e){System.out.println(e.getMessage());}
    }    
    @Override
    public int compareTo(Customer cust){
        return custID.compareTo(cust.custID);
    }
    @Override
    public String toString(){
        return this.custLastName + ", " + this.custFirstName;
    }
    public Integer getID(){
        return custID;
    }
    
    
}
