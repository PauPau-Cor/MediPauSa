package com.example.proyectoceti.ClassesAndModels;

import java.util.Date;

public class Code  {
    public String code;
    public Date date;
    public boolean used;

    public Code(String code, Date date, boolean used) {
        this.code = code;
        this.date = date;
        this.used = used;
    }

    public Code() {
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}
