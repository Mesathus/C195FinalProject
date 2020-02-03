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
    private Integer custID = null;
    private String custName;
    private String custFirstName;
    private String custLastName;
    private Integer addressID = null;
    private Boolean active;
    private String address1;
    private String address2;
    private String postalCode;
    private String phone;
    private String city;
    private String country;
    
    public Customer(int custID, String custName, Integer addressID, Boolean active,String add1, String add2, String postCode, String phone, String city, String country){
        try{
            this.custID = custID;
            String[] arrName = custName.split(" ");
            this.custFirstName = arrName[0];
            this.custLastName = arrName[1];
            this.addressID = addressID;
            this.active = active;
            this.address1 = add1;
            this.address2 = add2;
            this.postalCode = postCode;
            this.phone = phone;
            this.city = city;
            this.country = country;
        }
        catch(NullPointerException e){System.out.println(e.getMessage());}
    }
    public Customer(String custName, String address, Boolean active,String postCode, String phone, String city, String country){
        try{
            String[] arrName = custName.split(" ");
            this.custFirstName = arrName[0];
            this.custLastName = arrName[1];
            this.active = active;
            String[] addr = address.split(",");
            this.address1 = addr[0];
            this.address2 = addr[1];
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
    public String getName(){
        return custFirstName + " " + custLastName;
    }
    public Integer getAddrID(){
        return addressID;
    }
    public Boolean getActive(){
        return active;
    }
    public String[] getAddr(){
        String[] addr = new String[]{address1,address2};
        return addr;
    }
    public String getZip(){
        return postalCode;
    }
    public String getPhone(){
        return phone;
    }
    public String getCity(){
        return city;
    }
    public String getCountry(){
        return country;
    }
}
