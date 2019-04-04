/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ensibs.remote;

import java.rmi.Remote;

/**
 *
 * @author Kaarar
 */
public interface DeviceManager extends Remote {
    
    // create user / product
    
    // modifie user / product 
    
    // delete user / product
    
    //read user / product
    
    //login /logout 
    
    // payment systeme 
    
       
    
    /**
     * create user by 
     * @param username
     * @param password
     * @param balance
     * @param role
     */
    public void createUser(String username,String password ,Integer balance, Role role );
    
    /**
     * delete user by 
     * @param username
     */
    public void deleteUser(String username );
    
    /**
     * read user by 
     * @param username
     */
    public void readUser(String username );
    
    /**
     * update user by 
     * @param username
     */
    public void updateUser(String username );
    
    
    /**
     * login by 
     * @param username
     * @param password
     */
    public void login(String username , String password );
    
    
    /**
     * logout by 
     * @param username
     * @param password
     */
    public void logout(String username , String password );
    
}
