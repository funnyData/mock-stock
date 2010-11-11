package com.longone.broker.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

public class User implements IsSerializable{
    private String username;
    private String displayName;
    private String superUser;
    private Date startDate;
    private Date endDate;
    private double principal;
    private double initialPrincipal;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSuperUser() {
        return superUser;
    }

    public void setSuperUser(String superUser) {
        this.superUser = superUser;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getPrincipal() {
        return principal;
    }

    public void setPrincipal(double principal) {
        this.principal = principal;
    }

    public double getInitialPrincipal() {
        return initialPrincipal;
    }

    public void setInitialPrincipal(double initialPrincipal) {
        this.initialPrincipal = initialPrincipal;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
