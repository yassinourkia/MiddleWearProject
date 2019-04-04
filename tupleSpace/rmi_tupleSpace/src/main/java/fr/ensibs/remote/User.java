/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ensibs.remote;

/**
 *
 * @author KAARAR
 */
public class User {
    public String username;
    public String password ;
    public Integer balance;
    public Role role ;

    public User() {
    }

    public User(String username, String password, Integer balance, Role role) {
        this.username = username;
        this.password = password;
        this.balance = balance;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}